package com.jobagent;

import com.jobagent.entity.Job;
import com.jobagent.service.EligibilityFilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EligibilityFilterTest {

    private EligibilityFilterService filterService;

    @BeforeEach
    void setUp() {
        filterService = new EligibilityFilterService();
    }

    @Test
    @DisplayName("Should accept valid 2026 batch fresher job in India")
    void testAcceptEligible2026JobInIndia() {
        Job job = new Job();
        job.setTitle("Associate Software Engineer - Java");
        job.setLocation("Bangalore, India");
        job.setCountry("India");
        job.setDescription("Open for 2026 batch graduates and freshers. 0 years experience.");
        job.setMinExperienceYears(0.0);
        job.setMaxExperienceYears(1.0);
        job.setTargetGraduationYear(2026);

        EligibilityFilterService.EligibilityVerdict verdict = filterService.evaluate(job);
        assertTrue(verdict.eligible());
        assertFalse(verdict.manualReviewRequired());
    }

    @Test
    @DisplayName("HARD FILTER: Should reject job located in USA or foreign countries")
    void testRejectForeignLocation() {
        Job job = new Job();
        job.setTitle("Junior Software Engineer");
        job.setLocation("Seattle, WA, USA");
        job.setCountry("USA");
        job.setDescription("Entry level position for freshers.");
        job.setMinExperienceYears(0.0);
        job.setMaxExperienceYears(1.0);

        EligibilityFilterService.EligibilityVerdict verdict = filterService.evaluate(job);
        assertFalse(verdict.eligible());
        assertTrue(verdict.reason().contains("outside India"));
    }

    @Test
    @DisplayName("HARD FILTER: Should reject job requiring 2+ years of experience or Senior titles")
    void testRejectSeniorExperiencedRole() {
        Job job = new Job();
        job.setTitle("Senior Java Backend Engineer");
        job.setLocation("Hyderabad, India");
        job.setCountry("India");
        job.setDescription("Requires 4+ years of experience leading teams.");
        job.setMinExperienceYears(4.0);
        job.setMaxExperienceYears(6.0);

        EligibilityFilterService.EligibilityVerdict verdict = filterService.evaluate(job);
        assertFalse(verdict.eligible());
        assertTrue(verdict.reason().contains("Senior role") || verdict.reason().contains("exceeds fresher limit"));
    }

    @Test
    @DisplayName("HARD FILTER: Should reject job posted in 2026 but restricting to 2024 batch")
    void testRejectJobRestrictedToPastBatch() {
        Job job = new Job();
        job.setTitle("Graduate Trainee");
        job.setLocation("Chennai, India");
        job.setCountry("India");
        job.setDescription("Job posted September 2026. Only 2024 batch graduates eligible.");
        job.setMinExperienceYears(0.0);
        job.setMaxExperienceYears(1.0);
        job.setTargetGraduationYear(2024);

        EligibilityFilterService.EligibilityVerdict verdict = filterService.evaluate(job);
        assertFalse(verdict.eligible());
        assertTrue(verdict.reason().contains("earlier graduation batch"));
    }
}
