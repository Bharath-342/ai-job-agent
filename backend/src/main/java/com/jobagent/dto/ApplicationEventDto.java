package com.jobagent.dto;

import java.time.LocalDateTime;

public class ApplicationEventDto {
    private Long id;
    private Long applicationId;
    private String eventType;
    private String oldStatus;
    private String newStatus;
    private String source;
    private String description;
    private LocalDateTime eventTime;

    public ApplicationEventDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
}
