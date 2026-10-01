package com.jobagent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Application and system health monitoring")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.mock-mode:true}")
    private boolean mockMode;

    @Value("${app.auto-apply-enabled:false}")
    private boolean autoApplyEnabled;

    @Value("${app.real-email-enabled:false}")
    private boolean realEmailEnabled;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    @Operation(summary = "Check application and database health status")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("application", "UP");
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("mockMode", mockMode);
        health.put("autoApplyEnabled", autoApplyEnabled);
        health.put("realEmailEnabled", realEmailEnabled);

        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("databaseError", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }

        return ResponseEntity.ok(health);
    }
}
