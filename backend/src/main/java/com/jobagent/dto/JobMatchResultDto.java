package com.jobagent.dto;

import java.util.List;

public class JobMatchResultDto {
    private Long jobId;
    private String jobKey;
    private String company;
    private String title;
    private String location;
    private String jobUrl;
    private String applicationMethod;
    private int matchScore; // 0 - 100
    private boolean eligible;
    private boolean manualReviewRequired;
    private String eligibilityReason;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> missingPreferredSkills;
    private boolean roleMatch;
    private boolean locationMatch;
    private boolean experienceMatch;
    private boolean graduationMatch;

    public JobMatchResultDto() {}

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getJobKey() { return jobKey; }
    public void setJobKey(String jobKey) { this.jobKey = jobKey; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getJobUrl() { return jobUrl; }
    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }

    public String getApplicationMethod() { return applicationMethod; }
    public void setApplicationMethod(String applicationMethod) { this.applicationMethod = applicationMethod; }

    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }

    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }

    public boolean isManualReviewRequired() { return manualReviewRequired; }
    public void setManualReviewRequired(boolean manualReviewRequired) { this.manualReviewRequired = manualReviewRequired; }

    public String getEligibilityReason() { return eligibilityReason; }
    public void setEligibilityReason(String eligibilityReason) { this.eligibilityReason = eligibilityReason; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getMissingPreferredSkills() { return missingPreferredSkills; }
    public void setMissingPreferredSkills(List<String> missingPreferredSkills) { this.missingPreferredSkills = missingPreferredSkills; }

    public boolean isRoleMatch() { return roleMatch; }
    public void setRoleMatch(boolean roleMatch) { this.roleMatch = roleMatch; }

    public boolean isLocationMatch() { return locationMatch; }
    public void setLocationMatch(boolean locationMatch) { this.locationMatch = locationMatch; }

    public boolean isExperienceMatch() { return experienceMatch; }
    public void setExperienceMatch(boolean experienceMatch) { this.experienceMatch = experienceMatch; }

    public boolean isGraduationMatch() { return graduationMatch; }
    public void setGraduationMatch(boolean graduationMatch) { this.graduationMatch = graduationMatch; }
}
