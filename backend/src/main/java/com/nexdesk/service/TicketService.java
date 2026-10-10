package com.nexdesk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexdesk.dto.TicketRequest;
import com.nexdesk.dto.TicketResponse;
import com.nexdesk.entity.Technician;
import com.nexdesk.entity.Ticket;
import com.nexdesk.entity.User;
import com.nexdesk.exception.BadRequestException;
import com.nexdesk.exception.ResourceNotFoundException;
import com.nexdesk.repository.TechnicianRepository;
import com.nexdesk.repository.TicketRepository;
import com.nexdesk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TechnicianRepository technicianRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         TechnicianRepository technicianRepository,
                         AuditLogService auditLogService,
                         ObjectMapper objectMapper) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.technicianRepository = technicianRepository;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TicketResponse createTicket(TicketRequest req, Long userId, String performedBy, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Ticket ticket = new Ticket();
        ticket.setTitle(req.getTitle().trim());
        ticket.setDescription(req.getDescription().trim());
        ticket.setCategory(req.getCategory() != null ? req.getCategory().trim() : "General");
        ticket.setPriority(req.getPriority() != null ? req.getPriority().trim().toUpperCase() : "MEDIUM");
        ticket.setStatus("OPEN");
        ticket.setUser(user);
        ticket.setImpactScore(req.getImpactScore());

        if (req.getDiagnostics() != null && !req.getDiagnostics().isEmpty()) {
            try {
                ticket.setDiagnostics(objectMapper.writeValueAsString(req.getDiagnostics()));
            } catch (JsonProcessingException ignored) {
            }
        }

        ticket.calculateDueAt();
        Ticket saved = ticketRepository.save(ticket);

        auditLogService.record(
                saved.getTicketId(),
                "TICKET_CREATED",
                performedBy != null ? performedBy : user.getFullName(),
                role != null ? role : user.getRole(),
                "Ticket created with priority " + saved.getPriority() + " and category " + saved.getCategory()
        );

        return toResponse(saved);
    }

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Ticket getTicketEntity(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + id));
    }

    public TicketResponse getTicketById(Long id) {
        return toResponse(getTicketEntity(id));
    }

    public List<TicketResponse> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TicketResponse> getTicketsByTechnician(Long techId) {
        return ticketRepository.findByTechnicianTechnicianIdOrderByCreatedAtDesc(techId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TicketResponse> filterTickets(String status, String priority, String category) {
        return ticketRepository.filterTickets(status, priority, category).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TicketResponse> getSlaBreaches() {
        return ticketRepository.findSlaBreaches(LocalDateTime.now()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketResponse updateTicket(Long id, TicketRequest req) {
        Ticket ticket = getTicketEntity(id);

        if (req.getTitle() != null && !req.getTitle().trim().isEmpty()) {
            ticket.setTitle(req.getTitle().trim());
        }
        if (req.getDescription() != null && !req.getDescription().trim().isEmpty()) {
            ticket.setDescription(req.getDescription().trim());
        }
        if (req.getCategory() != null) {
            ticket.setCategory(req.getCategory().trim());
        }
        if (req.getPriority() != null) {
            ticket.setPriority(req.getPriority().trim().toUpperCase());
            ticket.calculateDueAt();
        }

        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved);
    }

    @Transactional
    public TicketResponse updateStatus(Long id, String newStatus, String notes, String performedBy, String role) {
        Ticket ticket = getTicketEntity(id);
        String oldStatus = ticket.getStatus();
        String normalizedStatus = newStatus.trim().toUpperCase();

        List<String> validStatuses = Arrays.asList("OPEN", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED", "REOPENED");
        if (!validStatuses.contains(normalizedStatus)) {
            throw new BadRequestException("Invalid ticket status: " + newStatus + ". Valid statuses are: " + validStatuses);
        }

        ticket.setStatus(normalizedStatus);

        if ("RESOLVED".equals(normalizedStatus) || "CLOSED".equals(normalizedStatus)) {
            if (ticket.getResolvedAt() == null) {
                ticket.setResolvedAt(LocalDateTime.now());
            }
        } else if ("REOPENED".equals(normalizedStatus)) {
            ticket.setResolvedAt(null);
        }

        Ticket saved = ticketRepository.save(ticket);

        String detail = "Status changed from " + oldStatus + " to " + normalizedStatus;
        if (notes != null && !notes.trim().isEmpty()) {
            detail += ". Resolution Notes: " + notes.trim();
        }

        auditLogService.record(
                ticket.getTicketId(),
                "STATUS_UPDATED",
                performedBy != null ? performedBy : "System",
                role != null ? role : "TECHNICIAN",
                detail
        );

        return toResponse(saved);
    }

    @Transactional
    public TicketResponse assignTicket(Long id, Long technicianId, String performedBy, String role) {
        Ticket ticket = getTicketEntity(id);

        if (technicianId == null || technicianId <= 0) {
            ticket.setTechnician(null);
            if ("ASSIGNED".equalsIgnoreCase(ticket.getStatus())) {
                ticket.setStatus("OPEN");
            }
            auditLogService.record(
                    ticket.getTicketId(),
                    "TECHNICIAN_UNASSIGNED",
                    performedBy != null ? performedBy : "Admin",
                    role != null ? role : "ADMIN",
                    "Ticket unassigned"
            );
        } else {
            Technician tech = technicianRepository.findById(technicianId)
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + technicianId));
            ticket.setTechnician(tech);
            if ("OPEN".equalsIgnoreCase(ticket.getStatus())) {
                ticket.setStatus("ASSIGNED");
            }
            auditLogService.record(
                    ticket.getTicketId(),
                    "TECHNICIAN_ASSIGNED",
                    performedBy != null ? performedBy : "Admin",
                    role != null ? role : "ADMIN",
                    "Assigned to " + tech.getFullName() + " (" + tech.getSpecialization() + ")"
            );
        }

        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved);
    }

    @Transactional
    public void deleteTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket not found with ID: " + id);
        }
        ticketRepository.deleteById(id);
    }

    public TicketResponse toResponse(Ticket t) {
        TicketResponse res = new TicketResponse();
        res.setTicketId(t.getTicketId());
        res.setTitle(t.getTitle());
        res.setDescription(t.getDescription());
        res.setCategory(t.getCategory());
        res.setPriority(t.getPriority());
        res.setStatus(t.getStatus());

        if (t.getUser() != null) {
            res.setUserId(t.getUser().getUserId());
            res.setUserName(t.getUser().getFullName());
            res.setUserEmail(t.getUser().getEmail());
        }

        if (t.getTechnician() != null) {
            res.setTechnicianId(t.getTechnician().getTechnicianId());
            res.setTechnicianName(t.getTechnician().getFullName());
            res.setTechnicianEmail(t.getTechnician().getEmail());
        }

        res.setCreatedAt(t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
        res.setUpdatedAt(t.getUpdatedAt() != null ? t.getUpdatedAt().toString() : null);
        res.setDueAt(t.getDueAt() != null ? t.getDueAt().toString() : null);
        res.setResolvedAt(t.getResolvedAt() != null ? t.getResolvedAt().toString() : null);

        // SLA Fields
        String priority = t.getPriority() != null ? t.getPriority().toUpperCase() : "MEDIUM";
        long slaHours = 8;
        if ("CRITICAL".equals(priority)) slaHours = 2;
        else if ("HIGH".equals(priority)) slaHours = 4;
        else if ("LOW".equals(priority)) slaHours = 24;

        res.setSlaTargetHours(slaHours);
        res.setSlaDeadline(t.getDueAt() != null ? t.getDueAt().toString() : null);
        res.setRemainingSlaMinutes(t.getRemainingSlaMinutes());
        res.setIsSlaBreached(t.isSlaBreached());

        // Impact & Diagnostics
        res.setImpactScore(t.getImpactScore());
        if (t.getDiagnostics() != null && !t.getDiagnostics().isEmpty()) {
            try {
                Map<String, Object> map = objectMapper.readValue(t.getDiagnostics(), new TypeReference<Map<String, Object>>() {});
                res.setDiagnostics(map);
            } catch (Exception ignored) {
            }
        }

        return res;
    }
}
