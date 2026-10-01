package com.jobagent.scheduler;

import com.jobagent.service.EmailMonitoringService;
import com.jobagent.service.JobDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobAgentSchedulers {

    private static final Logger log = LoggerFactory.getLogger(JobAgentSchedulers.class);

    private final JobDiscoveryService jobDiscoveryService;
    private final EmailMonitoringService emailMonitoringService;

    @Value("${app.real-email-enabled:false}")
    private boolean realEmailEnabled;

    public JobAgentSchedulers(JobDiscoveryService jobDiscoveryService, EmailMonitoringService emailMonitoringService) {
        this.jobDiscoveryService = jobDiscoveryService;
        this.emailMonitoringService = emailMonitoringService;
    }

    // Run discovery check every 30 minutes (1,800,000 ms)
    @Scheduled(fixedDelay = 1800000, initialDelay = 60000)
    public void scheduleJobDiscovery() {
        log.info("SCHEDULER: Initiating periodic job discovery for 2026 freshers in India");
        try {
            jobDiscoveryService.discoverJobs();
        } catch (Exception e) {
            log.error("SCHEDULER_ERROR: Job discovery cycle failed", e);
        }
    }

    // Run email polling check every 15 minutes (900,000 ms)
    @Scheduled(fixedDelay = 900000, initialDelay = 120000)
    public void scheduleEmailPolling() {
        if (!realEmailEnabled) {
            return;
        }
        log.info("SCHEDULER: Polling Gmail OAuth connected mailboxes for employer event updates");
    }
}
