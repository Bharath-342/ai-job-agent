package com.jobagent;

import com.jobagent.entity.ApplicationStatus;
import com.jobagent.service.EmailClassificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EmailClassificationTest {

    private EmailClassificationService classificationService;

    @BeforeEach
    void setUp() {
        classificationService = new EmailClassificationService();
    }

    @Test
    @DisplayName("Should detect UNDER_REVIEW status from hiring manager email")
    void testUnderReviewClassification() {
        String subject = "Status update on your application";
        String body = "Dear Candidate, your application is currently under review by our engineering team.";
        String sender = "talent@phonepe.com";

        EmailClassificationService.ClassificationResult result = classificationService.classify(subject, body, sender);

        assertTrue(result.isJobRelated());
        assertEquals(ApplicationStatus.UNDER_REVIEW, result.detectedStatus());
        assertTrue(result.confidence() >= 0.85);
    }

    @Test
    @DisplayName("Should detect ASSESSMENT_RECEIVED from coding test invite")
    void testAssessmentClassification() {
        String subject = "Invitation to online assessment - Software Engineer";
        String body = "Please complete the HackerRank coding test within 48 hours.";
        String sender = "no-reply@hackerrank.net";

        EmailClassificationService.ClassificationResult result = classificationService.classify(subject, body, sender);

        assertTrue(result.isJobRelated());
        assertEquals(ApplicationStatus.ASSESSMENT_RECEIVED, result.detectedStatus());
    }

    @Test
    @DisplayName("Should ignore spam, sales promos, and weekly newsletters")
    void testIgnoreSpamAndNewsletters() {
        String subject = "Special Discount: 50% off all dev courses";
        String body = "Don't miss our weekly newsletter and special promotion. Click here to unsubscribe.";
        String sender = "promotions@techsales.com";

        EmailClassificationService.ClassificationResult result = classificationService.classify(subject, body, sender);

        assertFalse(result.isJobRelated());
        assertNull(result.detectedStatus());
    }
}
