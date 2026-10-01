package com.jobagent.service;

import com.jobagent.dto.EmailEventDto;
import com.jobagent.entity.*;
import com.jobagent.repository.ApplicationEventRepository;
import com.jobagent.repository.ApplicationRepository;
import com.jobagent.repository.EmailEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmailMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(EmailMonitoringService.class);

    private final EmailEventRepository emailEventRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationEventRepository eventRepository;
    private final EmailClassificationService classificationService;
    private final NotificationService notificationService;

    @Value("${app.real-email-enabled:false}")
    private boolean realEmailEnabled;

    public EmailMonitoringService(
        EmailEventRepository emailEventRepository,
        ApplicationRepository applicationRepository,
        ApplicationEventRepository eventRepository,
        EmailClassificationService classificationService,
        NotificationService notificationService
    ) {
        this.emailEventRepository = emailEventRepository;
        this.applicationRepository = applicationRepository;
        this.eventRepository = eventRepository;
        this.classificationService = classificationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public EmailEvent processIncomingEmail(User user, String sender, String subject, String body, String messageId) {
        if (messageId == null || messageId.isBlank()) {
            messageId = "MSG-" + UUID.randomUUID().toString();
        }

        if (emailEventRepository.existsByMessageId(messageId)) {
            log.info("SKIP_DUPLICATE: Email with message ID {} already processed", messageId);
            return emailEventRepository.findByMessageId(messageId).get();
        }

        log.info("EMAIL_RECEIVED: From: '{}', Subject: '{}'", sender, subject);

        EmailClassificationService.ClassificationResult classification = classificationService.classify(subject, body, sender);
        log.info("EMAIL_CLASSIFIED: JobRelated={}, Status={}, Confidence={}",
            classification.isJobRelated(), classification.detectedStatus(), classification.confidence());

        EmailEvent emailEvent = new EmailEvent();
        emailEvent.setUser(user);
        emailEvent.setMessageId(messageId);
        emailEvent.setSender(sender);
        emailEvent.setSubject(subject);
        emailEvent.setSnippet(body.length() > 300 ? body.substring(0, 300) + "..." : body);
        emailEvent.setDetectedStatus(classification.detectedStatus() != null ? classification.detectedStatus().name() : null);
        emailEvent.setConfidenceScore(classification.confidence());
        emailEvent.setMatchRationale(classification.rationale());
        emailEvent.setReceivedAt(LocalDateTime.now());
        emailEvent.setIsProcessed(false);

        if (!classification.isJobRelated()) {
            emailEvent.setIsProcessed(true);
            return emailEventRepository.save(emailEvent);
        }

        // Match incoming email to candidate applications
        List<Application> applications = applicationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        Application matchedApp = null;

        for (Application app : applications) {
            String company = app.getJob().getCompany().toLowerCase();
            String title = app.getJob().getTitle().toLowerCase();
            String confId = app.getConfirmationId() != null ? app.getConfirmationId().toLowerCase() : "";

            String lowerSubj = subject.toLowerCase();
            String lowerBody = body.toLowerCase();
            String lowerSender = sender.toLowerCase();

            boolean companyMatch = lowerSubj.contains(company) || lowerBody.contains(company) || lowerSender.contains(company);
            boolean titleMatch = lowerSubj.contains(title) || lowerBody.contains(title);
            boolean confMatch = !confId.isEmpty() && (lowerSubj.contains(confId) || lowerBody.contains(confId));

            if (confMatch || (companyMatch && titleMatch) || companyMatch) {
                matchedApp = app;
                break;
            }
        }

        if (matchedApp != null && classification.confidence() >= 0.80 && classification.detectedStatus() != null) {
            emailEvent.setApplication(matchedApp);
            emailEvent.setIsProcessed(true);
            emailEvent = emailEventRepository.save(emailEvent);

            // Update application status
            String oldStatus = matchedApp.getStatus().name();
            ApplicationStatus newStatus = classification.detectedStatus();
            matchedApp.setStatus(newStatus);
            matchedApp = applicationRepository.save(matchedApp);

            // Record timeline event
            ApplicationEvent appEvent = new ApplicationEvent(
                matchedApp,
                "EMAIL_EVENT_DETECTED",
                oldStatus,
                newStatus.name(),
                "REAL COMPANY/ATS EMAIL",
                "Email received from " + sender + ": " + subject
            );
            eventRepository.save(appEvent);

            log.info("APPLICATION_STATUS_CHANGED: AppId={} updated to {} based on verified employer email",
                matchedApp.getId(), newStatus);

            // Send notification
            notificationService.sendNotification(user,
                "Company Update – " + matchedApp.getJob().getTitle() + " – " + matchedApp.getJob().getCompany(),
                "Event: " + newStatus.name() + " (Source: REAL COMPANY/ATS EMAIL). Subject: " + subject,
                newStatus.name(),
                "/applications/" + matchedApp.getId());

        } else {
            emailEvent.setMatchRationale("EMAIL_MATCH_REVIEW_REQUIRED: Email identified as ATS update but application match confidence < 80%");
            emailEvent = emailEventRepository.save(emailEvent);
        }

        return emailEvent;
    }

    public List<EmailEventDto> getUserEmailEvents(Long userId) {
        return emailEventRepository.findByUserIdOrderByReceivedAtDesc(userId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public EmailEventDto toDto(EmailEvent e) {
        EmailEventDto dto = new EmailEventDto();
        dto.setId(e.getId());
        dto.setUserId(e.getUser().getId());
        dto.setMessageId(e.getMessageId());
        dto.setThreadId(e.getThreadId());
        dto.setSender(e.getSender());
        dto.setSubject(e.getSubject());
        dto.setSnippet(e.getSnippet());
        dto.setDetectedStatus(e.getDetectedStatus());
        dto.setConfidenceScore(e.getConfidenceScore());
        dto.setMatchRationale(e.getMatchRationale());
        dto.setReceivedAt(e.getReceivedAt());
        dto.setIsProcessed(e.getIsProcessed());

        if (e.getApplication() != null) {
            dto.setApplicationId(e.getApplication().getId());
            dto.setCompany(e.getApplication().getJob().getCompany());
            dto.setJobTitle(e.getApplication().getJob().getTitle());
        }
        return dto;
    }
}
