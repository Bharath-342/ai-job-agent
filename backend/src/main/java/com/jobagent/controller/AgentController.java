package com.jobagent.controller;

import com.jobagent.entity.User;
import com.jobagent.entity.UserSettings;
import com.jobagent.repository.CandidateProfileRepository;
import com.jobagent.repository.UserSettingsRepository;
import com.jobagent.service.AutoApplyEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API for the autonomous auto-apply agent.
 *
 * Endpoints:
 *   POST /api/agent/run        — Immediately trigger an auto-apply cycle for this user
 *   POST /api/agent/enable     — Enable autonomous auto-apply (saves to user settings)
 *   POST /api/agent/disable    — Disable autonomous auto-apply
 *   GET  /api/agent/status     — Get current agent config for this user
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final AutoApplyEngine autoApplyEngine;
    private final UserSettingsRepository settingsRepository;
    private final CandidateProfileRepository profileRepository;

    public AgentController(
        AutoApplyEngine autoApplyEngine,
        UserSettingsRepository settingsRepository,
        CandidateProfileRepository profileRepository
    ) {
        this.autoApplyEngine = autoApplyEngine;
        this.settingsRepository = settingsRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Immediately trigger one auto-apply cycle for the authenticated user.
     * Returns a summary of what was applied, skipped, or failed.
     */
    @PostMapping("/run")
    public ResponseEntity<?> triggerNow(@AuthenticationPrincipal User user) {
        log.info("AGENT_API: Manual trigger by user {}", user.getEmail());

        var profile = profileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "No candidate profile found",
                "message", "Please upload your resume first before running the agent.",
                "action", "POST /api/profile/resume"
            ));
        }

        var settings = settingsRepository.findByUserId(user.getId()).orElse(new UserSettings());

        // Temporarily force auto-apply to true for this manual run
        boolean wasAutoApply = Boolean.TRUE.equals(settings.getAutoApplyEnabled());
        settings.setAutoApplyEnabled(true);

        AutoApplyEngine.AutoApplyUserResult result = autoApplyEngine.runForUser(user, profile, settings);

        // Restore original setting if it wasn't already on
        if (!wasAutoApply) {
            settings.setAutoApplyEnabled(false);
        }

        return ResponseEntity.ok(Map.of(
            "status", "completed",
            "user", user.getEmail(),
            "applied", result.applied(),
            "skipped", result.skipped(),
            "failed", result.failed(),
            "message", result.applied() > 0
                ? "✅ Agent applied to " + result.applied() + " job(s) on your behalf!"
                : "No new eligible jobs found this cycle. Agent will keep checking every hour."
        ));
    }

    /**
     * Enable autonomous auto-apply — agent will apply automatically every hour.
     */
    @PostMapping("/enable")
    public ResponseEntity<?> enableAutoApply(@AuthenticationPrincipal User user) {
        UserSettings settings = settingsRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                UserSettings s = new UserSettings();
                s.setUser(user);
                return s;
            });

        settings.setAutoApplyEnabled(true);
        settingsRepository.save(settings);

        log.info("AGENT_API: Auto-apply ENABLED for user {}", user.getEmail());
        return ResponseEntity.ok(Map.of(
            "autoApplyEnabled", true,
            "message", "✅ Autonomous mode ON. Your AI agent will automatically apply to matching jobs every hour."
        ));
    }

    /**
     * Disable autonomous auto-apply — agent stops automatic submissions.
     */
    @PostMapping("/disable")
    public ResponseEntity<?> disableAutoApply(@AuthenticationPrincipal User user) {
        UserSettings settings = settingsRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                UserSettings s = new UserSettings();
                s.setUser(user);
                return s;
            });

        settings.setAutoApplyEnabled(false);
        settingsRepository.save(settings);

        log.info("AGENT_API: Auto-apply DISABLED for user {}", user.getEmail());
        return ResponseEntity.ok(Map.of(
            "autoApplyEnabled", false,
            "message", "⏸ Autonomous mode OFF. You can trigger manually via POST /api/agent/run."
        ));
    }

    /**
     * Get current agent configuration and status for this user.
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@AuthenticationPrincipal User user) {
        var settings = settingsRepository.findByUserId(user.getId()).orElse(new UserSettings());
        var profile = profileRepository.findByUserId(user.getId()).orElse(null);

        return ResponseEntity.ok(Map.of(
            "user", user.getEmail(),
            "autoApplyEnabled", Boolean.TRUE.equals(settings.getAutoApplyEnabled()),
            "mockMode", Boolean.TRUE.equals(settings.getMockMode()),
            "minMatchScore", settings.getMinMatchScore() != null ? settings.getMinMatchScore() : 85,
            "dailyApplicationLimit", settings.getDailyApplicationLimit() != null ? settings.getDailyApplicationLimit() : 10,
            "profileReady", profile != null,
            "agentSchedule", "Runs every 60 minutes automatically when auto-apply is enabled",
            "message", Boolean.TRUE.equals(settings.getAutoApplyEnabled())
                ? "🤖 Agent is ACTIVE — applying autonomously every hour"
                : "⏸ Agent is PAUSED — enable with POST /api/agent/enable"
        ));
    }
}
