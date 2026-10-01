package com.jobagent.scheduler;

import com.jobagent.service.AutoApplyEngine;
import com.jobagent.service.EmailMonitoringService;
import com.jobagent.service.JobDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Central scheduler for all autonomous agent tasks.
 *
 * Runs continuously in the background on Render:
 *   - Job discovery:  every 30 minutes
 *   - Auto-apply:     every 60 minutes (after initial 2-min warm-up)
 *   - Email polling:  every 15 minutes (when Gmail OAuth is connected)
 *   - Weekly digest:  every Monday 8 AM IST (configurable)
 */
@Component
public class JobAgentSchedulers {

    private static final Logger log = LoggerFactory.getLogger(JobAgentSchedulers.class);

    private final JobDiscoveryService jobDiscoveryService;
    private final EmailMonitoringService emailMonitoringService;
    private final AutoApplyEngine autoApplyEngine;

    @Value("${app.real-email-enabled:false}")
    private boolean realEmailEnabled;

    @Value("${app.auto-apply-enabled:false}")
    private boolean autoApplyEnabled;

    public JobAgentSchedulers(
        JobDiscoveryService jobDiscoveryService,
        EmailMonitoringService emailMonitoringService,
        AutoApplyEngine autoApplyEngine
    ) {
        this.jobDiscoveryService = jobDiscoveryService;
        this.emailMonitoringService = emailMonitoringService;
        this.autoApplyEngine = autoApplyEngine;
    }

    // ─── 1. Job Discovery — every 30 minutes ──────────────────────────────────
    @Scheduled(fixedDelay = 1_800_000, initialDelay = 60_000)
    public void scheduleJobDiscovery() {
        log.info("SCHEDULER: Initiating periodic job discovery for 2026 freshers in India");
        try {
            jobDiscoveryService.discoverJobs();
        } catch (Exception e) {
            log.error("SCHEDULER_ERROR: Job discovery cycle failed", e);
        }
    }

    // ─── 2. Autonomous Auto-Apply — every 60 minutes ──────────────────────────
    /**
     * This is the core autonomous engine.
     * Every hour it:
     *   - Finds all users with auto_apply_enabled = true
     *   - Matches all active jobs against their profile (85% threshold)
     *   - Automatically submits eligible applications
     *   - Sends email + in-app notifications
     *
     * Users who haven't uploaded a resume are skipped with a warning.
     * Daily application quota (default: 10/day) is respected per user.
     */
    @Scheduled(fixedDelay = 3_600_000, initialDelay = 120_000)
    public void scheduleAutoApply() {
        log.info("SCHEDULER: ⚡ Starting autonomous auto-apply cycle");
        try {
            AutoApplyEngine.AutoApplyRunReport report = autoApplyEngine.runForAllUsers();
            log.info("SCHEDULER: ✅ Auto-apply cycle complete — Users={}, Applied={}, Skipped={}, Failed={}",
                report.usersProcessed(), report.totalApplied(), report.totalSkipped(), report.totalFailed());
        } catch (Exception e) {
            log.error("SCHEDULER_ERROR: Auto-apply cycle failed", e);
        }
    }

    // ─── 3. Email Polling — every 15 minutes ──────────────────────────────────
    @Scheduled(fixedDelay = 900_000, initialDelay = 180_000)
    public void scheduleEmailPolling() {
        if (!realEmailEnabled) {
            return;
        }
        log.info("SCHEDULER: Polling Gmail OAuth connected mailboxes for employer event updates");
        try {
            emailMonitoringService.pollAllConnectedMailboxes();
        } catch (Exception e) {
            log.error("SCHEDULER_ERROR: Email polling cycle failed", e);
        }
    }
}
