package com.sports.repository;

import com.sports.entity.TestRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
} 