package com.sports.service;

import com.sports.entity.TestRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeacherService {
    List<String> getTeacherClasses(Long teacherId);
    
    Page<TestRecord> getTestRecords(Long teacherId, Long sportsItemId, 
                                   String className, String studentNumber, 
                                   Pageable pageable);
    
    TestRecord addTestRecord(Long teacherId, TestRecord record);
    
    TestRecord updateTestRecord(Long teacherId, TestRecord record);
    
    void deleteTestRecord(Long teacherId, Long recordId);
} 