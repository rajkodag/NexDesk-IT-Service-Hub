package com.nexdesk.dto;

public class AssignTicketRequest {

    private Long ticketId; // optional if passed in path
    private Long technicianId;

    public AssignTicketRequest() {
    }

    public AssignTicketRequest(Long technicianId) {
        this.technicianId = technicianId;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }
}
