package com.sports.repository;

import com.sports.entity.ExemptionApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExemptionApplicationRepository extends JpaRepository<ExemptionApplication, Long> {
    
    @Query("SELECT COUNT(e) FROM ExemptionApplication e WHERE e.status IN ('PENDING_TEACHER', 'PENDING_ADMIN')")
    Integer countPendingApplications();
    
    @Query("SELECT COUNT(e) FROM ExemptionApplication e WHERE " +
           "(e.teacherReviewTime >= :since OR e.adminReviewTime >= :since) AND " +
           "e.status IN ('APPROVED', 'REJECTED', 'REJECTED_TEACHER')")
    Integer countReviewedSince(@Param("since") LocalDateTime since);

    @Query("SELECT e FROM ExemptionApplication e " +
           "WHERE (:type IS NULL OR e.type = :type) AND " +
           "(:keyword IS NULL OR e.studentNumber LIKE %:keyword% OR " +
           "e.studentName LIKE %:keyword%)")
    Page<ExemptionApplication> findAllWithFilters(
        @Param("type") String type,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    @Query("SELECT e FROM ExemptionApplication e " +
           "WHERE (:className IS NULL OR e.className = :className) AND " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:keyword IS NULL OR " +
           "e.studentNumber LIKE CONCAT('%', :keyword, '%') OR " +
           "e.studentName LIKE CONCAT('%', :keyword, '%'))")
    List<ExemptionApplication> findByFiltersForExport(
        @Param("className") String className,
        @Param("type") String type,
        @Param("status") String status,
        @Param("keyword") String keyword
    );

    @Query("SELECT DISTINCT e.className FROM ExemptionApplication e WHERE e.className IS NOT NULL ORDER BY e.className")
    List<String> findDistinctClassNames();
} 