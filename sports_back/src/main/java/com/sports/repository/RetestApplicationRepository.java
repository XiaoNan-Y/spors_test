package com.sports.repository;

import com.sports.entity.RetestApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RetestApplicationRepository extends JpaRepository<RetestApplication, Long> {
    
    @Query("SELECT r FROM RetestApplication r WHERE r.status = 'PENDING' " +
           "AND (:keyword IS NULL OR " +
           "LOWER(r.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.studentNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY r.applyTime DESC")
    Page<RetestApplication> findPendingApplications(
        @Param("keyword") String keyword, 
        Pageable pageable
    );

    @Query("SELECT r FROM RetestApplication r WHERE r.studentId = :studentId " +
           "ORDER BY r.applyTime DESC")
    Page<RetestApplication> findByStudentId(
        @Param("studentId") Long studentId, 
        Pageable pageable
    );
} 