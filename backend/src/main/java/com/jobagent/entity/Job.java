package com.jobagent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_key", nullable = false, unique = true)
    private String jobKey;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String country = "India";

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "job_url", nullable = false, columnDefinition = "TEXT")
    private String jobUrl;

    @Column(nullable = false)
    private String source;

    @Column(name = "posted_date")
    private LocalDateTime postedDate;

    @Column(name = "closing_date")
    private LocalDateTime closingDate;

    @Column(name = "required_skills_json", columnDefinition = "TEXT")
    private String requiredSkillsJson;

    @Column(name = "preferred_skills_json", columnDefinition = "TEXT")
    private String preferredSkillsJson;

    @Column(name = "min_experience_years")
    private Double minExperienceYears = 0.0;

    @Column(name = "max_experience_years")
    private Double maxExperienceYears = 1.0;

    @Column(name = "target_graduation_year")
    private Integer targetGraduationYear = 2026;

    @Column(name = "employment_type")
    private String employmentType = "FULL_TIME";

    @Column(name = "application_method")
    private String applicationMethod = "ATS_GREENHOUSE";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Job() {}

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

    public String getRequiredSkillsJson() { return requiredSkillsJson; }
    public void setRequiredSkillsJson(String requiredSkillsJson) { this.requiredSkillsJson = requiredSkillsJson; }

    public String getPreferredSkillsJson() { return preferredSkillsJson; }
    public void setPreferredSkillsJson(String preferredSkillsJson) { this.preferredSkillsJson = preferredSkillsJson; }

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
