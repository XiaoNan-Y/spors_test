package com.sports.repository;

import com.sports.entity.ScoreAppeal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ScoreAppealRepository extends JpaRepository<ScoreAppeal, Long> {
    
    // 教师查询方法 - 基础查询
    @Query("SELECT sa FROM ScoreAppeal sa " +
           "WHERE (:status IS NULL OR sa.status = :status)")
    Page<ScoreAppeal> findAllByStatus(
        @Param("status") String status,
        Pageable pageable
    );

    // 批量获取详细数据
    @Query("SELECT DISTINCT sa FROM ScoreAppeal sa " +
           "LEFT JOIN FETCH sa.student s " +
           "LEFT JOIN FETCH sa.testRecord tr " +
           "LEFT JOIN FETCH tr.sportsItem si " +
           "LEFT JOIN FETCH sa.reviewer r " +
           "WHERE sa IN :appeals")
    List<ScoreAppeal> findByAppealsWithDetails(@Param("appeals") Collection<ScoreAppeal> appeals);

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

    // 学生查询方法
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