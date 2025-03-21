package com.sports.repository;

import com.sports.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long>, JpaSpecificationExecutor<Feedback> {
    Page<Feedback> findByStudentId(Long studentId, Pageable pageable);
    Page<Feedback> findByStudentIdAndType(Long studentId, String type, Pageable pageable);
    Page<Feedback> findByTypeAndStatus(String type, String status, Pageable pageable);
    Page<Feedback> findByType(String type, Pageable pageable);
    Page<Feedback> findByStatus(String status, Pageable pageable);
} 