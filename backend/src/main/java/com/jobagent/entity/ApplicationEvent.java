package com.jobagent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application_events")
public class ApplicationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status", nullable = false)
    private String newStatus;

    @Column(nullable = false)
    private String source;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime = LocalDateTime.now();

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    public ApplicationEvent() {}

    public ApplicationEvent(Application application, String eventType, String oldStatus, String newStatus, String source, String description) {
        this.application = application;
        this.eventType = eventType;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.source = source;
        this.description = description;
        this.eventTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

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

    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }
}
