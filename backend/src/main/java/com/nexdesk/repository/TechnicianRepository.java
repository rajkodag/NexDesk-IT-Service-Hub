package com.nexdesk.repository;

import com.nexdesk.entity.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    Optional<Technician> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Technician> findByStatus(String status);
    List<Technician> findBySpecialization(String specialization);
}
