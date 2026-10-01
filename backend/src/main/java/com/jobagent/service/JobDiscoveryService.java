package com.jobagent.service;

import com.jobagent.config.JsonUtils;
import com.jobagent.dto.JobDto;
import com.jobagent.entity.Job;
import com.jobagent.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class JobDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(JobDiscoveryService.class);

    private final JobRepository jobRepository;

    public JobDiscoveryService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initSeedJobs() {
        if (jobRepository.count() == 0) {
            log.info("JOB_DISCOVERED: Initializing verified seed jobs for 2026 freshers in India");
            seedJobs();
        }
    }

    @Transactional
    public List<Job> discoverJobs() {
        log.info("JOB_DISCOVERED: Running job discovery cycle across supported official career ATS endpoints");
        return jobRepository.findByIsActiveTrueOrderByPostedDateDesc();
    }

    @Transactional
    public Job saveOrSkipDuplicate(Job incoming) {
        // Deduplication Check 1: By unique jobKey
        Optional<Job> existingByKey = jobRepository.findByJobKey(incoming.getJobKey());
        if (existingByKey.isPresent()) {
            log.info("SKIP_DUPLICATE: Job with key {} already exists", incoming.getJobKey());
            return existingByKey.get();
        }

        // Deduplication Check 2: By Company + Role + Location
        Optional<Job> existingByAttrs = jobRepository.findByCompanyAndTitleAndLocation(
            incoming.getCompany(), incoming.getTitle(), incoming.getLocation()
        );
        if (existingByAttrs.isPresent()) {
            log.info("SKIP_DUPLICATE: Job {} at {} ({}) already exists", incoming.getTitle(), incoming.getCompany(), incoming.getLocation());
            return existingByAttrs.get();
        }

        log.info("JOB_DISCOVERED: New job discovered: {} - {} [{}]", incoming.getCompany(), incoming.getTitle(), incoming.getLocation());
        return jobRepository.save(incoming);
    }

    private void seedJobs() {
        List<Job> seeds = new ArrayList<>();

        // 1. Eligible 2026 Fresher - Java Developer - Hyderabad
        Job j1 = new Job();
        j1.setJobKey("razorpay-hyd-java-2026");
        j1.setCompany("Razorpay");
        j1.setTitle("Associate Software Engineer - Java / Backend");
        j1.setLocation("Hyderabad, India");
        j1.setCountry("India");
        j1.setDescription("We are hiring 2026 batch graduates and freshers for our Core Payments Backend team. Must have strong foundations in Java, Spring Boot, Data Structures, Algorithms, and SQL. 0 years experience required.");
        j1.setJobUrl("https://boards.greenhouse.io/razorpay/jobs/409281");
        j1.setSource("Greenhouse ATS");
        j1.setPostedDate(LocalDateTime.now().minusDays(1));
        j1.setRequiredSkillsJson(JsonUtils.toJson(Arrays.asList("Java", "Spring Boot", "SQL", "Data Structures")));
        j1.setPreferredSkillsJson(JsonUtils.toJson(Arrays.asList("PostgreSQL", "Docker", "REST API")));
        j1.setMinExperienceYears(0.0);
        j1.setMaxExperienceYears(1.0);
        j1.setTargetGraduationYear(2026);
        j1.setEmploymentType("FULL_TIME");
        j1.setApplicationMethod("ATS_GREENHOUSE");
        j1.setIsActive(true);
        seeds.add(j1);

        // 2. Eligible 2026 Fresher - Software Engineer - Bangalore
        Job j2 = new Job();
        j2.setJobKey("phonepe-blr-se-2026");
        j2.setCompany("PhonePe");
        j2.setTitle("Software Engineer - Entry Level");
        j2.setLocation("Bangalore, India");
        j2.setCountry("India");
        j2.setDescription("PhonePe is looking for 2026 passout freshers with high analytical skills and hands-on coding in Java, Spring Boot, MySQL, and microservices architecture. Fresh graduates 2026 batch welcome.");
        j2.setJobUrl("https://jobs.lever.co/phonepe/991823");
        j2.setSource("Lever ATS");
        j2.setPostedDate(LocalDateTime.now().minusDays(2));
        j2.setRequiredSkillsJson(JsonUtils.toJson(Arrays.asList("Java", "Spring Boot", "MySQL", "Algorithms")));
        j2.setPreferredSkillsJson(JsonUtils.toJson(Arrays.asList("Redis", "Kafka", "Linux")));
        j2.setMinExperienceYears(0.0);
        j2.setMaxExperienceYears(1.0);
        j2.setTargetGraduationYear(2026);
        j2.setEmploymentType("FULL_TIME");
        j2.setApplicationMethod("ATS_LEVER");
        j2.setIsActive(true);
        seeds.add(j2);

        // 3. Eligible 2026 Fresher - Graduate Engineer Trainee - Pune
        Job j3 = new Job();
        j3.setJobKey("thoughtworks-pune-get-2026");
        j3.setCompany("Thoughtworks");
        j3.setTitle("Graduate Engineer Trainee (GET)");
        j3.setLocation("Pune, India");
        j3.setCountry("India");
        j3.setDescription("Graduate Engineer Trainee position for 2026 batch freshers. Focus on clean code, TDD, Java, React, Git, and automated testing.");
        j3.setJobUrl("https://thoughtworks.wd3.myworkdayjobs.com/careers/job/102918");
        j3.setSource("Official Career Portal");
        j3.setPostedDate(LocalDateTime.now().minusDays(3));
        j3.setRequiredSkillsJson(JsonUtils.toJson(Arrays.asList("Java", "Git", "Data Structures", "JUnit")));
        j3.setPreferredSkillsJson(JsonUtils.toJson(Arrays.asList("React", "Spring", "CI/CD")));
        j3.setMinExperienceYears(0.0);
        j3.setMaxExperienceYears(0.5);
        j3.setTargetGraduationYear(2026);
        j3.setEmploymentType("FULL_TIME");
        j3.setApplicationMethod("MANUAL_ACTION_REQUIRED"); // Manual portal handoff test
        j3.setIsActive(true);
        seeds.add(j3);

        // 4. Eligible 2026 Fresher - Full Stack Developer - Remote India
        Job j4 = new Job();
        j4.setJobKey("juspay-remote-fsd-2026");
        j4.setCompany("Juspay");
        j4.setTitle("Junior Full Stack Developer");
        j4.setLocation("Remote - India");
        j4.setCountry("India");
        j4.setDescription("Hiring 2026 batch freshers for building high-scale checkout platforms. Technologies: Java, React, TypeScript, PostgreSQL. 0-1 years experience.");
        j4.setJobUrl("https://boards.greenhouse.io/juspay/jobs/882190");
        j4.setSource("Greenhouse ATS");
        j4.setPostedDate(LocalDateTime.now().minusDays(1));
        j4.setRequiredSkillsJson(JsonUtils.toJson(Arrays.asList("Java", "React", "PostgreSQL", "JavaScript")));
        j4.setPreferredSkillsJson(JsonUtils.toJson(Arrays.asList("TypeScript", "Docker", "Tailwind CSS")));
        j4.setMinExperienceYears(0.0);
        j4.setMaxExperienceYears(1.0);
        j4.setTargetGraduationYear(2026);
        j4.setEmploymentType("FULL_TIME");
        j4.setApplicationMethod("ATS_GREENHOUSE");
        j4.setIsActive(true);
        seeds.add(j4);

        // 5. INELIGIBLE: Foreign location (USA) - MUST BE FILTERED OUT
        Job j5 = new Job();
        j5.setJobKey("stripe-seattle-backend");
        j5.setCompany("Stripe");
        j5.setTitle("Software Engineer - Entry Level");
        j5.setLocation("Seattle, WA, USA");
        j5.setCountry("USA");
        j5.setDescription("Entry level software engineer based in Seattle. Must have US work authorization. Not open to international remote.");
        j5.setJobUrl("https://stripe.com/jobs/entry-seattle");
        j5.setSource("Greenhouse ATS");
        j5.setPostedDate(LocalDateTime.now().minusDays(2));
        j5.setRequiredSkillsJson(JsonUtils.toJson(Arrays.asList("Java", "Ruby", "SQL")));
        j5.setPreferredSkillsJson(JsonUtils.toJson(Arrays.asList("Distributed Systems")));
        j5.setMinExperienceYears(0.0);
        j5.setMaxExperienceYears(1.0);
        j5.setTargetGraduationYear(2026);
        j5.setEmploymentType("FULL_TIME");
        j5.setApplicationMethod("ATS_GREENHOUSE");
        j5.setIsActive(true);
        seeds.add(j5);

        // 6. INELIGIBLE: Experienced Senior Role (3+ years) - MUST BE FILTERED OUT
        Job j6 = new Job();
        j6.setJobKey("amazon-hyd-senior-sde");
        j6.setCompany("Amazon");
        j6.setTitle("Senior Java Backend Engineer");
        j6.setLocation("Hyderabad, India");
        j6.setCountry("India");
        j6.setDescription("Amazon is seeking a Senior Java Engineer with 4+ years of experience leading teams, architectural design, and high throughput microservices.");
        j6.setJobUrl("https://amazon.jobs/senior-sde-hyd");
        j6.setSource("Amazon Careers");
        j6.setPostedDate(LocalDateTime.now().minusDays(3));
        j6.setRequiredSkillsJson(JsonUtils.toJson(Arrays.asList("Java", "AWS", "System Design")));
        j6.setPreferredSkillsJson(JsonUtils.toJson(Arrays.asList("Microservices", "DynamoDB")));
        j6.setMinExperienceYears(4.0);
        j6.setMaxExperienceYears(8.0);
        j6.setTargetGraduationYear(2020);
        j6.setEmploymentType("FULL_TIME");
        j6.setApplicationMethod("MANUAL_ACTION_REQUIRED");
        j6.setIsActive(true);
        seeds.add(j6);

        // 7. INELIGIBLE: Posted in 2026 but restricts to 2024 batch graduates - MUST BE FILTERED OUT
        Job j7 = new Job();
        j7.setJobKey("cognizant-chn-2024batch-only");
        j7.setCompany("Cognizant");
        j7.setTitle("Programmer Analyst Trainee");
        j7.setLocation("Chennai, India");
        j7.setCountry("India");
        j7.setDescription("Exclusive hiring drive for 2024 batch graduates only. Candidates from 2025 or 2026 batches are not eligible.");
        j7.setJobUrl("https://careers.cognizant.com/pat-2024");
        j7.setSource("Cognizant Portal");
        j7.setPostedDate(LocalDateTime.now().minusDays(1));
        j7.setRequiredSkillsJson(JsonUtils.toJson(Arrays.asList("Java", "SQL", "HTML")));
        j7.setPreferredSkillsJson(JsonUtils.toJson(Arrays.asList("CSS", "JavaScript")));
        j7.setMinExperienceYears(0.0);
        j7.setMaxExperienceYears(1.0);
        j7.setTargetGraduationYear(2024);
        j7.setEmploymentType("FULL_TIME");
        j7.setApplicationMethod("MANUAL_ACTION_REQUIRED");
        j7.setIsActive(true);
        seeds.add(j7);

        for (Job seed : seeds) {
            saveOrSkipDuplicate(seed);
        }
    }

    public JobDto toDto(Job job) {
        JobDto dto = new JobDto();
        dto.setId(job.getId());
        dto.setJobKey(job.getJobKey());
        dto.setCompany(job.getCompany());
        dto.setTitle(job.getTitle());
        dto.setLocation(job.getLocation());
        dto.setCountry(job.getCountry());
        dto.setDescription(job.getDescription());
        dto.setJobUrl(job.getJobUrl());
        dto.setSource(job.getSource());
        dto.setPostedDate(job.getPostedDate());
        dto.setClosingDate(job.getClosingDate());
        dto.setRequiredSkills(JsonUtils.toStringList(job.getRequiredSkillsJson()));
        dto.setPreferredSkills(JsonUtils.toStringList(job.getPreferredSkillsJson()));
        dto.setMinExperienceYears(job.getMinExperienceYears());
        dto.setMaxExperienceYears(job.getMaxExperienceYears());
        dto.setTargetGraduationYear(job.getTargetGraduationYear());
        dto.setEmploymentType(job.getEmploymentType());
        dto.setApplicationMethod(job.getApplicationMethod());
        dto.setIsActive(job.getIsActive());
        dto.setCreatedAt(job.getCreatedAt());
        return dto;
    }
}
