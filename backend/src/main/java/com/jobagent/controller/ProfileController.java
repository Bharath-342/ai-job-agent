package com.jobagent.controller;

import com.jobagent.dto.CandidateProfileDto;
import com.jobagent.entity.User;
import com.jobagent.service.AuthService;
import com.jobagent.service.CandidateProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Candidate Profile", description = "Candidate profile retrieval, manual editing, and resume parsing")
public class ProfileController {

    private final CandidateProfileService profileService;
    private final AuthService authService;

    public ProfileController(CandidateProfileService profileService, AuthService authService) {
        this.profileService = profileService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get the candidate profile of the authenticated user")
    public ResponseEntity<CandidateProfileDto> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(profileService.getProfileByUserId(user.getId()));
    }

    @PutMapping
    @Operation(summary = "Update the candidate profile")
    public ResponseEntity<CandidateProfileDto> updateProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody CandidateProfileDto dto
    ) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(profileService.updateProfile(user.getId(), dto));
    }

    @PostMapping(value = "/upload-resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload PDF or DOCX resume to extract metadata and populate candidate profile")
    public ResponseEntity<CandidateProfileDto> uploadResume(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam("file") MultipartFile file
    ) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(profileService.uploadAndParseResume(user.getId(), file));
    }
}
