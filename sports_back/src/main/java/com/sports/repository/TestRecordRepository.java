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

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRecordRepository extends JpaRepository<TestRecord, Long>, JpaSpecificationExecutor<TestRecord> {
    long countByStatus(String status);

    @Query("SELECT COUNT(t) FROM TestRecord t")
    long getTestCount();

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
    List<TestRecord> findByStudentNumberAndSportsItemId(String studentNumber, Long sportsItemId);
    
    // 根据学号查找记录
    List<TestRecord> findByStudentNumber(String studentNumber);
    
    // 根据状态查找记录
    List<TestRecord> findByStatus(String status);
    
    // 根据状态和体育项目ID查找记录
    List<TestRecord> findByStatusAndSportsItemId(String status, Long sportsItemId);
    
    // 根据学号和状态查找记录
    @Query("SELECT t FROM TestRecord t WHERE t.studentNumber = :studentNumber AND t.status = :status")
    List<TestRecord> findByStudentNumberAndStatus(
        @Param("studentNumber") String studentNumber, 
        @Param("status") String status
    );

    // 根据学号和体育项目ID查询最新记录
    @Query("SELECT t FROM TestRecord t WHERE t.studentNumber = :studentNumber AND t.sportsItemId = :sportsItemId " +
           "ORDER BY t.createdAt DESC")
    List<TestRecord> findLatestByStudentNumberAndSportsItemId(
        @Param("studentNumber") String studentNumber,
        @Param("sportsItemId") Long sportsItemId,
        Pageable pageable
    );

    @Query("SELECT t FROM TestRecord t LEFT JOIN t.student WHERE " +
           "(:status is null or t.status = :status) AND " +
           "(:sportsItemId is null or t.sportsItemId = :sportsItemId) " +
           "ORDER BY t.studentNumber, t.createdAt DESC")
    Page<TestRecord> findAllWithStudent(
        @Param("status") String status,
        @Param("sportsItemId") Long sportsItemId,
        Pageable pageable
    );

    @Query("SELECT DISTINCT t FROM TestRecord t " +
           "LEFT JOIN t.student s " +
           "LEFT JOIN t.sportsItem si " +
           "WHERE (:sportsItemId IS NULL OR t.sportsItemId = :sportsItemId) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:keyword IS NULL OR " +
           "LOWER(s.realName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.studentNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<TestRecord> findAllWithFilters(
        @Param("sportsItemId") Long sportsItemId,
        @Param("status") String status,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    @Query("SELECT DISTINCT t FROM TestRecord t " +
           "LEFT JOIN t.sportsItem " +
           "WHERE (:className IS NULL OR :className = '' OR t.className = :className) " +
           "AND (:sportsItemId IS NULL OR t.sportsItemId = :sportsItemId) " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(t.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.studentNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.createdAt DESC")
    Page<TestRecord> findByFiltersForTeacher(
        @Param("className") String className,
        @Param("sportsItemId") Long sportsItemId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    @Query("SELECT DISTINCT t FROM TestRecord t " +
           "LEFT JOIN FETCH t.sportsItem si " +
           "WHERE (:className IS NULL OR :className = '' OR t.className = :className) " +
           "AND (:sportsItemId IS NULL OR t.sportsItemId = :sportsItemId) " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(t.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.studentNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.createdAt DESC")
    List<TestRecord> findByFiltersForExport(
        @Param("className") String className,
        @Param("sportsItemId") Long sportsItemId,
        @Param("keyword") String keyword
    );

    @EntityGraph(value = "TestRecord.withSportsItem")
    @Query("SELECT DISTINCT tr FROM TestRecord tr " +
           "LEFT JOIN tr.sportsItem si " +
           "LEFT JOIN tr.student s " +
           "WHERE (:className IS NULL OR :className = '' OR tr.className = :className) " +
           "AND (:sportsItemId IS NULL OR tr.sportsItemId = :sportsItemId) " +
           "AND (:status IS NULL OR :status = '' OR tr.status = :status) " +
           "AND (:studentNumber IS NULL OR :studentNumber = '' OR tr.studentNumber = :studentNumber) " +
           "ORDER BY tr.createdAt DESC")
    Page<TestRecord> findByFilters(
        @Param("className") String className,
        @Param("sportsItemId") Long sportsItemId,
        @Param("status") String status,
        @Param("studentNumber") String studentNumber,
        Pageable pageable
    );

    @Query("SELECT DISTINCT t.className FROM TestRecord t WHERE t.className IS NOT NULL ORDER BY t.className")
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
           "AND (:sportsItemId IS NULL OR tr.sportsItemId = :sportsItemId) " +
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
} 