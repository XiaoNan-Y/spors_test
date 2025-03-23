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
    
    // 添加这个方法
    @Query("SELECT COUNT(e) FROM ExemptionApplication e WHERE e.studentId = :studentId AND e.status = :status")
    long countByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") String status);
    
    // 分页查询
    @Query(value = "SELECT DISTINCT e FROM ExemptionApplication e " +
           "LEFT JOIN FETCH e.sportsItem " +
           "WHERE e.studentId = :studentId",
           countQuery = "SELECT COUNT(DISTINCT e) FROM ExemptionApplication e WHERE e.studentId = :studentId")
    Page<ExemptionApplication> findByStudentId(Long studentId, Pageable pageable);
    
    // 根据类型和关键字查询申请列表（分页）
    @Query("SELECT DISTINCT e FROM ExemptionApplication e " +
           "LEFT JOIN FETCH e.sportsItem " +
           "WHERE (:type IS NULL OR e.type = :type) " +
           "AND (:keyword IS NULL OR " +
           "LOWER(e.studentNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.studentName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND e.status IN ('PENDING', 'PENDING_TEACHER')")
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
           "LOWER(e.studentName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND e.status IN ('PENDING', 'PENDING_TEACHER')")
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

    // 根据学生姓名或学号模糊查询
    Page<ExemptionApplication> findByStudentNameContainingOrStudentNumberContaining(
        String studentName, 
        String studentNumber, 
        Pageable pageable
    );

    @Query("SELECT e FROM ExemptionApplication e " +
           "WHERE (:className IS NULL OR e.className = :className) " +
           "AND (:type IS NULL OR e.type = :type) " +
           "AND (e.status = 'PENDING_TEACHER' OR e.status = 'PENDING') " +
           "AND (:keyword IS NULL OR " +
           "e.studentNumber LIKE CONCAT('%', :keyword, '%') OR " +
           "e.studentName LIKE CONCAT('%', :keyword, '%'))")
    List<ExemptionApplication> findAllWithFiltersNoPage(
        @Param("className") String className,
        @Param("type") String type,
        @Param("keyword") String keyword
    );

    @Query("SELECT e FROM ExemptionApplication e " +
           "WHERE (e.status = 'PENDING_TEACHER' OR e.status = 'PENDING') " +
           "AND (LOWER(e.studentName) LIKE LOWER(CONCAT('%', :studentName, '%')) " +
           "OR LOWER(e.studentNumber) LIKE LOWER(CONCAT('%', :studentNumber, '%')))")
    Page<ExemptionApplication> findTeacherPendingApplications(
        String studentName,
        String studentNumber,
        Pageable pageable
    );

    @Query(value = "SELECT DISTINCT e FROM ExemptionApplication e " +
           "LEFT JOIN FETCH e.sportsItem " +
           "WHERE e.studentId = :studentId " +
           "ORDER BY e.applyTime DESC",
           countQuery = "SELECT COUNT(e) FROM ExemptionApplication e WHERE e.studentId = :studentId")
    Page<ExemptionApplication> findByStudentIdWithPage(
        @Param("studentId") Long studentId, 
        Pageable pageable
    );

    // 使用学号查询
    @Query("SELECT e FROM ExemptionApplication e " +
           "LEFT JOIN FETCH e.sportsItem " +
           "WHERE e.studentNumber = :studentNumber " +
           "ORDER BY e.applyTime DESC")
    List<ExemptionApplication> findAllByStudentNumber(@Param("studentNumber") String studentNumber);

    @Query("SELECT DISTINCT e FROM ExemptionApplication e " +
           "LEFT JOIN FETCH e.sportsItem " +
           "WHERE e.studentId = :studentId " +
           "ORDER BY e.applyTime DESC")
    List<ExemptionApplication> findAllByStudentId(@Param("studentId") Long studentId);

    // 查询管理员待审核的免测申请
    @Query("SELECT e FROM ExemptionApplication e WHERE e.type = 'EXEMPTION' " +
           "AND (:keyword IS NULL OR " +
           "LOWER(e.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.studentNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ExemptionApplication> findPendingExemptionApplications(
        @Param("keyword") String keyword, 
        Pageable pageable
    );
    
    // 查询教师待审核的重测申请
    @Query("SELECT e FROM ExemptionApplication e WHERE e.type = 'RETEST' " +
           "AND e.status = 'PENDING' " +
           "AND (:keyword IS NULL OR " +
           "LOWER(e.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.studentNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ExemptionApplication> findPendingRetestApplications(
        @Param("keyword") String keyword, 
        Pageable pageable
    );

    @Query("SELECT e FROM ExemptionApplication e WHERE e.type = :type " +
           "AND (:keyword IS NULL OR " +
           "LOWER(e.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.studentNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ExemptionApplication> findByTypeAndKeyword(
        @Param("type") String type,
        @Param("keyword") String keyword, 
        Pageable pageable
    );

    @Query("SELECT e FROM ExemptionApplication e WHERE e.type = :type " +
           "AND e.status = :status " +
           "AND (:keyword IS NULL OR " +
           "LOWER(e.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.studentNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ExemptionApplication> findByTypeAndStatusAndKeyword(
        @Param("type") String type,
        @Param("status") String status,
        @Param("keyword") String keyword, 
        Pageable pageable
    );
} 