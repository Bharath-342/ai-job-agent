package com.jobagent.service;

import com.jobagent.entity.ApplicationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class EmailClassificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailClassificationService.class);

    private static final List<String> SPAM_KEYWORDS = Arrays.asList(
        "unsubscribe", "weekly newsletter", "promo", "special discount", "deal of the day",
        "digest", "marketing", "webinar invitation", "sale 50%"
    );

    public record ClassificationResult(
        ApplicationStatus detectedStatus,
        double confidence,
        boolean isJobRelated,
        String rationale
    ) {}

    public ClassificationResult classify(String subject, String body, String sender) {
        String combined = (subject + " " + body).toLowerCase();
        String senderLower = sender != null ? sender.toLowerCase() : "";

        // 1. Filter out spam and newsletters
        for (String spam : SPAM_KEYWORDS) {
            if (combined.contains(spam) || senderLower.contains("newsletter") || senderLower.contains("marketing")) {
                return new ClassificationResult(null, 0.0, false, "Filtered: Promotional/Newsletter email");
            }
        }

        // 2. Classify by stage keywords
        if (combined.contains("offer letter") || combined.contains("extend an offer") || combined.contains("job offer")) {
            return new ClassificationResult(ApplicationStatus.OFFER_RECEIVED, 0.95, true, "Detected official job offer keywords");
        }

        if (combined.contains("interview scheduled") || combined.contains("calendar invitation") || combined.contains("interview confirmed")) {
            return new ClassificationResult(ApplicationStatus.INTERVIEW_SCHEDULED, 0.90, true, "Detected interview confirmation/scheduling");
        }

        if (combined.contains("interview invitation") || combined.contains("invite you to interview") ||
            combined.contains("schedule an interview") || combined.contains("technical interview round") || combined.contains("phone screen")) {
            return new ClassificationResult(ApplicationStatus.INTERVIEW_INVITATION, 0.90, true, "Detected formal interview invitation");
        }

        if (combined.contains("online assessment") || combined.contains("hackerrank") || combined.contains("codesignal") ||
            combined.contains("coding test") || combined.contains("take our assessment")) {
            return new ClassificationResult(ApplicationStatus.ASSESSMENT_RECEIVED, 0.95, true, "Detected coding assessment invitation");
        }

        if (combined.contains("regret to inform") || combined.contains("decided to move forward with other candidates") ||
            combined.contains("not moving forward with your application") || combined.contains("position has been filled")) {
            return new ClassificationResult(ApplicationStatus.REJECTED_BY_COMPANY, 0.92, true, "Detected employer rejection notification");
        }

        if (combined.contains("under review") || combined.contains("reviewing your application") ||
            combined.contains("recruiter is currently reviewing") || combined.contains("profile is shortlisted")) {
            return new ClassificationResult(ApplicationStatus.UNDER_REVIEW, 0.88, true, "Detected application under review status");
        }

        if (combined.contains("thank you for applying") || combined.contains("application received") ||
            combined.contains("we have received your application") || combined.contains("application confirmation")) {
            return new ClassificationResult(ApplicationStatus.APPLICATION_RECEIVED, 0.92, true, "Detected initial application receipt confirmation");
        }

        // Low confidence or unrelated
        return new ClassificationResult(null, 0.3, false, "EMAIL_MATCH_REVIEW_REQUIRED: Insufficient confidence to establish ATS status");
    }
}
