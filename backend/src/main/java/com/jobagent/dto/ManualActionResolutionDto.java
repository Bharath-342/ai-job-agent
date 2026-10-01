package com.jobagent.dto;

import com.jobagent.entity.ApplicationStatus;

public class ManualActionResolutionDto {
    private ApplicationStatus resolutionStatus = ApplicationStatus.APPLIED;
    private String confirmationId;
    private String userNotes;

    public ManualActionResolutionDto() {}

    public ApplicationStatus getResolutionStatus() { return resolutionStatus; }
    public void setResolutionStatus(ApplicationStatus resolutionStatus) { this.resolutionStatus = resolutionStatus; }

    public String getConfirmationId() { return confirmationId; }
    public void setConfirmationId(String confirmationId) { this.confirmationId = confirmationId; }

    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }
}
