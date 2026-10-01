package com.jobagent.service;

import com.jobagent.dto.ApplicationEventDto;
import com.jobagent.dto.DashboardDto;
import com.jobagent.entity.ApplicationStatus;
import com.jobagent.entity.UserSettings;
import com.jobagent.repository.ApplicationEventRepository;
import com.jobagent.repository.ApplicationRepository;
import com.jobagent.repository.JobRepository;
import com.jobagent.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationEventRepository eventRepository;
    private final UserSettingsRepository settingsRepository;

    public DashboardService(
        JobRepository jobRepository,
        ApplicationRepository applicationRepository,
        ApplicationEventRepository eventRepository,
        UserSettingsRepository settingsRepository
    ) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.eventRepository = eventRepository;
        this.settingsRepository = settingsRepository;
    }

    public DashboardDto getDashboardStats(Long userId) {
        DashboardDto dto = new DashboardDto();

        long totalJobs = jobRepository.count();
        dto.setJobsDiscovered(totalJobs);

        // Discovered eligible jobs in India for 2026 batch
        long eligibleJobsCount = jobRepository.findEligibleJobsForFresher(2026, 1.0).size();
        dto.setEligibleJobs(eligibleJobsCount);

        // Status counts for user
        long submitted = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.APPLIED);
        long appReceived = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.APPLICATION_RECEIVED);
        dto.setApplicationsSubmitted(submitted + appReceived);

        dto.setManualActionsRequired(
            applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.MANUAL_ACTION_REQUIRED) +
            applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.MANUAL_ELIGIBILITY_REVIEW)
        );

        dto.setUnderReview(applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.UNDER_REVIEW));
        dto.setAssessments(applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.ASSESSMENT_RECEIVED));
        dto.setInterviews(
            applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.INTERVIEW_INVITATION) +
            applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.INTERVIEW_SCHEDULED)
        );
        dto.setRejections(applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.REJECTED_BY_COMPANY));
        dto.setOffers(applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.OFFER_RECEIVED));

        // Daily Limit & Quota
        UserSettings settings = settingsRepository.findByUserId(userId).orElseGet(UserSettings::new);
        int limit = settings.getDailyApplicationLimit() != null ? settings.getDailyApplicationLimit() : 10;
        dto.setDailyLimit(limit);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long appliedToday = applicationRepository.countSubmittedSince(userId, startOfDay);
        dto.setApplicationsToday(appliedToday);
        dto.setRemainingQuota(Math.max(0, limit - appliedToday));

        // Recent events
        List<ApplicationEventDto> recentEvents = eventRepository.findTop50ByOrderByEventTimeDesc()
            .stream()
            .filter(e -> e.getApplication().getUser().getId().equals(userId))
            .limit(10)
            .map(e -> {
                ApplicationEventDto edto = new ApplicationEventDto();
                edto.setId(e.getId());
                edto.setApplicationId(e.getApplication().getId());
                edto.setEventType(e.getEventType());
                edto.setOldStatus(e.getOldStatus());
                edto.setNewStatus(e.getNewStatus());
                edto.setSource(e.getSource());
                edto.setDescription(e.getDescription());
                edto.setEventTime(e.getEventTime());
                return edto;
            })
            .collect(Collectors.toList());
        dto.setRecentEvents(recentEvents);

        return dto;
    }
}
