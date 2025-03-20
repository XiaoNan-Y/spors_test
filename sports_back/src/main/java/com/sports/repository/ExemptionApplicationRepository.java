package com.sports.repository;

import com.sports.entity.ExemptionApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExemptionApplicationRepository extends JpaRepository<ExemptionApplication, Long> {
    
    // 使用 @Query 注解来明确指定查询
    @Query("SELECT COUNT(e) FROM ExemptionApplication e WHERE e.studentId = :studentId AND e.status = :status")
    long countByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") String status);
    
    // 使用 @Query 注解来明确指定查询
    @Query("SELECT e FROM ExemptionApplication e WHERE e.studentId = :studentId")
    Page<ExemptionApplication> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);
    
    // 根据类型和关键字查询申请列表（分页）
    @Query("SELECT DISTINCT e FROM ExemptionApplication e " +
           "LEFT JOIN FETCH e.sportsItem " +
           "WHERE (:type IS NULL OR e.type = :type) " +
           "AND (:keyword IS NULL OR " +
           "LOWER(e.studentNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ExemptionApplication> findAllWithFilters(
        @Param("type") String type,
        @Param("keyword") String keyword,
        Pageable pageable
    );
    
    // 统计待审核的申请数量
    @Query("SELECT COUNT(e) FROM ExemptionApplication e WHERE e.status = 'PENDING'")
    Integer countPendingApplications();
    
    @Query("SELECT COUNT(e) FROM ExemptionApplication e WHERE " +
           "(e.teacherReviewTime >= :since OR e.adminReviewTime >= :since) AND " +
           "e.status IN ('APPROVED', 'REJECTED', 'REJECTED_TEACHER')")
    Integer countReviewedSince(@Param("since") LocalDateTime since);

    @Query("SELECT DISTINCT e FROM ExemptionApplication e " +
           "LEFT JOIN FETCH e.sportsItem " +
           "WHERE (:className IS NULL OR e.className = :className) " +
           "AND (:type IS NULL OR e.type = :type) " +
           "AND e.status = :status " +
           "AND (:keyword IS NULL OR " +
           "LOWER(e.studentNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ExemptionApplication> findAllWithFiltersNoPage(
        @Param("className") String className,
        @Param("type") String type,
        @Param("status") String status,
        @Param("keyword") String keyword
    );

    // 计数查询
    @Query("SELECT COUNT(DISTINCT e) FROM ExemptionApplication e " +
           "WHERE (:type IS NULL OR e.type = :type) " +
           "AND (:keyword IS NULL OR " +
           "LOWER(e.studentNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    long countWithFilters(
        @Param("type") String type,
        @Param("keyword") String keyword
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

    @Query("SELECT COUNT(e) FROM ExemptionApplication e WHERE e.status = :status")
    long countByStatus(@Param("status") String status);
} 