package com.jobagent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_profiles")
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(columnDefinition = "TEXT")
    private String education;

    private String degree;

    @Column(name = "graduation_year")
    private Integer graduationYear = 2026;

    @Column(name = "experience_years")
    private Double experienceYears = 0.0;

    @Column(name = "skills_json", columnDefinition = "TEXT")
    private String skillsJson; // JSON array of skills or categorized map

    @Column(name = "target_roles_json", columnDefinition = "TEXT")
    private String targetRolesJson;

    @Column(name = "preferred_locations_json", columnDefinition = "TEXT")
    private String preferredLocationsJson;

    @Column(name = "projects_json", columnDefinition = "TEXT")
    private String projectsJson;

    @Column(name = "experience_json", columnDefinition = "TEXT")
    private String experienceJson;

    @Column(name = "certifications_json", columnDefinition = "TEXT")
    private String certificationsJson;

    @Column(name = "links_json", columnDefinition = "TEXT")
    private String linksJson;

    @Column(name = "active_resume_filename")
    private String activeResumeFilename;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public CandidateProfile() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public Double getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Double experienceYears) { this.experienceYears = experienceYears; }

    public String getSkillsJson() { return skillsJson; }
    public void setSkillsJson(String skillsJson) { this.skillsJson = skillsJson; }

    public String getTargetRolesJson() { return targetRolesJson; }
    public void setTargetRolesJson(String targetRolesJson) { this.targetRolesJson = targetRolesJson; }

    public String getPreferredLocationsJson() { return preferredLocationsJson; }
    public void setPreferredLocationsJson(String preferredLocationsJson) { this.preferredLocationsJson = preferredLocationsJson; }

    public String getProjectsJson() { return projectsJson; }
    public void setProjectsJson(String projectsJson) { this.projectsJson = projectsJson; }

    public String getExperienceJson() { return experienceJson; }
    public void setExperienceJson(String experienceJson) { this.experienceJson = experienceJson; }

    public String getCertificationsJson() { return certificationsJson; }
    public void setCertificationsJson(String certificationsJson) { this.certificationsJson = certificationsJson; }

    public String getLinksJson() { return linksJson; }
    public void setLinksJson(String linksJson) { this.linksJson = linksJson; }

    public String getActiveResumeFilename() { return activeResumeFilename; }
    public void setActiveResumeFilename(String activeResumeFilename) { this.activeResumeFilename = activeResumeFilename; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
