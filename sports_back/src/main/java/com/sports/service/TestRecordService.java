package com.sports.service;

import com.sports.entity.TestRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

public interface TestRecordService {
    
    Page<TestRecord> getRecordList(String status, Long teacherId, Long sportsItemId, 
                                 LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    TestRecord save(TestRecord record);
    
    TestRecord updateRecord(TestRecord record);
    
    TestRecord reviewRecord(Long id, String status, String reviewComment, Long reviewerId);
    
    boolean checkAbnormalScore(TestRecord record);
    
    String getAbnormalReason(TestRecord record);
}