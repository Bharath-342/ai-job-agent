package com.jobagent.service;

import com.jobagent.config.JsonUtils;
import com.jobagent.dto.CandidateProfileDto;
import com.jobagent.entity.CandidateProfile;
import com.jobagent.entity.User;
import com.jobagent.exception.ResourceNotFoundException;
import com.jobagent.repository.CandidateProfileRepository;
import com.jobagent.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

@Service
public class CandidateProfileService {

    private final CandidateProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ResumeParsingService resumeParsingService;

    public CandidateProfileService(
        CandidateProfileRepository profileRepository,
        UserRepository userRepository,
        ResumeParsingService resumeParsingService
    ) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.resumeParsingService = resumeParsingService;
    }

    public CandidateProfileDto getProfileByUserId(Long userId) {
        CandidateProfile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + userId));

        return toDto(profile);
    }

    @Transactional
    public CandidateProfileDto updateProfile(Long userId, CandidateProfileDto dto) {
        CandidateProfile profile = profileRepository.findByUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
                CandidateProfile p = new CandidateProfile();
                p.setUser(user);
                return p;
            });

        if (dto.getFullName() != null) profile.setFullName(dto.getFullName());
        if (dto.getEmail() != null) profile.setEmail(dto.getEmail());
        if (dto.getPhone() != null) profile.setPhone(dto.getPhone());
        if (dto.getEducation() != null) profile.setEducation(dto.getEducation());
        if (dto.getDegree() != null) profile.setDegree(dto.getDegree());
        if (dto.getGraduationYear() != null) profile.setGraduationYear(dto.getGraduationYear());
        if (dto.getExperienceYears() != null) profile.setExperienceYears(dto.getExperienceYears());

        if (dto.getSkills() != null) profile.setSkillsJson(JsonUtils.toJson(dto.getSkills()));
        if (dto.getTargetRoles() != null) profile.setTargetRolesJson(JsonUtils.toJson(dto.getTargetRoles()));
        if (dto.getPreferredLocations() != null) profile.setPreferredLocationsJson(JsonUtils.toJson(dto.getPreferredLocations()));
        if (dto.getProjects() != null) profile.setProjectsJson(JsonUtils.toJson(dto.getProjects()));
        if (dto.getExperience() != null) profile.setExperienceJson(JsonUtils.toJson(dto.getExperience()));
        if (dto.getCertifications() != null) profile.setCertificationsJson(JsonUtils.toJson(dto.getCertifications()));
        if (dto.getLinks() != null) profile.setLinksJson(JsonUtils.toJson(dto.getLinks()));
        if (dto.getActiveResumeFilename() != null) profile.setActiveResumeFilename(dto.getActiveResumeFilename());

        profile = profileRepository.save(profile);
        return toDto(profile);
    }

    @Transactional
    public CandidateProfileDto uploadAndParseResume(Long userId, MultipartFile file) {
        CandidateProfileDto parsed = resumeParsingService.parseResume(file);

        CandidateProfile profile = profileRepository.findByUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
                CandidateProfile p = new CandidateProfile();
                p.setUser(user);
                return p;
            });

        if (parsed.getFullName() != null && !parsed.getFullName().equalsIgnoreCase("Candidate")) {
            profile.setFullName(parsed.getFullName());
        }
        if (parsed.getEmail() != null) {
            profile.setEmail(parsed.getEmail());
        }
        if (parsed.getPhone() != null) {
            profile.setPhone(parsed.getPhone());
        }
        if (parsed.getDegree() != null) {
            profile.setDegree(parsed.getDegree());
        }
        if (parsed.getEducation() != null) {
            profile.setEducation(parsed.getEducation());
        }
        if (parsed.getGraduationYear() != null) {
            profile.setGraduationYear(parsed.getGraduationYear());
        }
        profile.setExperienceYears(0.0);
        profile.setActiveResumeFilename(file.getOriginalFilename());

        if (parsed.getSkills() != null && !parsed.getSkills().isEmpty()) {
            profile.setSkillsJson(JsonUtils.toJson(parsed.getSkills()));
        }
        if (parsed.getTargetRoles() != null && !parsed.getTargetRoles().isEmpty()) {
            profile.setTargetRolesJson(JsonUtils.toJson(parsed.getTargetRoles()));
        }
        if (parsed.getPreferredLocations() != null && !parsed.getPreferredLocations().isEmpty()) {
            profile.setPreferredLocationsJson(JsonUtils.toJson(parsed.getPreferredLocations()));
        }
        if (parsed.getLinks() != null && !parsed.getLinks().isEmpty()) {
            profile.setLinksJson(JsonUtils.toJson(parsed.getLinks()));
        }

        profile = profileRepository.save(profile);
        return toDto(profile);
    }

    public CandidateProfileDto toDto(CandidateProfile profile) {
        CandidateProfileDto dto = new CandidateProfileDto();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUser() != null ? profile.getUser().getId() : null);
        dto.setFullName(profile.getFullName());
        dto.setEmail(profile.getEmail());
        dto.setPhone(profile.getPhone());
        dto.setEducation(profile.getEducation());
        dto.setDegree(profile.getDegree());
        dto.setGraduationYear(profile.getGraduationYear());
        dto.setExperienceYears(profile.getExperienceYears());
        dto.setActiveResumeFilename(profile.getActiveResumeFilename());
        dto.setUpdatedAt(profile.getUpdatedAt());

        dto.setSkills(JsonUtils.toStringList(profile.getSkillsJson()));
        dto.setTargetRoles(JsonUtils.toStringList(profile.getTargetRolesJson()));
        dto.setPreferredLocations(JsonUtils.toStringList(profile.getPreferredLocationsJson() != null ? profile.getPreferredLocationsJson() : "[]"));
        dto.setProjects(JsonUtils.toStringList(profile.getProjectsJson()));
        dto.setExperience(JsonUtils.toStringList(profile.getExperienceJson()));
        dto.setCertifications(JsonUtils.toStringList(profile.getCertificationsJson()));
        dto.setLinks(JsonUtils.toStringList(profile.getLinksJson()));

        return dto;
    }
}
