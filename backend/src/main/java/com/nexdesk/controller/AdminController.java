package com.nexdesk.controller;

import com.nexdesk.dto.*;
import com.nexdesk.entity.AuditLog;
import com.nexdesk.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrator operational cockpit, analytics, user, and SLA control")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final DashboardService dashboardService;
    private final TicketService ticketService;
    private final UserService userService;
    private final TechnicianService technicianService;
    private final AuditLogService auditLogService;

    public AdminController(DashboardService dashboardService,
                           TicketService ticketService,
                           UserService userService,
                           TechnicianService technicianService,
                           AuditLogService auditLogService) {
        this.dashboardService = dashboardService;
        this.ticketService = ticketService;
        this.userService = userService;
        this.technicianService = technicianService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get overall enterprise dashboard analytics and KPIs")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboardMetrics());
    }

    @GetMapping("/tickets")
    @Operation(summary = "Get all tickets for administrative oversight")
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/users")
    @Operation(summary = "List all registered users in the organization")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/technicians")
    @Operation(summary = "List all technicians and workloads")
    public ResponseEntity<List<TechnicianResponse>> getAllTechnicians() {
        return ResponseEntity.ok(technicianService.getAll());
    }

    @PostMapping("/technicians")
    @Operation(summary = "Add a new technician to the roster")
    public ResponseEntity<TechnicianResponse> createTechnician(@Valid @RequestBody TechnicianRequest request) {
        return new ResponseEntity<>(technicianService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/tickets/{id}/assign")
    @Operation(summary = "Assign a ticket to a technician by ticket ID in path")
    public ResponseEntity<TicketResponse> assignTicketPath(
            @PathVariable Long id,
            @RequestBody AssignTicketRequest request,
            Authentication auth
    ) {
        String performer = auth != null ? auth.getName() : "Admin";
        return ResponseEntity.ok(ticketService.assignTicket(id, request.getTechnicianId(), performer, "ADMIN"));
    }

    // Support frontend route PUT /api/admin/tickets/assign with ticketId in body
    @PutMapping("/tickets/assign")
    @Operation(summary = "Assign a ticket to a technician with ticketId in body (Frontend compatibility)")
    public ResponseEntity<TicketResponse> assignTicketBody(
            @RequestBody AssignTicketRequest request,
            Authentication auth
    ) {
        String performer = auth != null ? auth.getName() : "Admin";
        return ResponseEntity.ok(ticketService.assignTicket(request.getTicketId(), request.getTechnicianId(), performer, "ADMIN"));
    }

    @PutMapping("/tickets/{id}/status")
    @Operation(summary = "Change ticket status by ticket ID in path")
    public ResponseEntity<TicketResponse> updateStatusPath(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            Authentication auth
    ) {
        String performer = auth != null ? auth.getName() : "Admin";
        return ResponseEntity.ok(ticketService.updateStatus(id, request.getStatus(), request.getNotes(), performer, "ADMIN"));
    }

    // Support frontend route PUT /api/admin/tickets/status with ticketId in body
    @PutMapping("/tickets/status")
    @Operation(summary = "Change ticket status with ticketId in body (Frontend compatibility)")
    public ResponseEntity<TicketResponse> updateStatusBody(
            @Valid @RequestBody StatusUpdateRequest request,
            Authentication auth
    ) {
        String performer = auth != null ? auth.getName() : "Admin";
        return ResponseEntity.ok(ticketService.updateStatus(request.getTicketId(), request.getStatus(), request.getNotes(), performer, "ADMIN"));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Retrieve entire immutable audit log history")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    @GetMapping("/sla-breaches")
    @Operation(summary = "Retrieve tickets that have breached their SLA deadline")
    public ResponseEntity<List<TicketResponse>> getSlaBreaches() {
        return ResponseEntity.ok(ticketService.getSlaBreaches());
    }
}
