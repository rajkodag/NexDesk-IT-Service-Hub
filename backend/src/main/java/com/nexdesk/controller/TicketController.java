package com.nexdesk.controller;

import com.nexdesk.dto.*;
import com.nexdesk.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Core IT ticket lifecycle, creation, assignment, and discussion APIs")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketService ticketService;
    private final TicketCommentService ticketCommentService;
    private final UserService userService;
    private final SystemHealthService systemHealthService;

    public TicketController(TicketService ticketService,
                            TicketCommentService ticketCommentService,
                            UserService userService,
                            SystemHealthService systemHealthService) {
        this.ticketService = ticketService;
        this.ticketCommentService = ticketCommentService;
        this.userService = userService;
        this.systemHealthService = systemHealthService;
    }

    @PostMapping
    @Operation(summary = "Create a new IT support ticket")
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody TicketRequest request, Authentication auth) {
        Long userId = request.getUserId();
        String performerName = "User";
        String performerRole = "USER";

        if (auth != null) {
            try {
                UserResponse cur = userService.getUserByEmail(auth.getName());
                userId = cur.getUserId();
                performerName = cur.getFullName();
                performerRole = cur.getRole();
            } catch (Exception ignored) {
            }
        }

        TicketResponse res = ticketService.createTicket(request, userId, performerName, performerRole);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List tickets with optional filtering by status, priority, category, or user")
    public ResponseEntity<List<TicketResponse>> getTickets(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category
    ) {
        if (userId != null && userId > 0) {
            return ResponseEntity.ok(ticketService.getTicketsByUser(userId));
        }
        if (status != null || priority != null || category != null) {
            return ResponseEntity.ok(ticketService.filterTickets(status, priority, category));
        }
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/my")
    @Operation(summary = "Get tickets submitted by the authenticated user")
    public ResponseEntity<List<TicketResponse>> getMyTickets(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse cur = userService.getUserByEmail(auth.getName());
        return ResponseEntity.ok(ticketService.getTicketsByUser(cur.getUserId()));
    }

    @GetMapping("/assigned")
    @Operation(summary = "Get tickets assigned to the authenticated user")
    public ResponseEntity<List<TicketResponse>> getAssignedTickets(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse cur = userService.getUserByEmail(auth.getName());
        return ResponseEntity.ok(ticketService.getTicketsByTechnician(cur.getUserId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed ticket by ID including SLA metrics and diagnostics")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ticket details (Title, Description, Category, Priority)")
    public ResponseEntity<TicketResponse> updateTicket(@PathVariable Long id, @RequestBody TicketRequest request) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ticket by ID")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update ticket status (OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED, REOPENED)")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            Authentication auth
    ) {
        String performerName = auth != null ? auth.getName() : "System";
        String performerRole = auth != null ? auth.getAuthorities().toString() : "USER";
        return ResponseEntity.ok(ticketService.updateStatus(id, request.getStatus(), request.getNotes(), performerName, performerRole));
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign technician to ticket")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable Long id,
            @RequestBody AssignTicketRequest request,
            Authentication auth
    ) {
        String performerName = auth != null ? auth.getName() : "Admin";
        String performerRole = auth != null ? auth.getAuthorities().toString() : "ADMIN";
        return ResponseEntity.ok(ticketService.assignTicket(id, request.getTechnicianId(), performerName, performerRole));
    }

    // Two-way ticket comments & discussions
    @GetMapping("/{id}/comments")
    @Operation(summary = "Get comment thread for ticket")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeInternal
    ) {
        return ResponseEntity.ok(ticketCommentService.getCommentsForTicket(id, includeInternal));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add comment to ticket")
    public ResponseEntity<CommentResponse> addComment(
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
        return new ResponseEntity<>(ticketCommentService.addComment(id, userId, request), HttpStatus.CREATED);
    }

    // Support flat endpoints for backwards compatibility with existing frontend
    @GetMapping("/comments")
    @Operation(summary = "Get comments by ticketId query param (Frontend backward compatibility)")
    public ResponseEntity<List<CommentResponse>> getCommentsByParam(
            @RequestParam Long ticketId,
            @RequestParam(defaultValue = "false") boolean includeInternal
    ) {
        return ResponseEntity.ok(ticketCommentService.getCommentsForTicket(ticketId, includeInternal));
    }

    @PostMapping("/comments")
    @Operation(summary = "Add comment using ticketId inside body (Frontend backward compatibility)")
    public ResponseEntity<CommentResponse> addCommentByBody(
            @Valid @RequestBody CommentRequest request,
            Authentication auth
    ) {
        Long userId = null;
        if (auth != null) {
            try {
                userId = userService.getUserByEmail(auth.getName()).getUserId();
            } catch (Exception ignored) {}
        }
        return new ResponseEntity<>(ticketCommentService.addComment(request.getTicketId(), userId, request), HttpStatus.CREATED);
    }

    @PostMapping("/deflect")
    @Operation(summary = "Record self-healing deflection when user resolves issue without ticket submission")
    public ResponseEntity<Map<String, Object>> deflectTicket() {
        int count = systemHealthService.incrementDeflected();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("deflectedTicketsCount", count);
        return ResponseEntity.ok(resp);
    }
}
