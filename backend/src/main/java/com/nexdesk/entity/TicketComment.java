package com.nexdesk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NX_TICKET_COMMENTS")
public class TicketComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nx_comment_seq")
    @SequenceGenerator(name = "nx_comment_seq", sequenceName = "NX_COMMENT_SEQ", allocationSize = 1)
    @Column(name = "COMMENT_ID")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TICKET_ID", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "AUTHOR_NAME", length = 100)
    private String authorName;

    @Column(name = "AUTHOR_ROLE", length = 20)
    private String authorRole;

    @Column(name = "MESSAGE", nullable = false, length = 2000)
    private String message;

    @Column(name = "IS_INTERNAL")
    private Boolean isInternal = false;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    public TicketComment() {
    }

    public TicketComment(Ticket ticket, User user, String authorName, String authorRole, String message, Boolean isInternal) {
        this.ticket = ticket;
        this.user = user;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.message = message;
        this.isInternal = isInternal != null ? isInternal : false;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isInternal == null) {
            isInternal = false;
        }
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
