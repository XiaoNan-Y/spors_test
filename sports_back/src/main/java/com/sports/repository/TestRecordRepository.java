package com.sports.repository;

import com.sports.entity.TestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TestRecordRepository extends JpaRepository<TestRecord, Long> {
    @Query("SELECT COUNT(t) FROM TestRecord t")
    long getTestCount();
    
    @Query("SELECT t.sportsItem.name, COUNT(t) FROM TestRecord t GROUP BY t.sportsItem.name")
    List<Object[]> getTestCountByItem();
    
    @Query("SELECT t.student.id, AVG(t.score) FROM TestRecord t GROUP BY t.student.id")
    List<Object[]> getAverageScoreByStudent();
} 