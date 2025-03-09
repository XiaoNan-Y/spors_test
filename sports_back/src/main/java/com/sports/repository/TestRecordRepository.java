package com.sports.repository;

import com.sports.entity.TestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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
} 