package com.sports.repository;

import com.sports.entity.TestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TestRecordRepository extends JpaRepository<TestRecord, Long>, JpaSpecificationExecutor<TestRecord> {
    @Query(value = "SELECT COUNT(*) FROM test_record", nativeQuery = true)
    long getTestCount();
    
    @Query(value = "SELECT si.name, COUNT(*) " +
           "FROM test_record tr " +
           "JOIN sports_item si ON tr.sports_item_id = si.id " +
           "GROUP BY si.name", nativeQuery = true)
    List<Object[]> getTestCountByItem();
    
    @Query(value = "SELECT tr.student_id, AVG(tr.score) " +
           "FROM test_record tr " +
           "GROUP BY tr.student_id", nativeQuery = true)
    List<Object[]> getAverageScoreByStudent();
} 