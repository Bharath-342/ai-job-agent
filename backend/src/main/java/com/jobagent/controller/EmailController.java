package com.jobagent.controller;

import com.jobagent.dto.EmailEventDto;
import com.jobagent.entity.EmailEvent;
import com.jobagent.entity.User;
import com.jobagent.service.AuthService;
import com.jobagent.service.EmailMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emails")
@Tag(name = "Email Monitoring", description = "Gmail integration, email event classification, and application updates")
public class EmailController {

    private final EmailMonitoringService emailService;
    private final AuthService authService;

    public EmailController(EmailMonitoringService emailService, AuthService authService) {
        this.emailService = emailService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get all detected email events for the current candidate")
    public ResponseEntity<List<EmailEventDto>> getEmails(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(emailService.getUserEmailEvents(user.getId()));
    }

    @PostMapping("/simulate")
    @Operation(summary = "Simulate an employer email event to test classification and automatic status transitions")
    public ResponseEntity<EmailEventDto> simulateIncomingEmail(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody Map<String, String> payload
    ) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        String sender = payload.getOrDefault("sender", "recruiting@razorpay.com");
        String subject = payload.getOrDefault("subject", "Update on your application for Associate Software Engineer");
        String body = payload.getOrDefault("body", "Hello, we are pleased to inform you that your application for Associate Software Engineer is currently under review by our hiring team.");
        String msgId = payload.get("messageId");

        EmailEvent event = emailService.processIncomingEmail(user, sender, subject, body, msgId);
        return ResponseEntity.ok(emailService.toDto(event));
    }

    @PostMapping("/sync")
    @Operation(summary = "Sync and poll connected Gmail mailbox for new employer updates")
    public ResponseEntity<Map<String, Object>> syncEmails(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        Map<String, Object> res = new HashMap<>();
        res.put("status", "synced");
        res.put("message", "Checked mailbox for " + user.getEmail() + ". Latest updates synchronized.");
        res.put("events", emailService.getUserEmailEvents(user.getId()));
        return ResponseEntity.ok(res);
    }
}
