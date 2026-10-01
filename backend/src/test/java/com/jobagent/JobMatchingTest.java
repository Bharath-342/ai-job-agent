package com.jobagent;

import com.jobagent.config.JsonUtils;
import com.jobagent.dto.JobMatchResultDto;
import com.jobagent.entity.CandidateProfile;
import com.jobagent.entity.Job;
import com.jobagent.entity.UserSettings;
import com.jobagent.service.EligibilityFilterService;
import com.jobagent.service.JobMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class JobMatchingTest {

    private JobMatchingService matchingService;

    @BeforeEach
    void setUp() {
        EligibilityFilterService filterService = new EligibilityFilterService();
        matchingService = new JobMatchingService(filterService);
    }

    @Test
    @DisplayName("Should score high (>85%) when candidate skills and roles match job criteria")
    void testHighMatchingScore() {
        Job job = new Job();
        job.setId(1L);
        job.setJobKey("job-1");
        job.setCompany("Razorpay");
        job.setTitle("Java Developer");
        job.setLocation("Hyderabad, India");
        job.setCountry("India");
        job.setDescription("Hiring 2026 batch freshers. Java, Spring Boot, SQL required.");
        job.setRequiredSkillsJson(JsonUtils.toJson(Arrays.asList("Java", "Spring Boot", "SQL")));
        job.setPreferredSkillsJson(JsonUtils.toJson(Arrays.asList("PostgreSQL")));
        job.setMinExperienceYears(0.0);
        job.setMaxExperienceYears(1.0);
        job.setTargetGraduationYear(2026);

        CandidateProfile profile = new CandidateProfile();
        profile.setFullName("Amar");
        profile.setGraduationYear(2026);
        profile.setExperienceYears(0.0);
        profile.setSkillsJson(JsonUtils.toJson(Arrays.asList("Java", "Spring Boot", "SQL", "PostgreSQL", "Git")));
        profile.setTargetRolesJson(JsonUtils.toJson(Arrays.asList("Java Developer", "Backend Developer")));
        profile.setPreferredLocationsJson(JsonUtils.toJson(Arrays.asList("Hyderabad", "Bangalore")));

        UserSettings settings = new UserSettings();
        settings.setMinMatchScore(85);

        JobMatchResultDto match = matchingService.matchJob(job, profile, settings);

        assertTrue(match.getMatchScore() >= 85, "Expected match score >= 85 but was " + match.getMatchScore());
        assertTrue(match.isEligible());
        assertTrue(match.getMatchedSkills().contains("Java"));
        assertTrue(match.getMissingSkills().isEmpty());
    }

    @Test
    @DisplayName("Should score low when essential skills are missing")
    void testLowSkillMatchScore() {
        Job job = new Job();
        job.setId(2L);
        job.setJobKey("job-2");
        job.setCompany("TechCorp");
        job.setTitle("Python Data Engineer");
        job.setLocation("Pune, India");
        job.setCountry("India");
        job.setDescription("Hiring 2026 freshers with Python, Pandas, Spark skills.");
        job.setRequiredSkillsJson(JsonUtils.toJson(Arrays.asList("Python", "Pandas", "Spark", "Airflow")));
        job.setMinExperienceYears(0.0);
        job.setMaxExperienceYears(1.0);
        job.setTargetGraduationYear(2026);

        CandidateProfile profile = new CandidateProfile();
        profile.setGraduationYear(2026);
        profile.setSkillsJson(JsonUtils.toJson(Arrays.asList("Java", "Spring Boot")));
        profile.setTargetRolesJson(JsonUtils.toJson(Arrays.asList("Java Developer")));
        profile.setPreferredLocationsJson(JsonUtils.toJson(Arrays.asList("Pune")));

        UserSettings settings = new UserSettings();
        settings.setMinMatchScore(85);

        JobMatchResultDto match = matchingService.matchJob(job, profile, settings);

        assertTrue(match.getMatchScore() < 85);
        assertFalse(match.isEligible());
        assertFalse(match.getMissingSkills().isEmpty());
    }
}
