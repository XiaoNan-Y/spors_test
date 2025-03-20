package com.sports.repository;

import com.sports.entity.ScoreAppeal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScoreAppealRepository extends JpaRepository<ScoreAppeal, Long> {
    
    @Query("SELECT DISTINCT sa FROM ScoreAppeal sa " +
           "LEFT JOIN FETCH sa.testRecord tr " +
           "LEFT JOIN FETCH tr.sportsItem si " +
           "LEFT JOIN FETCH sa.student s " +
           "LEFT JOIN FETCH sa.reviewer r " +
           "WHERE sa.student.id = :studentId " +
           "AND (:status IS NULL OR sa.status = :status)")
    List<ScoreAppeal> findByStudentIdAndStatusWithDetails(
        @Param("studentId") Long studentId,
        @Param("status") String status
    );

    boolean existsByTestRecordIdAndStatusNot(Long testRecordId, String status);
} 