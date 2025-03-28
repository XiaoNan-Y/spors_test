package com.sports.repository;

import com.sports.entity.TestRecord;
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
import java.util.Optional;

@Repository
public interface TestRecordRepository extends JpaRepository<TestRecord, Long>, JpaSpecificationExecutor<TestRecord> {
    long countByStatus(String status);

    @Query("SELECT COUNT(t) FROM TestRecord t WHERE t.createdAt >= :startTime")
    int countByCreateTimeAfter(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT COUNT(t) FROM TestRecord t WHERE t.className = :className AND t.createdAt >= :startTime")
    int countByClassNameAndCreateTimeAfter(@Param("className") String className, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT COUNT(DISTINCT t.studentNumber) FROM TestRecord t WHERE t.createdAt >= :startTime")
    int countDistinctStudentsByCreateTimeAfter(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT COUNT(t) FROM TestRecord t")
    long getTestCount();
    
    // 统计不重复的学号数量
    @Query("SELECT COUNT(DISTINCT t.studentNumber) FROM TestRecord t")
    long countDistinctStudentNumber();
    
    // 统计指定时间之后的记录数
    @Query("SELECT COUNT(t) FROM TestRecord t WHERE t.createdAt >= :startTime")
    long countByCreatedAtAfter(@Param("startTime") LocalDateTime startTime);
    
    // 统计不重复的班级数量
    @Query("SELECT COUNT(DISTINCT t.className) FROM TestRecord t")
    long countDistinctClassName();

    @Query("SELECT t.sportsItem.name, COUNT(t) " +
           "FROM TestRecord t " +
           "GROUP BY t.sportsItem.name")
    List<Object[]> getTestCountByItem();

    @Query("SELECT t.student.realName, AVG(t.score) " +
           "FROM TestRecord t " +
           "WHERE t.status = 'APPROVED' " +
           "GROUP BY t.student.id, t.student.realName")
    List<Object[]> getAverageScoreByStudent();

    // 根据学号和体育项目ID查找记录
    @Query("SELECT t FROM TestRecord t " +
           "LEFT JOIN t.sportsItem si " +
           "WHERE t.studentNumber = :studentNumber AND si.id = :sportsItemId")
    List<TestRecord> findByStudentNumberAndSportsItemId(
        @Param("studentNumber") String studentNumber,
        @Param("sportsItemId") Long sportsItemId
    );
    
    // 根据学号查找记录
    List<TestRecord> findByStudentNumber(String studentNumber);
    
    // 根据状态查找记录
    List<TestRecord> findByStatus(String status);
    
    // 根据状态和体育项目ID查找记录
    @Query("SELECT t FROM TestRecord t " +
           "LEFT JOIN t.sportsItem si " +
           "WHERE t.status = :status AND si.id = :sportsItemId")
    List<TestRecord> findByStatusAndSportsItemId(
        @Param("status") String status,
        @Param("sportsItemId") Long sportsItemId
    );
    
    // 根据学号和状态查找记录
    @Query("SELECT t FROM TestRecord t WHERE t.studentNumber = :studentNumber AND t.status = :status")
    List<TestRecord> findByStudentNumberAndStatus(
        @Param("studentNumber") String studentNumber, 
        @Param("status") String status
    );

    // 根据学号和体育项目ID查询最新记录
    @Query("SELECT t FROM TestRecord t " +
           "LEFT JOIN t.sportsItem si " +
           "WHERE t.studentNumber = :studentNumber " +
           "AND si.id = :sportsItemId " +
           "ORDER BY t.createdAt DESC")
    List<TestRecord> findLatestByStudentNumberAndSportsItemId(
        @Param("studentNumber") String studentNumber,
        @Param("sportsItemId") Long sportsItemId,
        Pageable pageable
    );

    @Query("SELECT DISTINCT t FROM TestRecord t " +
           "LEFT JOIN FETCH t.sportsItem si " +
           "WHERE (:className IS NULL OR :className = '' OR t.className = :className) " +
           "AND (:sportsItemId IS NULL OR t.sportsItem.id = :sportsItemId) " +
           "AND (:status IS NULL OR :status = '' OR t.status = :status) " +
           "AND (:studentNumber IS NULL OR :studentNumber = '' OR t.studentNumber = :studentNumber) " +
           "ORDER BY t.className, t.studentNumber, t.sportsItem.name")
    List<TestRecord> findByFiltersForExport(
        @Param("className") String className,
        @Param("sportsItemId") Long sportsItemId,
        @Param("status") String status,
        @Param("studentNumber") String studentNumber
    );

    @Query("SELECT DISTINCT tr FROM TestRecord tr " +
           "LEFT JOIN tr.sportsItem si " +
           "LEFT JOIN tr.student s " +
           "WHERE (:className IS NULL OR :className = '' OR tr.className = :className) " +
           "AND (:sportsItemId IS NULL OR si.id = :sportsItemId) " +
           "AND (:status IS NULL OR :status = '' OR tr.reviewStatus = :status) " +
           "AND (:studentNumber IS NULL OR :studentNumber = '' OR tr.studentNumber = :studentNumber) " +
           "ORDER BY tr.createdAt DESC")
    Page<TestRecord> findByFiltersWithReviewStatus(
        @Param("className") String className,
        @Param("sportsItemId") Long sportsItemId,
        @Param("status") String status,
        @Param("studentNumber") String studentNumber,
        Pageable pageable
    );

    @Query("SELECT t FROM TestRecord t " +
           "LEFT JOIN t.sportsItem si " +
           "WHERE (:status is null or t.status = :status) AND " +
           "(:sportsItemId is null or si.id = :sportsItemId) " +
           "ORDER BY t.studentNumber, t.createdAt DESC")
    Page<TestRecord> findAllWithStudent(
        @Param("status") String status,
        @Param("sportsItemId") Long sportsItemId,
        Pageable pageable
    );

    @Query("SELECT DISTINCT t FROM TestRecord t " +
           "LEFT JOIN t.sportsItem si " +
           "WHERE (:className IS NULL OR t.className = :className) " +
           "AND (:sportsItemId IS NULL OR si.id = :sportsItemId) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:studentNumber IS NULL OR " +
           "LOWER(t.studentNumber) LIKE LOWER(CONCAT('%', :studentNumber, '%')))")
    @EntityGraph(attributePaths = {"sportsItem"})
    Page<TestRecord> findByFiltersForTeacher(
        @Param("className") String className,
        @Param("sportsItemId") Long sportsItemId,
        @Param("status") String status,
        @Param("studentNumber") String studentNumber,
        Pageable pageable
    );

    @Query("SELECT DISTINCT t.className FROM TestRecord t ORDER BY t.className")
    List<String> findDistinctClassNames();

    @Query("SELECT tr.className, " +
           "COUNT(tr) as totalCount, " +
           "AVG(tr.score) as avgScore, " +
           "COUNT(CASE " +
           "    WHEN si.id IN (1, 5) AND tr.score <= si.excellentScore THEN 1 " +
           "    WHEN si.id NOT IN (1, 5) AND tr.score >= si.excellentScore THEN 1 " +
           "    ELSE NULL END) as excellentCount, " +
           "COUNT(CASE " +
           "    WHEN si.id IN (1, 5) AND tr.score <= si.passScore THEN 1 " +
           "    WHEN si.id NOT IN (1, 5) AND tr.score >= si.passScore THEN 1 " +
           "    ELSE NULL END) as passCount " +
           "FROM TestRecord tr " +
           "LEFT JOIN tr.sportsItem si " +
           "WHERE (:className IS NULL OR :className = '' OR tr.className = :className) " +
           "AND (:sportsItemId IS NULL OR si.id = :sportsItemId) " +
           "AND (:status IS NULL OR :status = '' OR tr.status = :status) " +
           "AND tr.score IS NOT NULL " +
           "GROUP BY tr.className")
    List<Object[]> getClassStatistics(
        @Param("className") String className,
        @Param("sportsItemId") Long sportsItemId,
        @Param("status") String status
    );

    @Query("SELECT t FROM TestRecord t " +
           "WHERE t.studentNumber = :studentNumber " +
           "AND t.status = 'APPROVED' " +
           "ORDER BY t.createdAt DESC")
    List<TestRecord> findStudentRecords(@Param("studentNumber") String studentNumber);

    @EntityGraph(attributePaths = {"sportsItem", "student"})
    Optional<TestRecord> findWithDetailsById(Long id);

    @Query("SELECT COUNT(t) FROM TestRecord t WHERE t.status = 'PENDING'")
    Integer countPendingRecords();

    @Query("SELECT COUNT(t) FROM TestRecord t WHERE t.createdAt >= :since")
    Integer countRecordsSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(t) FROM TestRecord t WHERE t.createdAt >= :startTime AND t.createdAt <= :endTime")
    Integer countRecordsBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query(value = "SELECT IFNULL(COUNT(CASE WHEN t.score >= 60 AND t.status = 'APPROVED' THEN 1 END) * 100.0 / " +
           "NULLIF(COUNT(CASE WHEN t.status = 'APPROVED' THEN 1 END), 0), 0) FROM test_record t", nativeQuery = true)
    Double calculatePassRate();

    @Query(value = "SELECT IFNULL(COUNT(CASE WHEN t.status = 'APPROVED' THEN 1 END) * 100.0 / " +
           "NULLIF(COUNT(*), 0), 0) FROM test_record t", nativeQuery = true)
    Double calculateTestCompletionRate();

    @Query("SELECT COUNT(DISTINCT t.className) FROM TestRecord t")
    Integer countDistinctClasses();

    @Query("SELECT COUNT(DISTINCT t.studentNumber) FROM TestRecord t")
    Integer countDistinctStudents();

    @Query("SELECT DISTINCT t FROM TestRecord t " +
           "LEFT JOIN t.student s " +
           "LEFT JOIN t.sportsItem si " +
           "WHERE (:sportsItemId IS NULL OR si.id = :sportsItemId) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:keyword IS NULL OR t.studentNumber LIKE %:keyword% " +
           "OR t.studentName LIKE %:keyword%)")
    @EntityGraph(value = "TestRecord.withDetails")
    Page<TestRecord> findAllWithFilters(
        @Param("sportsItemId") Long sportsItemId,
        @Param("status") String status,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    long countByStudentIdAndStatusNot(Long studentId, String status);
    long countByStudentIdAndStatus(Long studentId, String status);
    List<TestRecord> findTop5ByStudentIdOrderByCreatedAtDesc(Long studentId);
    Page<TestRecord> findByStudentIdAndSportsItemId(Long studentId, Long sportsItemId, Pageable pageable);
    Page<TestRecord> findByStudentId(Long studentId, Pageable pageable);
    Page<TestRecord> findByStudentIdAndStatusEquals(Long studentId, String status, Pageable pageable);

    default Page<TestRecord> findByStudentNumberAndStatus(String studentNumber, String status, Pageable pageable) {
        if (status == null || status.trim().isEmpty()) {
            return findByStudentNumber(studentNumber, pageable);
        }
        return findByStudentNumberAndStatusEquals(studentNumber, status.trim(), pageable);
    }

    Page<TestRecord> findByStudentNumber(String studentNumber, Pageable pageable);
    Page<TestRecord> findByStudentNumberAndStatusEquals(String studentNumber, String status, Pageable pageable);

    // 添加新方法
    List<TestRecord> findByStudentIdAndStatus(Long studentId, String status);

    @Query("SELECT DISTINCT t FROM TestRecord t " +
           "LEFT JOIN FETCH t.sportsItem si " +
           "WHERE t.studentNumber = :studentNumber " +
           "AND t.status = :status " +
           "ORDER BY t.createdAt DESC")
    List<TestRecord> findByStudentNumberAndStatusWithSportsItem(
        @Param("studentNumber") String studentNumber, 
        @Param("status") String status
    );

    @Query("SELECT DISTINCT t FROM TestRecord t " +
           "LEFT JOIN FETCH t.sportsItem si " +
           "LEFT JOIN FETCH t.student s " +
           "WHERE (:sportsItemId IS NULL OR si.id = :sportsItemId) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:keyword IS NULL OR t.studentNumber LIKE %:keyword% " +
           "OR t.studentName LIKE %:keyword%)")
    List<TestRecord> findAllForExport(
        @Param("sportsItemId") Long sportsItemId,
        @Param("status") String status,
        @Param("keyword") String keyword
    );

    Page<TestRecord> findByReviewStatus(String reviewStatus, Pageable pageable);

    @Query("SELECT t FROM TestRecord t WHERE " +
           "(:className IS NULL OR t.className = :className) AND " +
           "(:sportsItemId IS NULL OR t.sportsItem.id = :sportsItemId) AND " +
           "(:reviewStatus IS NULL OR t.reviewStatus = :reviewStatus) AND " +
           "(:studentNumber IS NULL OR t.studentNumber = :studentNumber)")
    Page<TestRecord> findByFilters(
        @Param("className") String className,
        @Param("sportsItemId") Long sportsItemId,
        @Param("reviewStatus") String reviewStatus,
        @Param("studentNumber") String studentNumber,
        Pageable pageable
    );
}