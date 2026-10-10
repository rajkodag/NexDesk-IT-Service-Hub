package com.nexdesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class TechnicianRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    private String specialization;
    private String status;
    private String password;

    public TechnicianRequest() {
    }

    public TechnicianRequest(String fullName, String email, String specialization, String status) {
        this.fullName = fullName;
        this.email = email;
        this.specialization = specialization;
        this.status = status;
    }

    public TechnicianRequest(String fullName, String email, String specialization, String status, String password) {
        this.fullName = fullName;
        this.email = email;
        this.specialization = specialization;
        this.status = status;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
