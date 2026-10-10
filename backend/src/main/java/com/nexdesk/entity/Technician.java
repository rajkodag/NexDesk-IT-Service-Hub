package com.nexdesk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NX_TECHNICIANS")
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nx_tech_seq")
    @SequenceGenerator(name = "nx_tech_seq", sequenceName = "NX_TECH_SEQ", allocationSize = 1)
    @Column(name = "TECH_ID")
    private Long technicianId;

    @Column(name = "TECH_NAME", nullable = false, length = 100)
    private String fullName;

    @Column(name = "EMAIL", unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "SPECIALIZATION", length = 100)
    private String specialization;

    @Column(name = "STATUS", length = 20)
    private String status; // AVAILABLE, BUSY, OFFLINE

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    public Technician() {
    }

    public Technician(String fullName, String email, String specialization, String status) {
        this.fullName = fullName;
        this.email = email;
        this.specialization = specialization;
        this.status = status != null ? status : "AVAILABLE";
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "AVAILABLE";
        }
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
