package com.nexdesk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NX_AUDIT_LOGS")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nx_audit_seq")
    @SequenceGenerator(name = "nx_audit_seq", sequenceName = "NX_AUDIT_SEQ", allocationSize = 1)
    @Column(name = "LOG_ID")
    private Long logId;

    @Column(name = "TICKET_ID")
    private Long ticketId;

    @Column(name = "ACTION", nullable = false, length = 100)
    private String action;

    @Column(name = "PERFORMED_BY", nullable = false, length = 100)
    private String performedBy;

    @Column(name = "USER_ROLE", length = 30)
    private String role;

    @Column(name = "DETAILS", length = 2000)
    private String details;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime timestamp;

    public AuditLog() {
    }

    public AuditLog(Long ticketId, String action, String performedBy, String role, String details) {
        this.ticketId = ticketId;
        this.action = action;
        this.performedBy = performedBy;
        this.role = role;
        this.details = details;
    }

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
