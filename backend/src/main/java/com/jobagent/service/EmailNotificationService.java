package com.jobagent.service;

import com.jobagent.entity.CandidateProfile;
import com.jobagent.entity.Job;
import com.jobagent.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Sends real HTML emails to users — application confirmations, manual action alerts, digests.
 *
 * If SMTP is not configured, all emails are logged (safe no-op mode).
 */
@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@ai-job-agent.app}")
    private String fromAddress;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.frontend-url:https://ai-job-agent-qtyz.onrender.com}")
    private String frontendUrl;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Notify user that their AI agent automatically applied on their behalf.
     */
    public void sendApplicationSubmitted(User user, Job job, int matchScore, String confirmationId) {
        String subject = "✅ AI Agent Applied: " + job.getTitle() + " @ " + job.getCompany();
        String html = """
            <div style="font-family:Inter,sans-serif;max-width:600px;margin:0 auto;background:#0f172a;color:#e2e8f0;border-radius:12px;overflow:hidden;">
              <div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:32px;text-align:center;">
                <h1 style="margin:0;font-size:24px;color:white;">🤖 AI Agent Applied!</h1>
                <p style="margin:8px 0 0;color:rgba(255,255,255,0.8);">Your autonomous job agent submitted an application</p>
              </div>
              <div style="padding:32px;">
                <div style="background:#1e293b;border-radius:8px;padding:20px;margin-bottom:20px;border-left:4px solid #6366f1;">
                  <h2 style="margin:0 0 8px;color:#a5b4fc;font-size:18px;">%s</h2>
                  <p style="margin:0;color:#94a3b8;font-size:14px;">%s • %s</p>
                </div>
                <div style="display:flex;gap:16px;margin-bottom:20px;">
                  <div style="flex:1;background:#1e293b;border-radius:8px;padding:16px;text-align:center;">
                    <div style="font-size:32px;font-weight:bold;color:#22c55e;">%d%%</div>
                    <div style="color:#94a3b8;font-size:12px;">Match Score</div>
                  </div>
                  <div style="flex:1;background:#1e293b;border-radius:8px;padding:16px;text-align:center;">
                    <div style="font-size:14px;font-weight:bold;color:#a5b4fc;word-break:break-all;">#%s</div>
                    <div style="color:#94a3b8;font-size:12px;">Confirmation ID</div>
                  </div>
                </div>
                <p style="color:#94a3b8;font-size:14px;line-height:1.6;">
                  Your AI agent has automatically submitted this application using your profile and resume.
                  You'll receive another email when the employer responds.
                </p>
                <a href="%s/dashboard" style="display:inline-block;background:linear-gradient(135deg,#6366f1,#8b5cf6);color:white;text-decoration:none;padding:12px 24px;border-radius:8px;font-weight:600;margin-top:8px;">
                  View Dashboard
                </a>
              </div>
              <div style="padding:16px 32px;background:#0f172a;border-top:1px solid #1e293b;text-align:center;color:#475569;font-size:12px;">
                AI Job Agent • Autonomous Applications for 2026 Freshers
              </div>
            </div>
            """.formatted(
                job.getTitle(), job.getCompany(), job.getLocation(),
                matchScore, confirmationId, frontendUrl
            );

        sendHtmlEmail(user.getEmail(), subject, html);
    }

    /**
     * Notify user that a job needs manual application (unsupported portal).
     */
    public void sendManualActionRequired(User user, Job job, int matchScore, String applyUrl) {
        String subject = "⚡ Action Needed: Apply to " + job.getTitle() + " @ " + job.getCompany();
        String html = """
            <div style="font-family:Inter,sans-serif;max-width:600px;margin:0 auto;background:#0f172a;color:#e2e8f0;border-radius:12px;overflow:hidden;">
              <div style="background:linear-gradient(135deg,#f59e0b,#ef4444);padding:32px;text-align:center;">
                <h1 style="margin:0;font-size:24px;color:white;">⚡ Action Required</h1>
                <p style="margin:8px 0 0;color:rgba(255,255,255,0.8);">Your AI agent found a great match — manual apply needed</p>
              </div>
              <div style="padding:32px;">
                <div style="background:#1e293b;border-radius:8px;padding:20px;margin-bottom:20px;border-left:4px solid #f59e0b;">
                  <h2 style="margin:0 0 8px;color:#fcd34d;font-size:18px;">%s</h2>
                  <p style="margin:0;color:#94a3b8;font-size:14px;">%s • %s</p>
                </div>
                <div style="background:#1e293b;border-radius:8px;padding:16px;text-align:center;margin-bottom:20px;">
                  <div style="font-size:32px;font-weight:bold;color:#22c55e;">%d%%</div>
                  <div style="color:#94a3b8;font-size:12px;">Match Score — Highly Recommended</div>
                </div>
                <p style="color:#94a3b8;font-size:14px;line-height:1.6;">
                  This portal requires you to log in and apply manually. Your profile data is ready — it should take under 5 minutes.
                </p>
                <a href="%s" style="display:inline-block;background:linear-gradient(135deg,#f59e0b,#ef4444);color:white;text-decoration:none;padding:12px 24px;border-radius:8px;font-weight:600;margin-top:8px;">
                  Apply Now →
                </a>
              </div>
              <div style="padding:16px 32px;background:#0f172a;border-top:1px solid #1e293b;text-align:center;color:#475569;font-size:12px;">
                AI Job Agent • Autonomous Applications for 2026 Freshers
              </div>
            </div>
            """.formatted(
                job.getTitle(), job.getCompany(), job.getLocation(),
                matchScore, applyUrl
            );

        sendHtmlEmail(user.getEmail(), subject, html);
    }

    /**
     * Send an actual email job application to a company that accepts email apps.
     */
    public void sendJobApplication(User user, Job job, CandidateProfile profile) {
        String toAddress = extractEmailFromUrl(job.getJobUrl());
        if (toAddress == null) {
            log.warn("EMAIL_APPLY: Cannot extract email address from job URL: {}", job.getJobUrl());
            return;
        }

        String subject = "Application for " + job.getTitle() + " – " + (profile.getFullName() != null ? profile.getFullName() : user.getEmail());
        String body = """
            Dear Hiring Team at %s,

            I am writing to apply for the position of %s.

            Name: %s
            Email: %s
            Phone: %s
            Graduation Year: %s

            I am a 2026 batch graduate with skills in %s.

            Please find my resume attached. I am enthusiastic about contributing to %s and believe my skills align well with this opportunity.

            Thank you for your consideration.

            Best regards,
            %s
            %s
            """.formatted(
                job.getCompany(),
                job.getTitle(),
                profile.getFullName() != null ? profile.getFullName() : user.getEmail(),
                user.getEmail(),
                profile.getPhone() != null ? profile.getPhone() : "Available on request",
                profile.getGraduationYear() != null ? profile.getGraduationYear().toString() : "2026",
                profile.getSkillsJson() != null ? profile.getSkillsJson().replace("[", "").replace("]", "").replace("\"", "") : "various technologies",
                job.getCompany(),
                profile.getFullName() != null ? profile.getFullName() : user.getEmail(),
                user.getEmail()
            );

        if (!mailEnabled) {
            log.info("EMAIL_APPLY: [DRY-RUN] Would send application email to {} for job {}", toAddress, job.getJobKey());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(user.getEmail()); // Send from user's address if possible
            message.setTo(toAddress);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("EMAIL_APPLY: Sent application email to {} for {}", toAddress, job.getTitle());
        } catch (Exception e) {
            log.error("EMAIL_APPLY: Failed to send application email to {}: {}", toAddress, e.getMessage());
        }
    }

    /**
     * Send a daily/weekly digest of auto-apply activity.
     */
    public void sendWeeklyDigest(User user, int applied, int pending, int interviews) {
        String subject = "📊 Your AI Agent Weekly Report";
        String html = """
            <div style="font-family:Inter,sans-serif;max-width:600px;margin:0 auto;background:#0f172a;color:#e2e8f0;border-radius:12px;overflow:hidden;">
              <div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:32px;text-align:center;">
                <h1 style="margin:0;font-size:24px;color:white;">📊 Weekly AI Agent Report</h1>
                <p style="margin:8px 0 0;color:rgba(255,255,255,0.8);">Here's what your AI agent did this week</p>
              </div>
              <div style="padding:32px;">
                <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:24px;">
                  <div style="background:#1e293b;border-radius:8px;padding:16px;text-align:center;">
                    <div style="font-size:32px;font-weight:bold;color:#6366f1;">%d</div>
                    <div style="color:#94a3b8;font-size:12px;">Applications Sent</div>
                  </div>
                  <div style="background:#1e293b;border-radius:8px;padding:16px;text-align:center;">
                    <div style="font-size:32px;font-weight:bold;color:#f59e0b;">%d</div>
                    <div style="color:#94a3b8;font-size:12px;">Awaiting Response</div>
                  </div>
                  <div style="background:#1e293b;border-radius:8px;padding:16px;text-align:center;">
                    <div style="font-size:32px;font-weight:bold;color:#22c55e;">%d</div>
                    <div style="color:#94a3b8;font-size:12px;">Interview Invites</div>
                  </div>
                </div>
                <a href="%s/dashboard" style="display:inline-block;background:linear-gradient(135deg,#6366f1,#8b5cf6);color:white;text-decoration:none;padding:12px 24px;border-radius:8px;font-weight:600;">
                  View Full Dashboard
                </a>
              </div>
            </div>
            """.formatted(applied, pending, interviews, frontendUrl);

        sendHtmlEmail(user.getEmail(), subject, html);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        if (!mailEnabled) {
            log.info("EMAIL_NOTIFY: [DRY-RUN] To={}, Subject={}", to, subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("EMAIL_NOTIFY: Sent '{}' to {}", subject, to);
        } catch (Exception e) {
            log.error("EMAIL_NOTIFY: Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String extractEmailFromUrl(String url) {
        if (url == null) return null;
        if (url.startsWith("mailto:")) {
            return url.substring(7).split("\\?")[0];
        }
        return null;
    }
}
