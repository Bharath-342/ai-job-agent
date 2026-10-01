package com.jobagent.service;

import com.jobagent.config.JsonUtils;
import com.jobagent.dto.CandidateProfileDto;
import com.jobagent.dto.JobMatchResultDto;
import com.jobagent.entity.CandidateProfile;
import com.jobagent.entity.Job;
import com.jobagent.entity.UserSettings;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JobMatchingService {

    private final EligibilityFilterService eligibilityFilterService;

    public JobMatchingService(EligibilityFilterService eligibilityFilterService) {
        this.eligibilityFilterService = eligibilityFilterService;
    }

    public JobMatchResultDto matchJob(Job job, CandidateProfile profile, UserSettings settings) {
        JobMatchResultDto result = new JobMatchResultDto();
        result.setJobId(job.getId());
        result.setJobKey(job.getJobKey());
        result.setCompany(job.getCompany());
        result.setTitle(job.getTitle());
        result.setLocation(job.getLocation());
        result.setJobUrl(job.getJobUrl());
        result.setApplicationMethod(job.getApplicationMethod());

        // 1. Run Strict Eligibility Verification
        EligibilityFilterService.EligibilityVerdict verdict = eligibilityFilterService.evaluate(job);
        result.setEligibilityReason(verdict.reason());
        result.setManualReviewRequired(verdict.manualReviewRequired());

        if (!verdict.eligible() && !verdict.manualReviewRequired()) {
            result.setEligible(false);
            result.setMatchScore(20); // Low score for non-eligible
            return result;
        }

        // 2. Skill Matching
        List<String> requiredSkills = JsonUtils.toStringList(job.getRequiredSkillsJson());
        List<String> preferredSkills = JsonUtils.toStringList(job.getPreferredSkillsJson());
        List<String> candidateSkills = JsonUtils.toStringList(profile.getSkillsJson());

        Set<String> matched = new HashSet<>();
        List<String> missingReq = new ArrayList<>();
        List<String> missingPref = new ArrayList<>();

        for (String req : requiredSkills) {
            boolean found = candidateSkills.stream().anyMatch(s -> s.equalsIgnoreCase(req) || req.toLowerCase().contains(s.toLowerCase()));
            if (found) {
                matched.add(req);
            } else {
                missingReq.add(req);
            }
        }

        for (String pref : preferredSkills) {
            boolean found = candidateSkills.stream().anyMatch(s -> s.equalsIgnoreCase(pref) || pref.toLowerCase().contains(s.toLowerCase()));
            if (found) {
                matched.add(pref);
            } else {
                missingPref.add(pref);
            }
        }

        result.setMatchedSkills(new ArrayList<>(matched));
        result.setMissingSkills(missingReq);
        result.setMissingPreferredSkills(missingPref);

        double skillScore = 100.0;
        if (!requiredSkills.isEmpty()) {
            skillScore = ((double) matched.size() / requiredSkills.size()) * 100.0;
            if (skillScore > 100.0) skillScore = 100.0;
        }

        // 3. Role Matching
        List<String> targetRoles = JsonUtils.toStringList(profile.getTargetRolesJson());
        String titleLower = job.getTitle().toLowerCase();
        boolean roleMatched = targetRoles.stream().anyMatch(r -> titleLower.contains(r.toLowerCase()) || r.toLowerCase().contains(titleLower));
        result.setRoleMatch(roleMatched);
        double roleScore = roleMatched ? 100.0 : 40.0;

        // 4. Experience Matching
        boolean expMatched = (job.getMinExperienceYears() == null || job.getMinExperienceYears() <= 1.0)
            && (job.getMaxExperienceYears() == null || job.getMaxExperienceYears() <= 2.0);
        result.setExperienceMatch(expMatched);
        double expScore = expMatched ? 100.0 : 30.0;

        // 5. Location Matching
        List<String> preferredLocations = JsonUtils.toStringList(profile.getPreferredLocationsJson());
        String jobLocLower = job.getLocation().toLowerCase();
        boolean locMatched = preferredLocations.stream().anyMatch(loc -> jobLocLower.contains(loc.toLowerCase()));
        result.setLocationMatch(locMatched);
        double locScore = locMatched ? 100.0 : 70.0; // In India is still viable

        // 6. Graduation Year Match
        boolean gradMatched = (job.getTargetGraduationYear() == null || job.getTargetGraduationYear().equals(profile.getGraduationYear()));
        result.setGraduationMatch(gradMatched);

        // Weighted Overall Score (Skill: 45%, Role: 25%, Exp: 15%, Loc: 15%)
        int overallScore = (int) Math.round((skillScore * 0.45) + (roleScore * 0.25) + (expScore * 0.15) + (locScore * 0.15));
        if (overallScore > 100) overallScore = 100;
        if (overallScore < 0) overallScore = 0;

        result.setMatchScore(overallScore);

        int minScore = settings != null && settings.getMinMatchScore() != null ? settings.getMinMatchScore() : 85;
        result.setEligible(verdict.eligible() && overallScore >= minScore);

        return result;
    }
}
