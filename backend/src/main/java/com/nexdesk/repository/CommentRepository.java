package com.nexdesk.repository;

import com.nexdesk.entity.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findByTicketTicketIdOrderByCreatedAtAsc(Long ticketId);
    List<TicketComment> findByTicketTicketIdAndIsInternalFalseOrderByCreatedAtAsc(Long ticketId);
}
