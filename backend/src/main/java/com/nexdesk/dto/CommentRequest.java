package com.nexdesk.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentRequest {

    private Long ticketId; // optional if in URL path

    @NotBlank(message = "Message cannot be empty")
    private String message;

    private Boolean isInternal = false;

    public CommentRequest() {
    }

    public CommentRequest(String message, Boolean isInternal) {
        this.message = message;
        this.isInternal = isInternal;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsInternal() {
        return isInternal;
    }

    public void setIsInternal(Boolean internal) {
        isInternal = internal;
    }
}
