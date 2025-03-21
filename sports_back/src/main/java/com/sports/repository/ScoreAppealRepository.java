package com.sports.repository;

import com.sports.entity.ScoreAppeal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreAppealRepository extends JpaRepository<ScoreAppeal, Long> {
    
    // 教师查询需要审核的申诉
    @Query("SELECT sa FROM ScoreAppeal sa " +
           "WHERE (sa.reviewer.id = :teacherId OR sa.reviewer IS NULL) " +
           "AND (:status IS NULL OR sa.status = :status)")
    Page<ScoreAppeal> findByReviewerId(
        @Param("teacherId") Long teacherId,
        @Param("status") String status,
        Pageable pageable
    );

    // 获取单个申诉详情
    @Query("SELECT sa FROM ScoreAppeal sa " +
           "LEFT JOIN FETCH sa.testRecord tr " +
           "LEFT JOIN FETCH tr.sportsItem si " +
           "LEFT JOIN FETCH sa.student s " +
           "LEFT JOIN FETCH sa.reviewer r " +
           "WHERE sa.id = :id")
    ScoreAppeal findByIdWithDetails(@Param("id") Long id);

    // 检查是否存在未完成的申诉
    boolean existsByTestRecordIdAndStatusNot(Long testRecordId, String status);

    // 修改学生查询方法，添加 FETCH JOIN
    @Query("SELECT DISTINCT sa FROM ScoreAppeal sa " +
           "LEFT JOIN FETCH sa.student s " +
           "LEFT JOIN FETCH sa.testRecord tr " +
           "LEFT JOIN FETCH tr.sportsItem si " +
           "LEFT JOIN FETCH sa.reviewer r " +
           "WHERE sa.student.id = :studentId " +
           "AND (:status IS NULL OR sa.status = :status)")
    List<ScoreAppeal> findByStudentIdAndStatusWithDetails(
        @Param("studentId") Long studentId,
        @Param("status") String status
    );
} 