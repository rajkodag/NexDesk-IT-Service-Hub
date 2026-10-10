package com.nexdesk.controller;

import com.nexdesk.dto.*;
import com.nexdesk.service.TechnicianService;
import com.nexdesk.service.TicketCommentService;
import com.nexdesk.service.TicketService;
import com.nexdesk.service.UserService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/technicians")
@Tag(name = "Technicians", description = "Technician management and operational workflows")
public class TechnicianController {

    private final TechnicianService technicianService;
    private final TicketService ticketService;
    private final TicketCommentService ticketCommentService;
    private final UserService userService;

    public TechnicianController(TechnicianService technicianService,
                                TicketService ticketService,
                                TicketCommentService ticketCommentService,
                                UserService userService) {
        this.technicianService = technicianService;
        this.ticketService = ticketService;
        this.ticketCommentService = ticketCommentService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get list of all technicians")
    public ResponseEntity<List<TechnicianResponse>> getAllTechnicians() {
        return ResponseEntity.ok(technicianService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get technician details by ID")
    public ResponseEntity<TechnicianResponse> getTechnicianById(@PathVariable Long id) {
        return ResponseEntity.ok(technicianService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new technician (Admin only)")
    public ResponseEntity<TechnicianResponse> createTechnician(@Valid @RequestBody TechnicianRequest request) {
        return new ResponseEntity<>(technicianService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update technician availability status (AVAILABLE, BUSY, OFFLINE)")
    public ResponseEntity<TechnicianResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "AVAILABLE");
        return ResponseEntity.ok(technicianService.updateStatus(id, status));
    }

    @GetMapping("/{id}/tickets")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get tickets assigned to a specific technician")
    public ResponseEntity<List<TicketResponse>> getTechnicianTickets(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketsByTechnician(id));
    }

    @PutMapping("/tickets/{id}/status")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update ticket status by technician (Technician or Admin)")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            Authentication auth
    ) {
        String performerName = auth != null ? auth.getName() : "Technician";
        return ResponseEntity.ok(ticketService.updateStatus(id, request.getStatus(), request.getNotes(), performerName, "TECHNICIAN"));
    }

    @PostMapping("/tickets/{id}/notes")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add internal technical note to ticket (Technician or Admin)")
    public ResponseEntity<CommentResponse> addInternalNote(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            Authentication auth
    ) {
        Long userId = null;
        if (auth != null) {
            try {
                userId = userService.getUserByEmail(auth.getName()).getUserId();
            } catch (Exception ignored) {}
        }
        request.setIsInternal(true);
        return new ResponseEntity<>(ticketCommentService.addComment(id, userId, request), HttpStatus.CREATED);
    }
}
