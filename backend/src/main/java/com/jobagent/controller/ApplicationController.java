package com.jobagent.controller;

import com.jobagent.dto.ApplicationDto;
import com.jobagent.dto.ApplyRequestDto;
import com.jobagent.dto.ManualActionResolutionDto;
import com.jobagent.entity.Application;
import com.jobagent.entity.User;
import com.jobagent.service.AuthService;
import com.jobagent.service.ApplicationSubmissionService;
import com.jobagent.service.ApplicationTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@Tag(name = "Applications", description = "Application submission, timeline tracking, and manual handoff resolution")
public class ApplicationController {

    private final ApplicationSubmissionService submissionService;
    private final ApplicationTrackingService trackingService;
    private final AuthService authService;

    public ApplicationController(
        ApplicationSubmissionService submissionService,
        ApplicationTrackingService trackingService,
        AuthService authService
    ) {
        this.submissionService = submissionService;
        this.trackingService = trackingService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get all applications for the current candidate")
    public ResponseEntity<List<ApplicationDto>> getApplications(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(trackingService.getUserApplications(user.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specific application details with full audit timeline events")
    public ResponseEntity<ApplicationDto> getApplicationById(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(trackingService.getApplicationById(id, user.getId()));
    }

    @PostMapping("/apply")
    @Operation(summary = "Submit or initiate verified application workflow")
    public ResponseEntity<ApplicationDto> apply(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ApplyRequestDto request
    ) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        Application app = submissionService.processApplication(user, request);
        return ResponseEntity.ok(trackingService.toDto(app));
    }

    @PostMapping("/{id}/resolve-manual")
    @Operation(summary = "Resolve manual action handoff and mark application as applied")
    public ResponseEntity<ApplicationDto> resolveManualAction(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody ManualActionResolutionDto resolution
    ) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(trackingService.resolveManualAction(id, user, resolution));
    }
}
