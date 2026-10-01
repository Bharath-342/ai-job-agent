package com.jobagent.controller;

import com.jobagent.entity.Notification;
import com.jobagent.entity.User;
import com.jobagent.service.AuthService;
import com.jobagent.service.NotificationService;
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
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "In-app alerts and application status change notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    public NotificationController(NotificationService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get all notifications for the current user")
    public ResponseEntity<List<Notification>> getNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(notificationService.getUserNotifications(user.getId()));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread notifications")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        Map<String, Long> res = new HashMap<>();
        res.put("count", notificationService.getUnreadCount(user.getId()));
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a specific notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }
}
