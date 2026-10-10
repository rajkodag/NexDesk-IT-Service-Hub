package com.nexdesk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexdesk.dto.TicketRequest;
import com.nexdesk.dto.TicketResponse;
import com.nexdesk.entity.Technician;
import com.nexdesk.entity.Ticket;
import com.nexdesk.entity.User;
import com.nexdesk.repository.TechnicianRepository;
import com.nexdesk.repository.TicketRepository;
import com.nexdesk.repository.UserRepository;
import com.nexdesk.service.AuditLogService;
import com.nexdesk.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private AuditLogService auditLogService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private TicketService ticketService;

    private User sampleUser;
    private Technician sampleTech;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Alice Employee", "alice@example.com", "pass", "USER");
        sampleUser.setUserId(10L);

        sampleTech = new Technician("Bob Tech", "bob@example.com", "Network", "AVAILABLE");
        sampleTech.setTechnicianId(20L);
    }

    @Test
    void testCreateTicket_SlaCalculation_Critical() {
        TicketRequest req = new TicketRequest("Server Down", "Core server unreachable", "Infrastructure", "CRITICAL");

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setTicketId(101L);
            return t;
        });

        TicketResponse resp = ticketService.createTicket(req, 10L, "Alice Employee", "USER");

        assertNotNull(resp);
        assertEquals("Server Down", resp.getTitle());
        assertEquals("CRITICAL", resp.getPriority());
        assertEquals("OPEN", resp.getStatus());
        assertEquals(2L, resp.getSlaTargetHours(), "Critical tickets must have 2 hours SLA deadline");
        assertNotNull(resp.getDueAt());
        verify(auditLogService, times(1)).record(eq(101L), eq("TICKET_CREATED"), anyString(), anyString(), anyString());
    }

    @Test
    void testCreateTicket_SlaCalculation_Medium() {
        TicketRequest req = new TicketRequest("Printer Jam", "Printer tray 2 jammed", "Hardware", "MEDIUM");

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setTicketId(102L);
            return t;
        });

        TicketResponse resp = ticketService.createTicket(req, 10L, "Alice Employee", "USER");

        assertNotNull(resp);
        assertEquals(8L, resp.getSlaTargetHours(), "Medium tickets must have 8 hours SLA deadline");
    }

    @Test
    void testAssignTicket_UpdatesStatusToAssigned() {
        Ticket ticket = new Ticket("VPN Issue", "VPN timeout", "Network", "HIGH", sampleUser);
        ticket.setTicketId(103L);
        ticket.setStatus("OPEN");

        when(ticketRepository.findById(103L)).thenReturn(Optional.of(ticket));
        when(technicianRepository.findById(20L)).thenReturn(Optional.of(sampleTech));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        TicketResponse resp = ticketService.assignTicket(103L, 20L, "Admin User", "ADMIN");

        assertEquals("ASSIGNED", resp.getStatus());
        assertEquals(20L, resp.getTechnicianId());
        assertEquals("Bob Tech", resp.getTechnicianName());
        verify(auditLogService, times(1)).record(eq(103L), eq("TECHNICIAN_ASSIGNED"), anyString(), anyString(), anyString());
    }

    @Test
    void testUpdateStatus_ResolveSetsResolvedAt() {
        Ticket ticket = new Ticket("Screen Flicker", "Monitor flickering", "Hardware", "LOW", sampleUser);
        ticket.setTicketId(104L);
        ticket.setStatus("IN_PROGRESS");

        when(ticketRepository.findById(104L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        TicketResponse resp = ticketService.updateStatus(104L, "RESOLVED", "Replaced HDMI cable", "Bob Tech", "TECHNICIAN");

        assertEquals("RESOLVED", resp.getStatus());
        assertNotNull(resp.getResolvedAt());
        verify(auditLogService, times(1)).record(eq(104L), eq("STATUS_UPDATED"), anyString(), anyString(), contains("Replaced HDMI cable"));
    }

    @Test
    void testSlaBreached_Detection() {
        Ticket ticket = new Ticket("Email Lag", "Slow sync", "Software", "HIGH", sampleUser);
        ticket.setCreatedAt(LocalDateTime.now().minusHours(10));
        ticket.setDueAt(LocalDateTime.now().minusHours(6)); // Overdue 6 hours ago
        ticket.setStatus("OPEN");

        assertTrue(ticket.isSlaBreached(), "Ticket with dueAt in the past and OPEN status must be marked as SLA breached");

        // Now resolve it within deadline in the past
        ticket.setStatus("RESOLVED");
        ticket.setResolvedAt(LocalDateTime.now().minusHours(7)); // resolved before dueAt (-6h)
        assertFalse(ticket.isSlaBreached(), "Ticket resolved before its dueAt must NOT be marked breached");
    }
}
