package com.sports.repository;

import com.sports.entity.TestExemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ExemptionApplicationRepository extends JpaRepository<TestExemption, Long> {
    
    @Query("SELECT COUNT(e) FROM TestExemption e WHERE e.status IN ('PENDING_TEACHER', 'PENDING_ADMIN')")
    Integer countPendingApplications();
    
    @Query("SELECT COUNT(e) FROM TestExemption e WHERE " +
           "(e.teacherReviewTime >= :since OR e.adminReviewTime >= :since) AND " +
           "e.status IN ('APPROVED', 'REJECTED', 'REJECTED_TEACHER')")
    Integer countReviewedSince(@Param("since") LocalDateTime since);
} 