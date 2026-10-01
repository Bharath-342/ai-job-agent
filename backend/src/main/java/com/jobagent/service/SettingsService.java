package com.jobagent.service;

import com.jobagent.config.JsonUtils;
import com.jobagent.dto.UserSettingsDto;
import com.jobagent.entity.User;
import com.jobagent.entity.UserSettings;
import com.jobagent.exception.ResourceNotFoundException;
import com.jobagent.repository.UserRepository;
import com.jobagent.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    private final UserSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    public SettingsService(UserSettingsRepository settingsRepository, UserRepository userRepository) {
        this.settingsRepository = settingsRepository;
        this.userRepository = userRepository;
    }

    public UserSettingsDto getSettings(Long userId) {
        UserSettings settings = settingsRepository.findByUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
                UserSettings s = new UserSettings();
                s.setUser(user);
                return settingsRepository.save(s);
            });
        return toDto(settings);
    }

    @Transactional
    public UserSettingsDto updateSettings(Long userId, UserSettingsDto dto) {
        UserSettings settings = settingsRepository.findByUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
                UserSettings s = new UserSettings();
                s.setUser(user);
                return s;
            });

        if (dto.getDailyApplicationLimit() != null) settings.setDailyApplicationLimit(dto.getDailyApplicationLimit());
        if (dto.getMinMatchScore() != null) settings.setMinMatchScore(dto.getMinMatchScore());
        if (dto.getAutoApplyEnabled() != null) settings.setAutoApplyEnabled(dto.getAutoApplyEnabled());
        if (dto.getMockMode() != null) settings.setMockMode(dto.getMockMode());
        if (dto.getRealEmailEnabled() != null) settings.setRealEmailEnabled(dto.getRealEmailEnabled());
        if (dto.getTargetGraduationYear() != null) settings.setTargetGraduationYear(dto.getTargetGraduationYear());
        if (dto.getMaxExperienceYears() != null) settings.setMaxExperienceYears(dto.getMaxExperienceYears());

        if (dto.getTargetRoles() != null) settings.setTargetRolesJson(JsonUtils.toJson(dto.getTargetRoles()));
        if (dto.getExcludedRoles() != null) settings.setExcludedRolesJson(JsonUtils.toJson(dto.getExcludedRoles()));
        if (dto.getPreferredTechnologies() != null) settings.setPreferredTechnologiesJson(JsonUtils.toJson(dto.getPreferredTechnologies()));

        settings = settingsRepository.save(settings);
        return toDto(settings);
    }

    public UserSettingsDto toDto(UserSettings s) {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setId(s.getId());
        dto.setDailyApplicationLimit(s.getDailyApplicationLimit());
        dto.setMinMatchScore(s.getMinMatchScore());
        dto.setAutoApplyEnabled(s.getAutoApplyEnabled());
        dto.setMockMode(s.getMockMode());
        dto.setRealEmailEnabled(s.getRealEmailEnabled());
        dto.setTargetGraduationYear(s.getTargetGraduationYear());
        dto.setMaxExperienceYears(s.getMaxExperienceYears());

        dto.setTargetRoles(JsonUtils.toStringList(s.getTargetRolesJson()));
        dto.setExcludedRoles(JsonUtils.toStringList(s.getExcludedRolesJson()));
        dto.setPreferredTechnologies(JsonUtils.toStringList(s.getPreferredTechnologiesJson()));
        return dto;
    }
}
