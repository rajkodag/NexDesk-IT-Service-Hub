package com.nexdesk.service;

import com.nexdesk.entity.AuditLog;
import com.nexdesk.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(Long ticketId, String action, String performedBy, String role, String details) {
        AuditLog log = new AuditLog(ticketId, action, performedBy, role, details);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsForTicket(Long ticketId) {
        return auditLogRepository.findByTicketIdOrderByTimestampDesc(ticketId);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
