package com.sports.repository;

import com.sports.entity.TestExemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestExemptionRepository extends JpaRepository<TestExemption, Long>, JpaSpecificationExecutor<TestExemption> {
    // 统计指定时间段内的记录数
    @Query("SELECT COUNT(t) FROM TestExemption t WHERE t.createdAt BETWEEN :startTime AND :endTime")
    long countByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 统计指定时间之后的记录数
    @Query("SELECT COUNT(t) FROM TestExemption t WHERE t.createdAt >= :startTime")
    long countByCreatedAtAfter(@Param("startTime") LocalDateTime startTime);
    
    // 统计指定状态的记录数
    long countByStatus(String status);
    
    @Query(value = "SELECT e FROM TestExemption e WHERE " +
           "(:className IS NULL OR :className = '' OR e.className = :className) AND " +
           "(:type IS NULL OR :type = '' OR e.type = :type) AND " +
           "(:status IS NULL OR :status = '' OR e.status = :status) AND " +
           "(:studentNumber IS NULL OR :studentNumber = '' OR e.studentNumber = :studentNumber) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(e.studentName) LIKE LOWER(CONCAT('%',:keyword,'%')) OR " +
           "LOWER(e.studentNumber) LIKE LOWER(CONCAT('%',:keyword,'%'))) " +
           "ORDER BY e.createTime DESC",
           countQuery = "SELECT COUNT(e) FROM TestExemption e WHERE " +
           "(:className IS NULL OR :className = '' OR e.className = :className) AND " +
           "(:type IS NULL OR :type = '' OR e.type = :type) AND " +
           "(:status IS NULL OR :status = '' OR e.status = :status) AND " +
           "(:studentNumber IS NULL OR :studentNumber = '' OR e.studentNumber = :studentNumber) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(e.studentName) LIKE LOWER(CONCAT('%',:keyword,'%')) OR " +
           "LOWER(e.studentNumber) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    Page<TestExemption> findByFilters(
        @Param("className") String className,
        @Param("type") String type,
        @Param("status") String status,
        @Param("studentNumber") String studentNumber,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    @Query("SELECT DISTINCT e.className FROM TestExemption e ORDER BY e.className")
    List<String> findDistinctClassNames();

    @Query("SELECT COUNT(e) FROM TestExemption e WHERE e.createTime BETWEEN :startTime AND :endTime")
    int countByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(e) FROM TestExemption e WHERE e.createTime >= :startTime")
    int countByCreateTimeAfter(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT COUNT(e) FROM TestExemption e")
    long countAll();

    @Query("SELECT e FROM TestExemption e WHERE " +
           "(:className IS NULL OR :className = '' OR e.className = :className) AND " +
           "(:type IS NULL OR :type = '' OR e.type = :type) AND " +
           "(:status IS NULL OR :status = '' OR e.status = :status) AND " +
           "(:studentNumber IS NULL OR :studentNumber = '' OR e.studentNumber = :studentNumber) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(e.studentName) LIKE LOWER(CONCAT('%',:keyword,'%')) OR " +
           "LOWER(e.studentNumber) LIKE LOWER(CONCAT('%',:keyword,'%'))) " +
           "ORDER BY e.createTime DESC")
    List<TestExemption> findByFiltersForExport(
        @Param("className") String className,
        @Param("type") String type,
        @Param("status") String status,
        @Param("studentNumber") String studentNumber,
        @Param("keyword") String keyword
    );
}