package com.sports.repository;

import com.sports.entity.ExemptionApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExemptionRepository extends JpaRepository<ExemptionApplication, Long> {
    Page<ExemptionApplication> findByStudentNameContainingOrStudentNumberContaining(
        String studentName, String studentNumber, Pageable pageable);
} 