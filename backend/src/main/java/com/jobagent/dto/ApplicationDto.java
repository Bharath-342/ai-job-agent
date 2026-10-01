package com.jobagent.dto;

import com.jobagent.entity.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ApplicationDto {
    private Long id;
    private Long userId;
    private Long jobId;
    private String company;
    private String title;
    private String location;
    private String jobUrl;
    private String jobKey;
    private String source;
    private ApplicationStatus status;
    private Integer matchScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> missingPreferredSkills;
    private String eligibilityReason;
    private String applicationMethod;
    private String confirmationId;
    private String failureReason;
    private String manualActionReason;
    private String notes;
    private LocalDateTime submittedAt;
    private LocalDateTime lastStatusChangeAt;
    private LocalDateTime createdAt;
    private List<ApplicationEventDto> events;

    public ApplicationDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getJobUrl() { return jobUrl; }
    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }

    public String getJobKey() { return jobKey; }
    public void setJobKey(String jobKey) { this.jobKey = jobKey; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public Integer getMatchScore() { return matchScore; }
    public void setMatchScore(Integer matchScore) { this.matchScore = matchScore; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getMissingPreferredSkills() { return missingPreferredSkills; }
    public void setMissingPreferredSkills(List<String> missingPreferredSkills) { this.missingPreferredSkills = missingPreferredSkills; }

    public String getEligibilityReason() { return eligibilityReason; }
    public void setEligibilityReason(String eligibilityReason) { this.eligibilityReason = eligibilityReason; }

    public String getApplicationMethod() { return applicationMethod; }
    public void setApplicationMethod(String applicationMethod) { this.applicationMethod = applicationMethod; }

    public String getConfirmationId() { return confirmationId; }
    public void setConfirmationId(String confirmationId) { this.confirmationId = confirmationId; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getManualActionReason() { return manualActionReason; }
    public void setManualActionReason(String manualActionReason) { this.manualActionReason = manualActionReason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getLastStatusChangeAt() { return lastStatusChangeAt; }
    public void setLastStatusChangeAt(LocalDateTime lastStatusChangeAt) { this.lastStatusChangeAt = lastStatusChangeAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ApplicationEventDto> getEvents() { return events; }
    public void setEvents(List<ApplicationEventDto> events) { this.events = events; }
}
