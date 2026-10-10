package com.nexdesk.repository;

import com.nexdesk.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByOrderByCreatedAtDesc();

    List<Ticket> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    List<Ticket> findByTechnicianTechnicianIdOrderByCreatedAtDesc(Long technicianId);

    List<Ticket> findByStatus(String status);

    List<Ticket> findByPriority(String priority);

    long countByStatus(String status);

    long countByPriority(String priority);

    @Query("SELECT t FROM Ticket t WHERE t.dueAt < :now AND t.status NOT IN ('RESOLVED', 'CLOSED') ORDER BY t.dueAt ASC")
    List<Ticket> findSlaBreaches(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.dueAt < :now AND t.status NOT IN ('RESOLVED', 'CLOSED')")
    long countSlaBreaches(@Param("now") LocalDateTime now);

    @Query("SELECT t FROM Ticket t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:category IS NULL OR t.category = :category) " +
            "ORDER BY t.createdAt DESC")
    List<Ticket> filterTickets(@Param("status") String status,
                               @Param("priority") String priority,
                               @Param("category") String category);

    @Query("SELECT t FROM Ticket t WHERE " +
            "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) " +
            "ORDER BY t.createdAt DESC")
    Page<Ticket> searchTickets(@Param("search") String search,
                               @Param("status") String status,
                               @Param("priority") String priority,
                               Pageable pageable);
}
