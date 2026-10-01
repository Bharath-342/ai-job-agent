package com.jobagent.service;

import com.jobagent.dto.CandidateProfileDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeParsingService {

    private static final Logger log = LoggerFactory.getLogger(ResumeParsingService.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6})");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:(?:\\+?91[\\-\\s]?)?[6-9]\\d{9}|\\(?\\d{3}\\)?[\\-\\s]?\\d{3}[\\-\\s]?\\d{4})");
    private static final Pattern GRAD_YEAR_PATTERN = Pattern.compile("\\b(202[4-9])\\b");
    private static final Pattern LINK_PATTERN = Pattern.compile("(https?://[^\\s]+|(?:github|linkedin|leetcode)\\.com/[^\\s]+)", Pattern.CASE_INSENSITIVE);

    private static final List<String> KNOWN_SKILLS = Arrays.asList(
        "Java", "Spring", "Spring Boot", "Hibernate", "JPA", "REST API", "Microservices",
        "Python", "Django", "Flask", "C", "C++", "JavaScript", "TypeScript", "React",
        "Angular", "Node.js", "Express", "HTML", "HTML5", "CSS", "Tailwind CSS",
        "SQL", "PostgreSQL", "MySQL", "MongoDB", "Redis", "Oracle",
        "Git", "GitHub", "Docker", "Kubernetes", "AWS", "Azure", "CI/CD",
        "Linux", "Maven", "Gradle", "Postman", "JUnit", "Data Structures", "Algorithms"
    );

    public String extractText(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        try (InputStream inputStream = file.getInputStream()) {
            if (filename.endsWith(".pdf")) {
                try (PDDocument document = PDDocument.load(inputStream)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(document);
                }
            } else if (filename.endsWith(".docx") || filename.endsWith(".doc")) {
                try (XWPFDocument doc = new XWPFDocument(inputStream)) {
                    StringBuilder sb = new StringBuilder();
                    for (XWPFParagraph p : doc.getParagraphs()) {
                        sb.append(p.getText()).append("\n");
                    }
                    return sb.toString();
                }
            } else {
                return new String(file.getBytes());
            }
        }
    }

    public CandidateProfileDto parseResume(MultipartFile file) {
        CandidateProfileDto dto = new CandidateProfileDto();
        dto.setActiveResumeFilename(file.getOriginalFilename());

        try {
            String text = extractText(file);
            log.info("Extracted {} characters of text from resume {}", text.length(), file.getOriginalFilename());

            // 1. Email extraction
            Matcher emailMatcher = EMAIL_PATTERN.matcher(text);
            if (emailMatcher.find()) {
                dto.setEmail(emailMatcher.group(1).trim());
            }

            // 2. Phone extraction
            Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
            if (phoneMatcher.find()) {
                dto.setPhone(phoneMatcher.group(0).trim());
            }

            // 3. Name extraction (from first 3 lines or before contact info)
            String[] lines = text.split("\\r?\\n");
            for (String line : lines) {
                String clean = line.trim();
                if (!clean.isEmpty() && !clean.contains("@") && !clean.matches(".*\\d{5,}.*") && clean.length() < 50) {
                    dto.setFullName(clean);
                    break;
                }
            }
            if (dto.getFullName() == null || dto.getFullName().isEmpty()) {
                dto.setFullName("Candidate");
            }

            // 4. Degree & Education extraction
            String lowerText = text.toLowerCase();
            if (lowerText.contains("b.tech") || lowerText.contains("btech") || lowerText.contains("bachelor of technology")) {
                dto.setDegree("Bachelor of Technology (B.Tech)");
            } else if (lowerText.contains("b.e") || lowerText.contains("bachelor of engineering")) {
                dto.setDegree("Bachelor of Engineering (B.E)");
            } else if (lowerText.contains("m.tech") || lowerText.contains("master of technology")) {
                dto.setDegree("Master of Technology (M.Tech)");
            } else if (lowerText.contains("mca")) {
                dto.setDegree("Master of Computer Applications (MCA)");
            } else if (lowerText.contains("bca") || lowerText.contains("b.sc")) {
                dto.setDegree("Bachelor of Computer Applications / Science");
            } else {
                dto.setDegree("B.Tech / Bachelor's in Engineering");
            }

            // 5. Graduation Year extraction
            Matcher gradMatcher = GRAD_YEAR_PATTERN.matcher(text);
            int gradYear = 2026; // Default targeted fresher batch
            while (gradMatcher.find()) {
                int y = Integer.parseInt(gradMatcher.group(1));
                if (y >= 2024 && y <= 2028) {
                    gradYear = y;
                }
            }
            dto.setGraduationYear(gradYear);
            dto.setExperienceYears(0.0); // Fresher default

            // 6. Skills extraction
            Set<String> matchedSkills = new LinkedHashSet<>();
            for (String skill : KNOWN_SKILLS) {
                Pattern skillPattern = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b", Pattern.CASE_INSENSITIVE);
                if (skillPattern.matcher(text).find()) {
                    matchedSkills.add(skill);
                }
            }
            dto.setSkills(new ArrayList<>(matchedSkills));

            // 7. Links extraction
            List<String> links = new ArrayList<>();
            Matcher linkMatcher = LINK_PATTERN.matcher(text);
            while (linkMatcher.find()) {
                String link = linkMatcher.group(1).trim();
                if (!links.contains(link)) {
                    links.add(link);
                }
            }
            dto.setLinks(links);

            // 8. Target roles inference
            List<String> roles = new ArrayList<>();
            if (matchedSkills.contains("Java")) {
                roles.add("Java Developer");
                roles.add("Java Backend Developer");
                roles.add("Software Engineer - Java");
            }
            if (matchedSkills.contains("React") || matchedSkills.contains("Node.js")) {
                roles.add("Full Stack Developer");
            }
            roles.add("Backend Developer");
            roles.add("Software Engineer");
            roles.add("Associate Software Engineer");
            roles.add("Graduate Engineer Trainee");
            dto.setTargetRoles(roles);

            // 9. Default target Indian locations
            dto.setPreferredLocations(Arrays.asList(
                "Hyderabad", "Bangalore", "Pune", "Chennai", "Noida", "Gurugram", "Remote - India"
            ));

            // 10. Education description
            dto.setEducation(dto.getDegree() + " (" + dto.getGraduationYear() + " Batch)");

        } catch (Exception e) {
            log.error("Failed to parse resume file", e);
        }

        return dto;
    }
}
