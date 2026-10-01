package com.jobagent.dto;

import java.util.List;

public class DashboardDto {
    private long jobsDiscovered;
    private long eligibleJobs;
    private long applicationsSubmitted;
    private long manualActionsRequired;
    private long underReview;
    private long assessments;
    private long interviews;
    private long rejections;
    private long offers;
    private int dailyLimit;
    private long applicationsToday;
    private long remainingQuota;
    private List<ApplicationEventDto> recentEvents;

    public DashboardDto() {}

    public long getJobsDiscovered() { return jobsDiscovered; }
    public void setJobsDiscovered(long jobsDiscovered) { this.jobsDiscovered = jobsDiscovered; }

    public long getEligibleJobs() { return eligibleJobs; }
    public void setEligibleJobs(long eligibleJobs) { this.eligibleJobs = eligibleJobs; }

    public long getApplicationsSubmitted() { return applicationsSubmitted; }
    public void setApplicationsSubmitted(long applicationsSubmitted) { this.applicationsSubmitted = applicationsSubmitted; }

    public long getManualActionsRequired() { return manualActionsRequired; }
    public void setManualActionsRequired(long manualActionsRequired) { this.manualActionsRequired = manualActionsRequired; }

    public long getUnderReview() { return underReview; }
    public void setUnderReview(long underReview) { this.underReview = underReview; }

    public long getAssessments() { return assessments; }
    public void setAssessments(long assessments) { this.assessments = assessments; }

    public long getInterviews() { return interviews; }
    public void setInterviews(long interviews) { this.interviews = interviews; }

    public long getRejections() { return rejections; }
    public void setRejections(long rejections) { this.rejections = rejections; }

    public long getOffers() { return offers; }
    public void setOffers(long offers) { this.offers = offers; }

    public int getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(int dailyLimit) { this.dailyLimit = dailyLimit; }

    public long getApplicationsToday() { return applicationsToday; }
    public void setApplicationsToday(long applicationsToday) { this.applicationsToday = applicationsToday; }

    public long getRemainingQuota() { return remainingQuota; }
    public void setRemainingQuota(long remainingQuota) { this.remainingQuota = remainingQuota; }

    public List<ApplicationEventDto> getRecentEvents() { return recentEvents; }
    public void setRecentEvents(List<ApplicationEventDto> recentEvents) { this.recentEvents = recentEvents; }
}
