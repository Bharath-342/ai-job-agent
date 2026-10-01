package com.jobagent.dto;

import java.time.LocalDateTime;
import java.util.List;

public class JobDto {
    private Long id;
    private String jobKey;
    private String company;
    private String title;
    private String location;
    private String country;
    private String description;
    private String jobUrl;
    private String source;
    private LocalDateTime postedDate;
    private LocalDateTime closingDate;
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private Double minExperienceYears;
    private Double maxExperienceYears;
    private Integer targetGraduationYear;
    private String employmentType;
    private String applicationMethod;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public JobDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getJobKey() { return jobKey; }
    public void setJobKey(String jobKey) { this.jobKey = jobKey; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getJobUrl() { return jobUrl; }
    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDateTime postedDate) { this.postedDate = postedDate; }

    public LocalDateTime getClosingDate() { return closingDate; }
    public void setClosingDate(LocalDateTime closingDate) { this.closingDate = closingDate; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public List<String> getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(List<String> preferredSkills) { this.preferredSkills = preferredSkills; }

    public Double getMinExperienceYears() { return minExperienceYears; }
    public void setMinExperienceYears(Double minExperienceYears) { this.minExperienceYears = minExperienceYears; }

    public Double getMaxExperienceYears() { return maxExperienceYears; }
    public void setMaxExperienceYears(Double maxExperienceYears) { this.maxExperienceYears = maxExperienceYears; }

    public Integer getTargetGraduationYear() { return targetGraduationYear; }
    public void setTargetGraduationYear(Integer targetGraduationYear) { this.targetGraduationYear = targetGraduationYear; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getApplicationMethod() { return applicationMethod; }
    public void setApplicationMethod(String applicationMethod) { this.applicationMethod = applicationMethod; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
