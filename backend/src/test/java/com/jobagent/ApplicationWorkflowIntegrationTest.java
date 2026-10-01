package com.jobagent;

import com.jobagent.dto.*;
import com.jobagent.entity.Application;
import com.jobagent.entity.ApplicationStatus;
import com.jobagent.entity.Job;
import com.jobagent.entity.User;
import com.jobagent.repository.ApplicationRepository;
import com.jobagent.repository.JobRepository;
import com.jobagent.repository.UserRepository;
import com.jobagent.service.ApplicationSubmissionService;
import com.jobagent.service.ApplicationTrackingService;
import com.jobagent.service.AuthService;
import com.jobagent.service.EmailMonitoringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ApplicationWorkflowIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationSubmissionService submissionService;

    @Autowired
    private ApplicationTrackingService trackingService;

    @Autowired
    private EmailMonitoringService emailService;

    @Autowired
    private com.jobagent.service.CandidateProfileService profileService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    @DisplayName("End-to-End Workflow: Register -> Discovered Job -> Submit -> Manual Handoff -> Email ATS Event")
    void testCompleteApplicationLifecycle() {
        // 1. User Registration
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("integration.tester@example.com");
        registerReq.setPassword("Password123!");
        registerReq.setFullName("Integration Test Candidate");
        AuthResponse authRes = authService.register(registerReq);

        assertNotNull(authRes.getToken());
        User user = userRepository.findByEmail("integration.tester@example.com").orElseThrow();
        assertEquals("Integration Test Candidate", user.getFullName());

        // Populate skills from parsed resume
        CandidateProfileDto profileDto = new CandidateProfileDto();
        profileDto.setFullName("Integration Test Candidate");
        profileDto.setEmail(user.getEmail());
        profileDto.setGraduationYear(2026);
        profileDto.setSkills(java.util.Arrays.asList("Java", "Spring Boot", "SQL", "PostgreSQL", "Data Structures", "Git"));
        profileDto.setTargetRoles(java.util.Arrays.asList("Java Developer", "Associate Software Engineer", "Backend Developer"));
        profileDto.setPreferredLocations(java.util.Arrays.asList("Hyderabad", "Bangalore"));
        profileService.updateProfile(user.getId(), profileDto);

        // 2. Discover Jobs
        List<Job> jobs = jobRepository.findAll();
        assertFalse(jobs.isEmpty(), "Seed jobs should be populated by discovery service");

        Job targetJob = jobs.stream()
            .filter(j -> j.getJobKey().contains("razorpay"))
            .findFirst()
            .orElseThrow();

        // 3. Submit Application
        ApplyRequestDto applyReq = new ApplyRequestDto();
        applyReq.setJobId(targetJob.getId());
        applyReq.setCustomNotes("Automated test application");

        Application app = submissionService.processApplication(user, applyReq);
        assertNotNull(app.getId());
        assertTrue(
            app.getStatus() == ApplicationStatus.READY_TO_APPLY ||
            app.getStatus() == ApplicationStatus.APPLIED ||
            app.getStatus() == ApplicationStatus.MANUAL_ACTION_REQUIRED
        );

        // 4. Test Manual Handoff Resolution
        ManualActionResolutionDto resolution = new ManualActionResolutionDto();
        resolution.setResolutionStatus(ApplicationStatus.APPLIED);
        resolution.setConfirmationId("CONF-INT-999");
        resolution.setUserNotes("Submitted through career portal");

        ApplicationDto resolved = trackingService.resolveManualAction(app.getId(), user, resolution);
        assertEquals(ApplicationStatus.APPLIED, resolved.getStatus());
        assertEquals("CONF-INT-999", resolved.getConfirmationId());

        // 5. Test Email Detection & Status Transition
        emailService.processIncomingEmail(
            user,
            "careers@razorpay.com",
            "Update regarding your application for Associate Software Engineer",
            "We are pleased to inform you that your profile is currently under review by our tech lead.",
            "MSG-TEST-001"
        );

        ApplicationDto updated = trackingService.getApplicationById(app.getId(), user.getId());
        assertEquals(ApplicationStatus.UNDER_REVIEW, updated.getStatus());
        assertFalse(updated.getEvents().isEmpty());
    }
}
