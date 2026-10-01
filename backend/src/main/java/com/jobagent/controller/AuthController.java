package com.jobagent.controller;

import com.jobagent.dto.AuthRequest;
import com.jobagent.dto.AuthResponse;
import com.jobagent.dto.RegisterRequest;
import com.jobagent.dto.UserDto;
import com.jobagent.entity.User;
import com.jobagent.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, login, and OAuth authentication endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account with default 2026 fresher profile and settings")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile details")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(userDetails.getUsername());
        UserDto dto = new UserDto(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getCreatedAt());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/google/url")
    @Operation(summary = "Get the Google OAuth2 consent URL for Gmail read integration")
    public ResponseEntity<Map<String, String>> getGoogleAuthUrl() {
        Map<String, String> res = new HashMap<>();
        res.put("url", authService.getGoogleAuthUrl());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/google/callback")
    @Operation(summary = "OAuth callback endpoint for Google Gmail integration")
    public ResponseEntity<Map<String, Object>> handleGoogleCallback(@RequestParam(required = false) String code, @RequestParam(required = false) String error) {
        Map<String, Object> res = new HashMap<>();
        if (error != null) {
            res.put("status", "error");
            res.put("message", "OAuth authorization error: " + error);
            return ResponseEntity.badRequest().body(res);
        }
        res.put("status", "success");
        res.put("message", "Google OAuth token received and linked successfully for email monitoring");
        res.put("codeReceived", code != null && !code.isBlank());
        return ResponseEntity.ok(res);
    }
}
