package com.nexdesk.dto;

import jakarta.validation.constraints.NotBlank;

public class StatusUpdateRequest {

    private Long ticketId; // optional if passed in path

    @NotBlank(message = "Status is required")
    private String status;

    private String notes;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(String status) {
        this.status = status;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
