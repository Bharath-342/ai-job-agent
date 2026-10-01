package com.jobagent.controller;

import com.jobagent.dto.DashboardDto;
import com.jobagent.entity.User;
import com.jobagent.service.AuthService;
import com.jobagent.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "High-level metrics, funnel stats, and daily quota monitoring")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    public DashboardController(DashboardService dashboardService, AuthService authService) {
        this.dashboardService = dashboardService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get aggregated dashboard statistics, quotas, and recent activity")
    public ResponseEntity<DashboardDto> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(dashboardService.getDashboardStats(user.getId()));
    }
}
