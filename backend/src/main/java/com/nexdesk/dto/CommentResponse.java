package com.nexdesk.dto;

public class CommentResponse {

    private Long commentId;
    private Long ticketId;
    private Long userId;
    private String authorName;
    private String authorRole;
    private String message;
    private Boolean isInternal;
    private String createdAt;

    public CommentResponse() {
    }

    public CommentResponse(Long commentId, Long ticketId, Long userId, String authorName,
                           String authorRole, String message, Boolean isInternal, String createdAt) {
        this.commentId = commentId;
        this.ticketId = ticketId;
        this.userId = userId;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.message = message;
        this.isInternal = isInternal;
        this.createdAt = createdAt;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
