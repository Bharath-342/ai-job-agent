package com.jobagent.dto;

import jakarta.validation.constraints.NotNull;

public class ApplyRequestDto {
    @NotNull(message = "Job ID is required")
    private Long jobId;

    private boolean forceManual;
    private String customNotes;

    public ApplyRequestDto() {}

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public boolean isForceManual() { return forceManual; }
    public void setForceManual(boolean forceManual) { this.forceManual = forceManual; }

    public String getCustomNotes() { return customNotes; }
    public void setCustomNotes(String customNotes) { this.customNotes = customNotes; }
}
