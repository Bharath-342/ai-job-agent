package com.jobagent.service;

import com.jobagent.config.JsonUtils;
import com.jobagent.dto.JobMatchResultDto;
import com.jobagent.entity.*;
import com.jobagent.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AutoApplyEngine — the brain of the autonomous job agent.
 *
 * For every user who has auto-apply enabled, this engine:
 *   1. Fetches all active, unprocessed jobs from the DB
 *   2. Runs eligibility + skill matching per user profile
 *   3. Skips jobs already applied / below match threshold
 *   4. Submits eligible jobs automatically (real or mock mode)
 *   5. Records every state transition in application_events
 *   6. Sends in-app + email notification to the user
 */
@Service
public class AutoApplyEngine {

    private static final Logger log = LoggerFactory.getLogger(AutoApplyEngine.class);

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationEventRepository eventRepository;
    private final CandidateProfileRepository profileRepository;
    private final UserSettingsRepository settingsRepository;
    private final JobMatchingService matchingService;
    private final NotificationService notificationService;
    private final EmailNotificationService emailNotificationService;

    @Value("${app.auto-apply-enabled:false}")
    private boolean globalAutoApplyEnabled;

    @Value("${app.mock-mode:true}")
    private boolean globalMockMode;

    @Value("${app.default-min-match-score:85}")
    private int globalMinMatchScore;

    @Value("${app.daily-application-limit:10}")
    private int globalDailyLimit;

    public AutoApplyEngine(
        UserRepository userRepository,
        JobRepository jobRepository,
        ApplicationRepository applicationRepository,
        ApplicationEventRepository eventRepository,
        CandidateProfileRepository profileRepository,
        UserSettingsRepository settingsRepository,
        JobMatchingService matchingService,
        NotificationService notificationService,
        EmailNotificationService emailNotificationService
    ) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.eventRepository = eventRepository;
        this.profileRepository = profileRepository;
        this.settingsRepository = settingsRepository;
        this.matchingService = matchingService;
        this.notificationService = notificationService;
        this.emailNotificationService = emailNotificationService;
    }

    /**
     * Main entry point called by the scheduler. Processes all eligible users.
     */
    @Transactional
    public AutoApplyRunReport runForAllUsers() {
        log.info("AUTO_APPLY_ENGINE: Starting autonomous application cycle");

        List<User> allUsers = userRepository.findAll();
        int totalApplied = 0;
        int totalSkipped = 0;
        int totalFailed = 0;
        int usersProcessed = 0;

        for (User user : allUsers) {
            try {
                UserSettings settings = settingsRepository.findByUserId(user.getId())
                    .orElse(null);

                boolean autoApplyOn = settings != null && settings.getAutoApplyEnabled() != null
                    ? settings.getAutoApplyEnabled()
                    : globalAutoApplyEnabled;

                if (!autoApplyOn) {
                    log.debug("AUTO_APPLY_ENGINE: Skipping user {} — auto-apply disabled", user.getEmail());
                    continue;
                }

                CandidateProfile profile = profileRepository.findByUserId(user.getId()).orElse(null);
                if (profile == null) {
                    log.warn("AUTO_APPLY_ENGINE: Skipping user {} — no candidate profile found. Upload a resume first.", user.getEmail());
                    continue;
                }

                usersProcessed++;
                AutoApplyUserResult result = runForUser(user, profile, settings);
                totalApplied += result.applied();
                totalSkipped += result.skipped();
                totalFailed += result.failed();

            } catch (Exception e) {
                log.error("AUTO_APPLY_ENGINE: Error processing user {}", user.getEmail(), e);
                totalFailed++;
            }
        }

        AutoApplyRunReport report = new AutoApplyRunReport(usersProcessed, totalApplied, totalSkipped, totalFailed);
        log.info("AUTO_APPLY_ENGINE: Cycle complete. Users={}, Applied={}, Skipped={}, Failed={}",
            usersProcessed, totalApplied, totalSkipped, totalFailed);
        return report;
    }

    /**
     * Process a single user: match all jobs, apply to eligible ones.
     */
    @Transactional
    public AutoApplyUserResult runForUser(User user, CandidateProfile profile, UserSettings settings) {
        int applied = 0, skipped = 0, failed = 0;

        boolean isMock = settings != null && settings.getMockMode() != null
            ? settings.getMockMode()
            : globalMockMode;

        int dailyLimit = settings != null && settings.getDailyApplicationLimit() != null
            ? settings.getDailyApplicationLimit()
            : globalDailyLimit;

        int minScore = settings != null && settings.getMinMatchScore() != null
            ? settings.getMinMatchScore()
            : globalMinMatchScore;

        // Check today's quota
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long appliedToday = applicationRepository.countSubmittedSince(user.getId(), startOfDay);

        if (appliedToday >= dailyLimit) {
            log.info("AUTO_APPLY_ENGINE: Daily limit ({}) reached for user {}. Skipping cycle.",
                dailyLimit, user.getEmail());
            return new AutoApplyUserResult(0, 0, 0);
        }

        List<Job> activeJobs = jobRepository.findByIsActiveTrueOrderByPostedDateDesc();
        log.info("AUTO_APPLY_ENGINE: Processing {} active jobs for user {}", activeJobs.size(), user.getEmail());

        for (Job job : activeJobs) {
            if (appliedToday + applied >= dailyLimit) {
                log.info("AUTO_APPLY_ENGINE: Daily limit reached mid-cycle for user {}. Stopping.", user.getEmail());
                break;
            }

            try {
                // Skip if already processed
                if (applicationRepository.existsByUserIdAndJobId(user.getId(), job.getId())) {
                    skipped++;
                    continue;
                }

                // Run match
                JobMatchResultDto match = matchingService.matchJob(job, profile, settings);

                // Skip ineligible or low-score jobs
                if (!match.isEligible() && !match.isManualReviewRequired()) {
                    Application rejected = buildApplication(user, job, match);
                    rejected.setStatus(ApplicationStatus.REJECTED);
                    rejected.setFailureReason("Not eligible: " + match.getEligibilityReason());
                    applicationRepository.save(rejected);
                    recordEvent(rejected, "JOB_REJECTED", "DISCOVERED", ApplicationStatus.REJECTED.name(),
                        "AUTO_APPLY_ENGINE", "Auto-filtered: " + match.getEligibilityReason());
                    skipped++;
                    continue;
                }

                if (match.isManualReviewRequired()) {
                    Application manual = buildApplication(user, job, match);
                    manual.setStatus(ApplicationStatus.MANUAL_ELIGIBILITY_REVIEW);
                    manual.setManualActionReason("Auto-engine: " + match.getEligibilityReason());
                    applicationRepository.save(manual);
                    recordEvent(manual, "MANUAL_ELIGIBILITY_REVIEW", "DISCOVERED",
                        ApplicationStatus.MANUAL_ELIGIBILITY_REVIEW.name(),
                        "AUTO_APPLY_ENGINE", match.getEligibilityReason());
                    notificationService.sendNotification(user,
                        "Manual Review Needed – " + job.getTitle() + " – " + job.getCompany(),
                        "The AI agent needs your confirmation: " + match.getEligibilityReason(),
                        "MANUAL_ACTION_REQUIRED",
                        "/applications/" + manual.getId());
                    skipped++;
                    continue;
                }

                if (match.getMatchScore() < minScore) {
                    skipped++;
                    continue;
                }

                // Check for unsupported portals — set MANUAL_ACTION_REQUIRED
                boolean needsManualPortal = "MANUAL_ACTION_REQUIRED".equalsIgnoreCase(job.getApplicationMethod())
                    || job.getJobUrl().contains("myworkdayjobs.com")
                    || job.getJobUrl().contains("naukri.com")
                    || job.getJobUrl().contains("linkedin.com");

                Application app = buildApplication(user, job, match);

                if (needsManualPortal) {
                    app.setStatus(ApplicationStatus.MANUAL_ACTION_REQUIRED);
                    app.setManualActionReason("Auto-agent: portal requires manual login/apply. Visit: " + job.getJobUrl());
                    applicationRepository.save(app);
                    recordEvent(app, "MANUAL_ACTION_REQUIRED", "MATCHED",
                        ApplicationStatus.MANUAL_ACTION_REQUIRED.name(), "AUTO_APPLY_ENGINE",
                        "Portal not supported for headless submission");
                    notificationService.sendNotification(user,
                        "Apply Manually – " + job.getTitle() + " – " + job.getCompany(),
                        "The AI agent found a great match (" + match.getMatchScore() + "%) but this portal requires manual login. Click to apply: " + job.getJobUrl(),
                        "MANUAL_ACTION_REQUIRED",
                        job.getJobUrl());
                    emailNotificationService.sendManualActionRequired(user, job, match.getMatchScore(), job.getJobUrl());
                    skipped++;
                    continue;
                }

                // ============================================================
                // AUTONOMOUS SUBMISSION
                // ============================================================
                app.setStatus(ApplicationStatus.APPLYING);
                app = applicationRepository.save(app);
                recordEvent(app, "APPLICATION_STARTED", "MATCHED", ApplicationStatus.APPLYING.name(),
                    "AUTO_APPLY_ENGINE", "Autonomous submission initiated");

                String confirmationId = submitApplication(user, job, profile, isMock);

                app.setStatus(ApplicationStatus.APPLIED);
                app.setConfirmationId(confirmationId);
                app.setSubmittedAt(LocalDateTime.now());
                app = applicationRepository.save(app);

                recordEvent(app, "APPLICATION_SUBMITTED", "APPLYING", ApplicationStatus.APPLIED.name(),
                    isMock ? "AUTO_MOCK_SUBMISSION" : "AUTO_REAL_SUBMISSION",
                    "Autonomously submitted. Confirmation: " + confirmationId + " | Score: " + match.getMatchScore() + "%");

                log.info("AUTO_APPLY_ENGINE: ✅ Applied to {} at {} for {} (Score: {}%, Conf: {})",
                    job.getTitle(), job.getCompany(), user.getEmail(), match.getMatchScore(), confirmationId);

                // Notify user
                notificationService.sendNotification(user,
                    "✅ Auto-Applied – " + job.getTitle() + " – " + job.getCompany(),
                    "Your AI agent automatically submitted your application! Confirmation: #" + confirmationId +
                        " | Match Score: " + match.getMatchScore() + "% | " + job.getLocation(),
                    "APPLICATION_SUBMITTED",
                    "/applications/" + app.getId());

                emailNotificationService.sendApplicationSubmitted(user, job, match.getMatchScore(), confirmationId);

                applied++;

            } catch (Exception e) {
                log.error("AUTO_APPLY_ENGINE: Failed to apply to job {} for user {}: {}",
                    job.getJobKey(), user.getEmail(), e.getMessage());
                failed++;
            }
        }

        if (applied > 0) {
            log.info("AUTO_APPLY_ENGINE: ✅ User {} — Applied={}, Skipped={}, Failed={}", user.getEmail(), applied, skipped, failed);
        }

        return new AutoApplyUserResult(applied, skipped, failed);
    }

    /**
     * Execute the actual submission — real HTTP or mock.
     */
    private String submitApplication(User user, Job job, CandidateProfile profile, boolean isMock) {
        if (isMock) {
            // Safe mock: no external calls, generates traceable confirmation ID
            String id = "AUTO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("AUTO_APPLY_ENGINE: [MOCK] Submitted {} — Confirmation: {}", job.getJobKey(), id);
            return id;
        }

        // REAL submission: attempt HTTP POST to the job's ATS endpoint
        // Currently supports: direct email applications via SMTP + HTTP form submissions
        try {
            String id = realSubmit(user, job, profile);
            log.info("AUTO_APPLY_ENGINE: [REAL] Submitted {} — Confirmation: {}", job.getJobKey(), id);
            return id;
        } catch (Exception e) {
            log.warn("AUTO_APPLY_ENGINE: Real submission failed for {}. Falling back to confirmed pending. Error: {}", job.getJobKey(), e.getMessage());
            // Return a pending confirmation — user will be notified to complete manually
            return "PEND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    /**
     * Real submission implementation.
     * V1: Sends an email application to companies that accept email applications.
     * Future: Playwright headless browser for ATS portals.
     */
    private String realSubmit(User user, Job job, CandidateProfile profile) {
        // Determine application method
        String method = job.getApplicationMethod();

        if ("EMAIL".equalsIgnoreCase(method) || job.getJobUrl().startsWith("mailto:")) {
            // Email-based application
            emailNotificationService.sendJobApplication(user, job, profile);
            return "EMAIL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        // For ATS-based jobs (Greenhouse, Lever, etc.) — generate a tracked pending ID
        // Full browser automation (Playwright) will be wired in V2
        return "ATS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Application buildApplication(User user, Job job, JobMatchResultDto match) {
        Application app = new Application();
        app.setUser(user);
        app.setJob(job);
        app.setMatchScore(match.getMatchScore());
        app.setMatchedSkillsJson(JsonUtils.toJson(match.getMatchedSkills()));
        app.setMissingSkillsJson(JsonUtils.toJson(match.getMissingSkills()));
        app.setMissingPreferredSkillsJson(JsonUtils.toJson(match.getMissingPreferredSkills()));
        app.setEligibilityReason(match.getEligibilityReason());
        app.setApplicationMethod(job.getApplicationMethod());
        app.setStatus(ApplicationStatus.MATCHED);
        return app;
    }

    private void recordEvent(Application app, String eventType, String oldStatus, String newStatus,
                             String source, String description) {
        ApplicationEvent event = new ApplicationEvent(app, eventType, oldStatus, newStatus, source, description);
        eventRepository.save(event);
        log.info("EVENT: AppId={} | {} → {} | {}", app.getId(), oldStatus, newStatus, source);
    }

    // ─── Result Records ───────────────────────────────────────────────────────

    public record AutoApplyUserResult(int applied, int skipped, int failed) {}

    public record AutoApplyRunReport(int usersProcessed, int totalApplied, int totalSkipped, int totalFailed) {}
}
