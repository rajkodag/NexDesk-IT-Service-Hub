package com.nexdesk.service;

import com.nexdesk.dto.TechnicianRequest;
import com.nexdesk.dto.TechnicianResponse;
import com.nexdesk.entity.Technician;
import com.nexdesk.entity.User;
import com.nexdesk.exception.BadRequestException;
import com.nexdesk.exception.ResourceNotFoundException;
import com.nexdesk.repository.TechnicianRepository;
import com.nexdesk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public TechnicianService(TechnicianRepository technicianRepository,
                             UserRepository userRepository,
                             org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                             AuditLogService auditLogService) {
        this.technicianRepository = technicianRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public List<TechnicianResponse> getAll() {
        return technicianRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Technician getTechnicianEntity(Long id) {
        return technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + id));
    }

    public TechnicianResponse getById(Long id) {
        return toResponse(getTechnicianEntity(id));
    }

    @Transactional
    public TechnicianResponse create(TechnicianRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (technicianRepository.existsByEmail(email)) {
            throw new BadRequestException("Technician with email " + email + " already exists.");
        }

        String rawPassword = req.getPassword();
        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            AuthService.validatePasswordStrength(rawPassword);
        } else {
            rawPassword = "Tech@2026!"; // default enterprise initial password meeting complexity policy
        }

        // Also provision corresponding User login account if not present
        if (!userRepository.existsByEmail(email)) {
            User techUser = new User(
                    req.getFullName().trim(),
                    email,
                    passwordEncoder.encode(rawPassword),
                    "TECHNICIAN"
            );
            userRepository.save(techUser);
        }

        Technician t = new Technician(
                req.getFullName().trim(),
                email,
                req.getSpecialization() != null ? req.getSpecialization() : "General Support",
                req.getStatus() != null ? req.getStatus().toUpperCase() : "AVAILABLE"
        );
        Technician saved = technicianRepository.save(t);

        auditLogService.record(null, "TECHNICIAN_PROVISIONED", "Admin", "ADMIN",
                "Manually added technician: " + saved.getFullName() + " (" + saved.getSpecialization() + ")");

        return toResponse(saved);
    }

    @Transactional
    public TechnicianResponse updateStatus(Long id, String status) {
        Technician t = getTechnicianEntity(id);
        t.setStatus(status.toUpperCase());
        return toResponse(technicianRepository.save(t));
    }

    @Transactional
    public void delete(Long id) {
        if (!technicianRepository.existsById(id)) {
            throw new ResourceNotFoundException("Technician not found with ID: " + id);
        }
        technicianRepository.deleteById(id);
    }

    public TechnicianResponse toResponse(Technician t) {
        return new TechnicianResponse(
                t.getTechnicianId(),
                t.getFullName(),
                t.getEmail(),
                t.getSpecialization(),
                t.getStatus(),
                t.getCreatedAt() != null ? t.getCreatedAt().toString() : null
        );
    }
}
