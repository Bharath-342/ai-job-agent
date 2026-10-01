package com.jobagent.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CandidateProfileDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String education;
    private String degree;
    private Integer graduationYear;
    private Double experienceYears;
    private List<String> skills;
    private List<String> targetRoles;
    private List<String> preferredLocations;
    private List<String> projects;
    private List<String> experience;
    private List<String> certifications;
    private List<String> links;
    private String activeResumeFilename;
    private LocalDateTime updatedAt;

    public CandidateProfileDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public List<String> getTargetRoles() { return targetRoles; }
    public void setTargetRoles(List<String> targetRoles) { this.targetRoles = targetRoles; }

    public List<String> getPreferredLocations() { return preferredLocations; }
    public void setPreferredLocations(List<String> preferredLocations) { this.preferredLocations = preferredLocations; }

    public List<String> getProjects() { return projects; }
    public void setProjects(List<String> projects) { this.projects = projects; }

    public List<String> getExperience() { return experience; }
    public void setExperience(List<String> experience) { this.experience = experience; }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }

    public List<String> getLinks() { return links; }
    public void setLinks(List<String> links) { this.links = links; }

    public String getActiveResumeFilename() { return activeResumeFilename; }
    public void setActiveResumeFilename(String activeResumeFilename) { this.activeResumeFilename = activeResumeFilename; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
