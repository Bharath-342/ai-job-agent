package com.jobagent.controller;

import com.jobagent.dto.JobDto;
import com.jobagent.dto.JobMatchResultDto;
import com.jobagent.entity.CandidateProfile;
import com.jobagent.entity.Job;
import com.jobagent.entity.User;
import com.jobagent.entity.UserSettings;
import com.jobagent.exception.ResourceNotFoundException;
import com.jobagent.repository.CandidateProfileRepository;
import com.jobagent.repository.JobRepository;
import com.jobagent.repository.UserSettingsRepository;
import com.jobagent.service.AuthService;
import com.jobagent.service.JobDiscoveryService;
import com.jobagent.service.JobMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Job discovery, strict fresher filtering, and candidate matching")
public class JobController {

    private final JobDiscoveryService jobDiscoveryService;
    private final JobMatchingService jobMatchingService;
    private final JobRepository jobRepository;
    private final CandidateProfileRepository profileRepository;
    private final UserSettingsRepository settingsRepository;
    private final AuthService authService;

    public JobController(
        JobDiscoveryService jobDiscoveryService,
        JobMatchingService jobMatchingService,
        JobRepository jobRepository,
        CandidateProfileRepository profileRepository,
        UserSettingsRepository settingsRepository,
        AuthService authService
    ) {
        this.jobDiscoveryService = jobDiscoveryService;
        this.jobMatchingService = jobMatchingService;
        this.jobRepository = jobRepository;
        this.profileRepository = profileRepository;
        this.settingsRepository = settingsRepository;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get all discovered active jobs")
    public ResponseEntity<List<JobDto>> getAllJobs() {
        List<JobDto> jobs = jobRepository.findByIsActiveTrueOrderByPostedDateDesc()
            .stream()
            .map(jobDiscoveryService::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/eligible")
    @Operation(summary = "Get all jobs that pass the strict India 2026 fresher filter with matching scores")
    public ResponseEntity<List<JobMatchResultDto>> getEligibleJobs(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        CandidateProfile profile = profileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        UserSettings settings = settingsRepository.findByUserId(user.getId())
            .orElseGet(UserSettings::new);

        List<JobMatchResultDto> results = jobRepository.findByIsActiveTrueOrderByPostedDateDesc()
            .stream()
            .map(job -> jobMatchingService.matchJob(job, profile, settings))
            .filter(r -> r.isEligible() || r.isManualReviewRequired())
            .sorted((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}/match")
    @Operation(summary = "Calculate detailed match breakdown for a specific job against candidate profile")
    public ResponseEntity<JobMatchResultDto> getJobMatch(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        CandidateProfile profile = profileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        UserSettings settings = settingsRepository.findByUserId(user.getId())
            .orElseGet(UserSettings::new);
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));

        JobMatchResultDto match = jobMatchingService.matchJob(job, profile, settings);
        return ResponseEntity.ok(match);
    }

    @PostMapping("/discover")
    @Operation(summary = "Trigger on-demand discovery cycle for official career pages and ATS platforms")
    public ResponseEntity<List<JobDto>> triggerDiscovery() {
        List<JobDto> discovered = jobDiscoveryService.discoverJobs()
            .stream()
            .map(jobDiscoveryService::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(discovered);
    }
}
