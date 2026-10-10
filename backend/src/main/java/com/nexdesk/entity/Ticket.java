package com.nexdesk.entity;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "NX_TICKETS")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nx_ticket_seq")
    @SequenceGenerator(name = "nx_ticket_seq", sequenceName = "NX_TICKET_SEQ", allocationSize = 1)
    @Column(name = "TICKET_ID")
    private Long ticketId;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "DESCRIPTION", nullable = false, length = 2000)
    private String description;

    @Column(name = "CATEGORY", length = 50)
    private String category;

    @Column(name = "PRIORITY", length = 20)
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "STATUS", length = 20)
    private String status; // OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED, REOPENED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CREATED_BY", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ASSIGNED_TO")
    private Technician technician;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "DUE_AT")
    private LocalDateTime dueAt;

    @Column(name = "RESOLVED_AT")
    private LocalDateTime resolvedAt;

    @Column(name = "DIAGNOSTICS_DATA", length = 2000)
    private String diagnostics;

    @Column(name = "IMPACT_SCORE")
    private Integer impactScore;

    public Ticket() {
    }

    public Ticket(String title, String description, String category, String priority, User user) {
        this.title = title;
        this.description = description;
        this.category = category != null ? category : "General";
        this.priority = priority != null ? priority : "MEDIUM";
        this.status = "OPEN";
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "OPEN";
        }
        if (priority == null) {
            priority = "MEDIUM";
        }
        if (category == null) {
            category = "General";
        }
        calculateDueAt();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (dueAt == null) {
            calculateDueAt();
        }
        if (("RESOLVED".equalsIgnoreCase(status) || "CLOSED".equalsIgnoreCase(status)) && resolvedAt == null) {
            resolvedAt = LocalDateTime.now();
        }
    }

    public void calculateDueAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        long hours;
        String p = priority != null ? priority.toUpperCase() : "MEDIUM";
        switch (p) {
            case "CRITICAL":
                hours = 2;
                break;
            case "HIGH":
                hours = 4;
                break;
            case "MEDIUM":
                hours = 8;
                break;
            case "LOW":
            default:
                hours = 24;
                break;
        }
        this.dueAt = createdAt.plusHours(hours);
    }

    @Transient
    public boolean isSlaBreached() {
        if ("RESOLVED".equalsIgnoreCase(status) || "CLOSED".equalsIgnoreCase(status)) {
            if (resolvedAt != null && dueAt != null) {
                return resolvedAt.isAfter(dueAt);
            }
            return false;
        }
        return dueAt != null && LocalDateTime.now().isAfter(dueAt);
    }

    @Transient
    public long getRemainingSlaMinutes() {
        if (dueAt == null) return 0;
        LocalDateTime target = ("RESOLVED".equalsIgnoreCase(status) || "CLOSED".equalsIgnoreCase(status)) && resolvedAt != null
                ? resolvedAt : LocalDateTime.now();
        return Duration.between(target, dueAt).toMinutes();
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Technician getTechnician() {
        return technician;
    }

    public void setTechnician(Technician technician) {
        this.technician = technician;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(String diagnostics) {
        this.diagnostics = diagnostics;
    }

    public Integer getImpactScore() {
        return impactScore;
    }

    public void setImpactScore(Integer impactScore) {
        this.impactScore = impactScore;
    }
}
