package com.jobagent.controller;

import com.jobagent.dto.UserSettingsDto;
import com.jobagent.entity.User;
import com.jobagent.service.AuthService;
import com.jobagent.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@Tag(name = "Settings", description = "User preferences, limits, and auto-apply toggles")
public class SettingsController {

    private final SettingsService settingsService;
    private final AuthService authService;

    public SettingsController(SettingsService settingsService, AuthService authService) {
        this.settingsService = settingsService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get user settings and application guardrails")
    public ResponseEntity<UserSettingsDto> getSettings(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(settingsService.getSettings(user.getId()));
    }

    @PutMapping
    @Operation(summary = "Update user settings, thresholds, and quota")
    public ResponseEntity<UserSettingsDto> updateSettings(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody UserSettingsDto dto
    ) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(settingsService.updateSettings(user.getId(), dto));
    }
}
