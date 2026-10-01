package com.jobagent.dto;

import java.time.LocalDateTime;

public class EmailEventDto {
    private Long id;
    private Long userId;
    private Long applicationId;
    private String company;
    private String jobTitle;
    private String messageId;
    private String threadId;
    private String sender;
    private String subject;
    private String snippet;
    private String detectedStatus;
    private Double confidenceScore;
    private String matchRationale;
    private LocalDateTime receivedAt;
    private Boolean isProcessed;

    public EmailEventDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public String getDetectedStatus() { return detectedStatus; }
    public void setDetectedStatus(String detectedStatus) { this.detectedStatus = detectedStatus; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getMatchRationale() { return matchRationale; }
    public void setMatchRationale(String matchRationale) { this.matchRationale = matchRationale; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public Boolean getIsProcessed() { return isProcessed; }
    public void setIsProcessed(Boolean isProcessed) { this.isProcessed = isProcessed; }
}
