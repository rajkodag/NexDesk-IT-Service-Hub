package com.nexdesk.dto;

import java.util.Map;

public class TicketResponse {

    private Long ticketId;
    private Long ticket_id; // legacy frontend alias
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;

    private Long userId;
    private Long createdBy; // legacy alias
    private String userName;
    private String user_name; // legacy alias
    private String userEmail;

    private Long technicianId;
    private Long assignedTo; // legacy alias
    private String technicianName;
    private String technicianEmail;

    private String createdAt;
    private String updatedAt;
    private String dueAt;
    private String resolvedAt;

    private Long slaTargetHours;
    private String slaDeadline;
    private Long remainingSlaMinutes;
    private Boolean isSlaBreached;

    private Map<String, Object> diagnostics;
    private Integer impactScore;

    public TicketResponse() {
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
        this.ticket_id = ticketId;
    }

    public Long getTicket_id() {
        return ticket_id;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
        this.createdBy = userId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        this.user_name = userName;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
        this.assignedTo = technicianId;
    }

    public Long getAssignedTo() {
        return assignedTo;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public String getTechnicianEmail() {
        return technicianEmail;
    }

    public void setTechnicianEmail(String technicianEmail) {
        this.technicianEmail = technicianEmail;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDueAt() {
        return dueAt;
    }

    public void setDueAt(String dueAt) {
        this.dueAt = dueAt;
    }

    public String getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(String resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Long getSlaTargetHours() {
        return slaTargetHours;
    }

    public void setSlaTargetHours(Long slaTargetHours) {
        this.slaTargetHours = slaTargetHours;
    }

    public String getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(String slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    public Long getRemainingSlaMinutes() {
        return remainingSlaMinutes;
    }

    public void setRemainingSlaMinutes(Long remainingSlaMinutes) {
        this.remainingSlaMinutes = remainingSlaMinutes;
    }

    public Boolean getIsSlaBreached() {
        return isSlaBreached;
    }

    public void setIsSlaBreached(Boolean slaBreached) {
        isSlaBreached = slaBreached;
    }

    public Map<String, Object> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(Map<String, Object> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public Integer getImpactScore() {
        return impactScore;
    }

    public void setImpactScore(Integer impactScore) {
        this.impactScore = impactScore;
    }
}
