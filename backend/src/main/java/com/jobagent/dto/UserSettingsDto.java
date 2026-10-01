package com.jobagent.dto;

import java.util.List;

public class UserSettingsDto {
    private Long id;
    private Integer dailyApplicationLimit;
    private Integer minMatchScore;
    private Boolean autoApplyEnabled;
    private Boolean mockMode;
    private Boolean realEmailEnabled;
    private Integer targetGraduationYear;
    private Double maxExperienceYears;
    private List<String> targetRoles;
    private List<String> excludedRoles;
    private List<String> preferredTechnologies;

    public UserSettingsDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getDailyApplicationLimit() { return dailyApplicationLimit; }
    public void setDailyApplicationLimit(Integer dailyApplicationLimit) { this.dailyApplicationLimit = dailyApplicationLimit; }

    public Integer getMinMatchScore() { return minMatchScore; }
    public void setMinMatchScore(Integer minMatchScore) { this.minMatchScore = minMatchScore; }

    public Boolean getAutoApplyEnabled() { return autoApplyEnabled; }
    public void setAutoApplyEnabled(Boolean autoApplyEnabled) { this.autoApplyEnabled = autoApplyEnabled; }

    public Boolean getMockMode() { return mockMode; }
    public void setMockMode(Boolean mockMode) { this.mockMode = mockMode; }

    public Boolean getRealEmailEnabled() { return realEmailEnabled; }
    public void setRealEmailEnabled(Boolean realEmailEnabled) { this.realEmailEnabled = realEmailEnabled; }

    public Integer getTargetGraduationYear() { return targetGraduationYear; }
    public void setTargetGraduationYear(Integer targetGraduationYear) { this.targetGraduationYear = targetGraduationYear; }

    public Double getMaxExperienceYears() { return maxExperienceYears; }
    public void setMaxExperienceYears(Double maxExperienceYears) { this.maxExperienceYears = maxExperienceYears; }

    public List<String> getTargetRoles() { return targetRoles; }
    public void setTargetRoles(List<String> targetRoles) { this.targetRoles = targetRoles; }

    public List<String> getExcludedRoles() { return excludedRoles; }
    public void setExcludedRoles(List<String> excludedRoles) { this.excludedRoles = excludedRoles; }

    public List<String> getPreferredTechnologies() { return preferredTechnologies; }
    public void setPreferredTechnologies(List<String> preferredTechnologies) { this.preferredTechnologies = preferredTechnologies; }
}
