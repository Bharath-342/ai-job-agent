package com.jobagent.service;

import com.jobagent.config.JsonUtils;
import com.jobagent.dto.ApplicationDto;
import com.jobagent.dto.ApplicationEventDto;
import com.jobagent.dto.ManualActionResolutionDto;
import com.jobagent.entity.Application;
import com.jobagent.entity.ApplicationEvent;
import com.jobagent.entity.ApplicationStatus;
import com.jobagent.entity.User;
import com.jobagent.exception.ResourceNotFoundException;
import com.jobagent.repository.ApplicationEventRepository;
import com.jobagent.repository.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationTrackingService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationTrackingService.class);

    private final ApplicationRepository applicationRepository;
    private final ApplicationEventRepository eventRepository;
    private final NotificationService notificationService;

    public ApplicationTrackingService(
        ApplicationRepository applicationRepository,
        ApplicationEventRepository eventRepository,
        NotificationService notificationService
    ) {
        this.applicationRepository = applicationRepository;
        this.eventRepository = eventRepository;
        this.notificationService = notificationService;
    }

    public List<ApplicationDto> getUserApplications(Long userId) {
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public ApplicationDto getApplicationById(Long id, Long userId) {
        Application app = applicationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));

        if (!app.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Application not accessible");
        }

        ApplicationDto dto = toDto(app);
        List<ApplicationEventDto> events = eventRepository.findByApplicationIdOrderByEventTimeAsc(app.getId())
            .stream()
            .map(this::toEventDto)
            .collect(Collectors.toList());
        dto.setEvents(events);
        return dto;
    }

    @Transactional
    public ApplicationDto resolveManualAction(Long applicationId, User user, ManualActionResolutionDto resolution) {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!app.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Application not accessible");
        }

        String oldStatus = app.getStatus().name();
        ApplicationStatus newStatus = resolution.getResolutionStatus() != null ? resolution.getResolutionStatus() : ApplicationStatus.APPLIED;
        app.setStatus(newStatus);
        if (resolution.getConfirmationId() != null && !resolution.getConfirmationId().isBlank()) {
            app.setConfirmationId(resolution.getConfirmationId());
        }
        if (resolution.getUserNotes() != null) {
            app.setNotes((app.getNotes() != null ? app.getNotes() + "\n" : "") + "User Note: " + resolution.getUserNotes());
        }
        if (newStatus == ApplicationStatus.APPLIED) {
            app.setSubmittedAt(LocalDateTime.now());
        }
        app = applicationRepository.save(app);

        ApplicationEvent event = new ApplicationEvent(
            app,
            "MANUAL_ACTION_RESOLVED",
            oldStatus,
            newStatus.name(),
            "USER_HANDOFF",
            "User resolved manual action and marked status as " + newStatus.name()
        );
        eventRepository.save(event);

        log.info("APPLICATION_STATUS_CHANGED: User {} resolved manual action on AppId={}, Status: {} -> {}",
            user.getEmail(), app.getId(), oldStatus, newStatus);

        notificationService.sendNotification(user,
            "Application Updated – " + app.getJob().getTitle() + " – " + app.getJob().getCompany(),
            "Status updated to " + newStatus.name() + " via manual completion.",
            newStatus.name(),
            "/applications/" + app.getId());

        return getApplicationById(app.getId(), user.getId());
    }

    @Transactional
    public void updateApplicationStatusFromExternal(Application app, ApplicationStatus newStatus, String source, String description) {
        String oldStatus = app.getStatus().name();
        app.setStatus(newStatus);
        app = applicationRepository.save(app);

        ApplicationEvent event = new ApplicationEvent(app, "STATUS_UPDATE", oldStatus, newStatus.name(), source, description);
        eventRepository.save(event);

        log.info("APPLICATION_STATUS_CHANGED: AppId={}, Old={}, New={}, Source={}", app.getId(), oldStatus, newStatus, source);

        notificationService.sendNotification(app.getUser(),
            "Company Update – " + app.getJob().getTitle() + " – " + app.getJob().getCompany(),
            "Event: " + newStatus.name() + " (Source: " + source + "). " + (description != null ? description : ""),
            newStatus.name(),
            "/applications/" + app.getId());
    }

    public ApplicationDto toDto(Application app) {
        ApplicationDto dto = new ApplicationDto();
        dto.setId(app.getId());
        dto.setUserId(app.getUser().getId());
        dto.setJobId(app.getJob().getId());
        dto.setCompany(app.getJob().getCompany());
        dto.setTitle(app.getJob().getTitle());
        dto.setLocation(app.getJob().getLocation());
        dto.setJobUrl(app.getJob().getJobUrl());
        dto.setJobKey(app.getJob().getJobKey());
        dto.setSource(app.getJob().getSource());
        dto.setStatus(app.getStatus());
        dto.setMatchScore(app.getMatchScore());
        dto.setMatchedSkills(JsonUtils.toStringList(app.getMatchedSkillsJson()));
        dto.setMissingSkills(JsonUtils.toStringList(app.getMissingSkillsJson()));
        dto.setMissingPreferredSkills(JsonUtils.toStringList(app.getMissingPreferredSkillsJson()));
        dto.setEligibilityReason(app.getEligibilityReason());
        dto.setApplicationMethod(app.getApplicationMethod());
        dto.setConfirmationId(app.getConfirmationId());
        dto.setFailureReason(app.getFailureReason());
        dto.setManualActionReason(app.getManualActionReason());
        dto.setNotes(app.getNotes());
        dto.setSubmittedAt(app.getSubmittedAt());
        dto.setLastStatusChangeAt(app.getLastStatusChangeAt());
        dto.setCreatedAt(app.getCreatedAt());
        return dto;
    }

    public ApplicationEventDto toEventDto(ApplicationEvent event) {
        ApplicationEventDto dto = new ApplicationEventDto();
        dto.setId(event.getId());
        dto.setApplicationId(event.getApplication().getId());
        dto.setEventType(event.getEventType());
        dto.setOldStatus(event.getOldStatus());
        dto.setNewStatus(event.getNewStatus());
        dto.setSource(event.getSource());
        dto.setDescription(event.getDescription());
        dto.setEventTime(event.getEventTime());
        return dto;
    }
}
