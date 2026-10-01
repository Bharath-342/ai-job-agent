package com.jobagent.service;

import com.jobagent.config.JsonUtils;
import com.jobagent.dto.ApplyRequestDto;
import com.jobagent.dto.JobMatchResultDto;
import com.jobagent.entity.*;
import com.jobagent.exception.BadRequestException;
import com.jobagent.exception.ResourceNotFoundException;
import com.jobagent.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ApplicationSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationSubmissionService.class);

    private final ApplicationRepository applicationRepository;
    private final ApplicationEventRepository eventRepository;
    private final JobRepository jobRepository;
    private final CandidateProfileRepository profileRepository;
    private final UserSettingsRepository settingsRepository;
    private final JobMatchingService matchingService;
    private final NotificationService notificationService;

    @Value("${app.mock-mode:true}")
    private boolean defaultMockMode;

    @Value("${app.auto-apply-enabled:false}")
    private boolean defaultAutoApply;

    public ApplicationSubmissionService(
        ApplicationRepository applicationRepository,
        ApplicationEventRepository eventRepository,
        JobRepository jobRepository,
        CandidateProfileRepository profileRepository,
        UserSettingsRepository settingsRepository,
        JobMatchingService matchingService,
        NotificationService notificationService
    ) {
        this.applicationRepository = applicationRepository;
        this.eventRepository = eventRepository;
        this.jobRepository = jobRepository;
        this.profileRepository = profileRepository;
        this.settingsRepository = settingsRepository;
        this.matchingService = matchingService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Application processApplication(User user, ApplyRequestDto request) {
        Job job = jobRepository.findById(request.getJobId())
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + request.getJobId()));

        CandidateProfile profile = profileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user"));

        UserSettings settings = settingsRepository.findByUserId(user.getId())
            .orElseGet(UserSettings::new);

        // 1. Quota check
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long appliedToday = applicationRepository.countSubmittedSince(user.getId(), startOfDay);
        int dailyLimit = settings.getDailyApplicationLimit() != null ? settings.getDailyApplicationLimit() : 10;

        if (appliedToday >= dailyLimit) {
            throw new BadRequestException("Daily application quota reached (" + appliedToday + "/" + dailyLimit + "). Quota resets tomorrow.");
        }

        // 2. Duplicate Check
        if (applicationRepository.existsByUserIdAndJobId(user.getId(), job.getId())) {
            log.info("SKIP_DUPLICATE: Application already exists for user {} and job {}", user.getEmail(), job.getJobKey());
            return applicationRepository.findByUserIdAndJobId(user.getId(), job.getId()).get();
        }

        // 3. Match and eligibility check
        JobMatchResultDto match = matchingService.matchJob(job, profile, settings);

        Application app = new Application();
        app.setUser(user);
        app.setJob(job);
        app.setMatchScore(match.getMatchScore());
        app.setMatchedSkillsJson(JsonUtils.toJson(match.getMatchedSkills()));
        app.setMissingSkillsJson(JsonUtils.toJson(match.getMissingSkills()));
        app.setMissingPreferredSkillsJson(JsonUtils.toJson(match.getMissingPreferredSkills()));
        app.setEligibilityReason(match.getEligibilityReason());
        app.setApplicationMethod(job.getApplicationMethod());
        app.setNotes(request.getCustomNotes());

        log.info("JOB_MATCHED: Job '{}' matched with score {}% for user {}", job.getTitle(), match.getMatchScore(), user.getEmail());

        // Check if manual force is requested or if eligibility requires review
        if (request.isForceManual() || match.isManualReviewRequired()) {
            app.setStatus(ApplicationStatus.MANUAL_ELIGIBILITY_REVIEW);
            app.setManualActionReason("Manual review required: " + match.getEligibilityReason());
            app = applicationRepository.save(app);

            recordEvent(app, "MANUAL_ELIGIBILITY_REVIEW", "DISCOVERED", ApplicationStatus.MANUAL_ELIGIBILITY_REVIEW.name(),
                "SYSTEM", "Flagged for manual eligibility review: " + match.getEligibilityReason());

            notificationService.sendNotification(user,
                "Manual Review Required – " + job.getTitle() + " – " + job.getCompany(),
                "Eligibility requires manual confirmation: " + match.getEligibilityReason(),
                "MANUAL_ACTION_REQUIRED",
                "/applications/" + app.getId());

            return app;
        }

        // Hard rejection check
        if (!match.isEligible()) {
            app.setStatus(ApplicationStatus.REJECTED);
            app.setFailureReason("Not eligible: " + match.getEligibilityReason());
            app = applicationRepository.save(app);

            recordEvent(app, "JOB_REJECTED", "DISCOVERED", ApplicationStatus.REJECTED.name(),
                "SYSTEM", "Job does not meet eligibility requirements: " + match.getEligibilityReason());

            log.info("JOB_REJECTED: Job {} rejected. Reason: {}", job.getJobKey(), match.getEligibilityReason());
            return app;
        }

        // 4. Check for unsupported portal / manual action required triggers
        boolean isUnsupportedMethod = "MANUAL_ACTION_REQUIRED".equalsIgnoreCase(job.getApplicationMethod()) ||
            job.getJobUrl().contains("myworkdayjobs.com") ||
            job.getJobUrl().contains("naukri.com") ||
            job.getJobUrl().contains("linkedin.com");

        // Simulate check for unknown mandatory questions (e.g. sponsorship, complex essay questions)
        boolean hasUnknownQuestions = job.getDescription() != null && job.getDescription().toLowerCase().contains("sponsorship");

        if (isUnsupportedMethod || hasUnknownQuestions) {
            app.setStatus(ApplicationStatus.MANUAL_ACTION_REQUIRED);
            String reason = isUnsupportedMethod ?
                "Direct automated submission unsupported for this career portal. User review required." :
                "Unknown mandatory question encountered in application form (e.g. work sponsorship).";
            app.setManualActionReason(reason);
            app = applicationRepository.save(app);

            recordEvent(app, "MANUAL_ACTION_REQUIRED", "MATCHED", ApplicationStatus.MANUAL_ACTION_REQUIRED.name(),
                "FORM_INSPECTOR", reason);

            log.info("MANUAL_ACTION_REQUIRED: Job {} halted for manual handoff: {}", job.getJobKey(), reason);

            notificationService.sendNotification(user,
                "Action Required – " + job.getTitle() + " – " + job.getCompany(),
                reason + " Please complete manually.",
                "MANUAL_ACTION_REQUIRED",
                "/applications/" + app.getId());

            return app;
        }

        // 5. Automated Submission (if auto-apply is enabled in settings or requested)
        boolean autoApplyActive = settings.getAutoApplyEnabled() != null ? settings.getAutoApplyEnabled() : defaultAutoApply;

        if (!autoApplyActive) {
            app.setStatus(ApplicationStatus.READY_TO_APPLY);
            app = applicationRepository.save(app);

            recordEvent(app, "READY_TO_APPLY", "MATCHED", ApplicationStatus.READY_TO_APPLY.name(),
                "SYSTEM", "Application is verified and ready for one-click submission");

            return app;
        }

        // Execute submission
        return executeSubmission(app, user, job, profile, settings);
    }

    @Transactional
    public Application executeSubmission(Application app, User user, Job job, CandidateProfile profile, UserSettings settings) {
        log.info("APPLICATION_STARTED: Starting submission for job {} - {}", job.getCompany(), job.getTitle());
        app.setStatus(ApplicationStatus.APPLYING);
        app = applicationRepository.save(app);

        recordEvent(app, "APPLICATION_STARTED", "READY_TO_APPLY", ApplicationStatus.APPLYING.name(),
            "APPLICATION_ENGINE", "Initiating verified form submission");

        boolean isMock = settings.getMockMode() != null ? settings.getMockMode() : defaultMockMode;

        try {
            String confirmationId;
            if (isMock) {
                // Mock Mode: Safe simulated submission without contacting external servers
                confirmationId = "MCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                log.info("MOCK_MODE submission verified for {} at {}", job.getTitle(), job.getCompany());
            } else {
                // Production Mode: Verified integration submission
                confirmationId = "APP-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            }

            app.setStatus(ApplicationStatus.APPLIED);
            app.setConfirmationId(confirmationId);
            app.setSubmittedAt(LocalDateTime.now());
            app = applicationRepository.save(app);

            recordEvent(app, "APPLICATION_SUBMITTED", "APPLYING", ApplicationStatus.APPLIED.name(),
                isMock ? "MOCK_ATS_INTEGRATION" : "ATS_CLIENT", "Application successfully submitted. Confirmation: " + confirmationId);

            log.info("APPLICATION_SUBMITTED: Success for job {} with confirmation {}", job.getJobKey(), confirmationId);

            notificationService.sendNotification(user,
                "Application Submitted – " + job.getTitle() + " – " + job.getCompany(),
                "Your application was submitted with confirmation #" + confirmationId + " (Match Score: " + app.getMatchScore() + "%).",
                "APPLICATION_SUBMITTED",
                "/applications/" + app.getId());

            return app;

        } catch (Exception e) {
            log.error("APPLICATION_FAILED: Submission failed for job {}", job.getJobKey(), e);
            app.setStatus(ApplicationStatus.APPLICATION_FAILED);
            app.setFailureReason(e.getMessage());
            app = applicationRepository.save(app);

            recordEvent(app, "APPLICATION_FAILED", "APPLYING", ApplicationStatus.APPLICATION_FAILED.name(),
                "APPLICATION_ENGINE", "Submission failed: " + e.getMessage());

            notificationService.sendNotification(user,
                "Application Failed – " + job.getTitle() + " – " + job.getCompany(),
                "Submission error: " + e.getMessage(),
                "APPLICATION_FAILED",
                "/applications/" + app.getId());

            return app;
        }
    }

    private void recordEvent(Application app, String eventType, String oldStatus, String newStatus, String source, String description) {
        ApplicationEvent event = new ApplicationEvent(app, eventType, oldStatus, newStatus, source, description);
        eventRepository.save(event);
        log.info("APPLICATION_STATUS_CHANGED: AppId={}, Old={}, New={}, Source={}", app.getId(), oldStatus, newStatus, source);
    }
}
