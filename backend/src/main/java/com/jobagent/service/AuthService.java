package com.jobagent.service;

import com.jobagent.dto.AuthRequest;
import com.jobagent.dto.AuthResponse;
import com.jobagent.dto.RegisterRequest;
import com.jobagent.dto.UserDto;
import com.jobagent.entity.CandidateProfile;
import com.jobagent.entity.User;
import com.jobagent.entity.UserSettings;
import com.jobagent.exception.BadRequestException;
import com.jobagent.exception.ResourceNotFoundException;
import com.jobagent.repository.CandidateProfileRepository;
import com.jobagent.repository.UserRepository;
import com.jobagent.repository.UserSettingsRepository;
import com.jobagent.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CandidateProfileRepository profileRepository;
    private final UserSettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.google.client-id:}")
    private String googleClientId;

    @Value("${app.google.redirect-uri:http://localhost:8080/api/auth/google/callback}")
    private String googleRedirectUri;

    public AuthService(
        UserRepository userRepository,
        CandidateProfileRepository profileRepository,
        UserSettingsRepository settingsRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.settingsRepository = settingsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new BadRequestException("An account with email " + request.getEmail() + " already exists");
        }

        User user = new User(
            request.getEmail().toLowerCase(),
            passwordEncoder.encode(request.getPassword()),
            request.getFullName()
        );
        user = userRepository.save(user);

        // Initialize default empty profile for candidate
        CandidateProfile profile = new CandidateProfile();
        profile.setUser(user);
        profile.setFullName(user.getFullName());
        profile.setEmail(user.getEmail());
        profile.setGraduationYear(2026);
        profile.setExperienceYears(0.0);
        profile.setTargetRolesJson("[\"Java Developer\",\"Backend Developer\",\"Software Engineer\",\"Associate Software Engineer\",\"Graduate Engineer Trainee\"]");
        profile.setPreferredLocationsJson("[\"Hyderabad\",\"Bangalore\",\"Pune\",\"Chennai\",\"Noida\",\"Gurugram\",\"Remote - India\"]");
        profile.setSkillsJson("[]");
        profileRepository.save(profile);

        // Initialize default settings
        UserSettings settings = new UserSettings();
        settings.setUser(user);
        settings.setDailyApplicationLimit(10);
        settings.setMinMatchScore(85);
        settings.setAutoApplyEnabled(false);
        settings.setMockMode(true);
        settings.setRealEmailEnabled(false);
        settings.setTargetGraduationYear(2026);
        settings.setMaxExperienceYears(1.0);
        settings.setTargetRolesJson("[\"Java Developer\",\"Backend Developer\",\"Software Engineer\",\"Associate Software Engineer\",\"Graduate Engineer Trainee\"]");
        settings.setExcludedRolesJson("[\"Senior\",\"Lead\",\"Manager\",\"Architect\",\"Director\"]");
        settingsRepository.save(settings);

        String token = jwtService.generateToken(user.getEmail(), user.getId());
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getCreatedAt());

        return new AuthResponse(token, userDto);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getId());
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getCreatedAt());

        return new AuthResponse(token, userDto);
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public String getGoogleAuthUrl() {
        if (googleClientId == null || googleClientId.isBlank()) {
            return "https://accounts.google.com/o/oauth2/v2/auth?mock=true";
        }
        return "https://accounts.google.com/o/oauth2/v2/auth"
            + "?client_id=" + googleClientId
            + "&redirect_uri=" + googleRedirectUri
            + "&response_type=code"
            + "&scope=https://www.googleapis.com/auth/gmail.readonly https://www.googleapis.com/auth/userinfo.email"
            + "&access_type=offline"
            + "&prompt=consent";
    }
}
