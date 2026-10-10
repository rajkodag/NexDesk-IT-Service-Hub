package com.nexdesk.service;

import com.nexdesk.dto.CommentRequest;
import com.nexdesk.dto.CommentResponse;
import com.nexdesk.entity.Ticket;
import com.nexdesk.entity.TicketComment;
import com.nexdesk.entity.User;
import com.nexdesk.exception.ResourceNotFoundException;
import com.nexdesk.repository.CommentRepository;
import com.nexdesk.repository.TicketRepository;
import com.nexdesk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketCommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public TicketCommentService(CommentRepository commentRepository,
                                TicketRepository ticketRepository,
                                UserRepository userRepository,
                                AuditLogService auditLogService) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public CommentResponse addComment(Long ticketId, Long userId, CommentRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        User user = null;
        String authorName = "System";
        String authorRole = "USER";

        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                authorName = user.getFullName();
                authorRole = user.getRole();
            }
        }

        TicketComment comment = new TicketComment(
                ticket,
                user,
                authorName,
                authorRole,
                request.getMessage().trim(),
                request.getIsInternal()
        );

        TicketComment saved = commentRepository.save(comment);

        auditLogService.record(
                ticketId,
                Boolean.TRUE.equals(request.getIsInternal()) ? "INTERNAL_NOTE_ADDED" : "COMMENT_ADDED",
                authorName,
                authorRole,
                "Added comment: " + (request.getMessage().length() > 60 ? request.getMessage().substring(0, 57) + "..." : request.getMessage())
        );

        return toResponse(saved);
    }

    public List<CommentResponse> getCommentsForTicket(Long ticketId, boolean includeInternal) {
        List<TicketComment> comments = includeInternal
                ? commentRepository.findByTicketTicketIdOrderByCreatedAtAsc(ticketId)
                : commentRepository.findByTicketTicketIdAndIsInternalFalseOrderByCreatedAtAsc(ticketId);

        return comments.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private CommentResponse toResponse(TicketComment c) {
        return new CommentResponse(
                c.getCommentId(),
                c.getTicket().getTicketId(),
                c.getUser() != null ? c.getUser().getUserId() : null,
                c.getAuthorName(),
                c.getAuthorRole(),
                c.getMessage(),
                c.getIsInternal(),
                c.getCreatedAt() != null ? c.getCreatedAt().toString() : null
        );
    }
}
