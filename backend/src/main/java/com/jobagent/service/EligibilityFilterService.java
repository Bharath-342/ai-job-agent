package com.jobagent.service;

import com.jobagent.entity.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EligibilityFilterService {

    private static final Logger log = LoggerFactory.getLogger(EligibilityFilterService.class);

    private static final List<String> ACCEPTED_INDIA_LOCATIONS = Arrays.asList(
        "hyderabad", "bangalore", "bengaluru", "chennai", "pune", "mumbai",
        "delhi", "delhi ncr", "noida", "gurugram", "gurgaon", "visakhapatnam",
        "vizag", "kolkata", "ahmedabad", "remote - india", "india-wide", "india", "pan india"
    );

    private static final List<String> REJECTED_COUNTRIES_LOCATIONS = Arrays.asList(
        "usa", "united states", "us only", "uk", "united kingdom", "london",
        "canada", "toronto", "vancouver", "australia", "sydney", "melbourne",
        "europe", "germany", "berlin", "singapore", "foreign"
    );

    private static final List<String> REJECTED_SENIORITY = Arrays.asList(
        "senior", "sr.", "lead", "principal", "manager", "director", "head of",
        "architect", "staff engineer", "vp"
    );

    private static final Pattern SENIORITY_YEARS_PATTERN = Pattern.compile("(\\b[2-9]|1[0-9])\\+?\\s*(?:years?|yrs?)(?:\\s+of)?\\s+experience", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAST_BATCH_PATTERN = Pattern.compile("\\b(202[0-4])\\s*(?:batch|passout|pass-out|graduates?)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern BATCH_2026_PATTERN = Pattern.compile("\\b(2026)\\s*(?:batch|passout|pass-out|graduates?|passouts?)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern FRESHER_PATTERN = Pattern.compile("\\b(fresher|freshers|fresh graduate|entry level|graduate trainee|get|0(?:-1)?\\s*years?)\\b", Pattern.CASE_INSENSITIVE);

    public record EligibilityVerdict(
        boolean eligible,
        boolean manualReviewRequired,
        String reason
    ) {}

    public EligibilityVerdict evaluate(Job job) {
        String title = job.getTitle() != null ? job.getTitle().toLowerCase() : "";
        String location = job.getLocation() != null ? job.getLocation().toLowerCase() : "";
        String country = job.getCountry() != null ? job.getCountry().toLowerCase() : "";
        String desc = job.getDescription() != null ? job.getDescription().toLowerCase() : "";

        // 1. HARD FILTER: Location & Country (India only)
        for (String rejectedLoc : REJECTED_COUNTRIES_LOCATIONS) {
            if (location.contains(rejectedLoc) || country.contains(rejectedLoc)) {
                return new EligibilityVerdict(false, false, "Rejected: Location outside India (" + rejectedLoc.toUpperCase() + ")");
            }
        }

        boolean hasIndiaLocation = "india".equals(country) || ACCEPTED_INDIA_LOCATIONS.stream().anyMatch(location::contains);
        if (!hasIndiaLocation) {
            return new EligibilityVerdict(false, true, "Manual Review Required: India location cannot be definitively verified");
        }

        // 2. HARD FILTER: Seniority & High Experience
        for (String seniorKeyword : REJECTED_SENIORITY) {
            // Check in title
            Pattern titleSeniorPattern = Pattern.compile("\\b" + Pattern.quote(seniorKeyword) + "\\b", Pattern.CASE_INSENSITIVE);
            if (titleSeniorPattern.matcher(title).find()) {
                return new EligibilityVerdict(false, false, "Rejected: Experienced/Senior role detected (" + seniorKeyword + ")");
            }
        }

        if (job.getMinExperienceYears() != null && job.getMinExperienceYears() >= 2.0) {
            return new EligibilityVerdict(false, false, "Rejected: Min experience required (" + job.getMinExperienceYears() + " yrs) exceeds fresher limit");
        }

        Matcher expMatcher = SENIORITY_YEARS_PATTERN.matcher(desc);
        if (expMatcher.find()) {
            return new EligibilityVerdict(false, false, "Rejected: Description requires " + expMatcher.group(0));
        }

        // 3. HARD FILTER: Non-2026 Batch Restrictions (e.g. "Only 2024 graduates eligible")
        if (job.getTargetGraduationYear() != null && job.getTargetGraduationYear() < 2026) {
            return new EligibilityVerdict(false, false, "Rejected: Job restricted to earlier graduation batch (" + job.getTargetGraduationYear() + ")");
        }

        Matcher pastBatchMatcher = PAST_BATCH_PATTERN.matcher(desc);
        boolean mentionsEligible2026Batch = BATCH_2026_PATTERN.matcher(desc).find();
        if (pastBatchMatcher.find() && !mentionsEligible2026Batch) {
            return new EligibilityVerdict(false, false, "Rejected: Job restricted to earlier graduation batch (" + pastBatchMatcher.group(0) + ")");
        }

        // 4. POSITIVE FRESHER / 2026 CHECKS (Do NOT confuse "Posted in 2026" with 2026 batch eligibility)
        boolean mentions2026 = mentionsEligible2026Batch || (job.getTargetGraduationYear() != null && job.getTargetGraduationYear() == 2026);
        boolean mentionsFresher = FRESHER_PATTERN.matcher(desc).find() || FRESHER_PATTERN.matcher(title).find() || (job.getMaxExperienceYears() != null && job.getMaxExperienceYears() <= 1.0);

        if (mentions2026) {
            return new EligibilityVerdict(true, false, "Eligible: Explicitly accepts 2026 batch graduates in India");
        }

        if (mentionsFresher) {
            return new EligibilityVerdict(true, false, "Eligible: Open to freshers / 0-1 year entry level in India");
        }

        // If neither explicitly affirmed nor rejected -> Set Manual Eligibility Review
        return new EligibilityVerdict(false, true, "Manual Review Required: Fresher or 2026 batch eligibility requires human confirmation");
    }
}
