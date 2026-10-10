package com.nexdesk.dto;

public class TechnicianResponse {

    private Long technicianId;
    private Long technician_id; // legacy frontend alias
    private String fullName;
    private String full_name; // legacy frontend alias
    private String email;
    private String specialization;
    private String status;
    private String createdAt;

    public TechnicianResponse() {
    }

    public TechnicianResponse(Long technicianId, String fullName, String email, String specialization, String status, String createdAt) {
        this.technicianId = technicianId;
        this.technician_id = technicianId;
        this.fullName = fullName;
        this.full_name = fullName;
        this.email = email;
        this.specialization = specialization;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
        this.technician_id = technicianId;
    }

    public Long getTechnician_id() {
        return technician_id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        this.full_name = fullName;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
