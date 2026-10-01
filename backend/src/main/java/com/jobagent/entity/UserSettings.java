package com.jobagent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "daily_application_limit")
    private Integer dailyApplicationLimit = 10;

    @Column(name = "min_match_score")
    private Integer minMatchScore = 85;

    @Column(name = "auto_apply_enabled")
    private Boolean autoApplyEnabled = false;

    @Column(name = "mock_mode")
    private Boolean mockMode = true;

    @Column(name = "real_email_enabled")
    private Boolean realEmailEnabled = false;

    @Column(name = "target_graduation_year")
    private Integer targetGraduationYear = 2026;

    @Column(name = "max_experience_years")
    private Double maxExperienceYears = 1.0;

    @Column(name = "target_roles_json", columnDefinition = "TEXT")
    private String targetRolesJson;

    @Column(name = "excluded_roles_json", columnDefinition = "TEXT")
    private String excludedRolesJson;

    @Column(name = "preferred_technologies_json", columnDefinition = "TEXT")
    private String preferredTechnologiesJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public UserSettings() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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

    public String getTargetRolesJson() { return targetRolesJson; }
    public void setTargetRolesJson(String targetRolesJson) { this.targetRolesJson = targetRolesJson; }

    public String getExcludedRolesJson() { return excludedRolesJson; }
    public void setExcludedRolesJson(String excludedRolesJson) { this.excludedRolesJson = excludedRolesJson; }

    public String getPreferredTechnologiesJson() { return preferredTechnologiesJson; }
    public void setPreferredTechnologiesJson(String preferredTechnologiesJson) { this.preferredTechnologiesJson = preferredTechnologiesJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
