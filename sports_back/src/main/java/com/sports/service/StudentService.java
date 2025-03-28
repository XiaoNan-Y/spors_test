package com.sports.service;

import com.sports.entity.TestRecord;
import com.sports.entity.ExemptionApplication;
import com.sports.dto.TestRecordDTO;
import com.sports.dto.ScoreAppealDTO;
import com.sports.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface StudentService {
    /**
     * 获取学生仪表盘统计数据
     */
    Map<String, Object> getDashboardStats(Long userId);

    /**
     * 获取学生的测试记录
     */
    Page<TestRecord> getTestRecords(Long userId, Long sportsItemId, Pageable pageable);

    /**
     * 提交免测/重测申请
     */
    ExemptionApplication submitExemption(ExemptionApplication application);

    /**
     * 获取学生的免测/重测申请列表
     */
    Page<ExemptionApplication> getExemptions(Long userId, Pageable pageable);

    Page<TestRecordDTO> getStudentTestRecords(Long userId, String status, Pageable pageable);

    // 添加成绩申诉相关的方法
    List<TestRecordDTO> getAppealableRecords(Long userId);
    
    ScoreAppealDTO submitAppeal(ScoreAppealDTO appealDTO, Long userId);
    
    Page<ScoreAppealDTO> getStudentAppeals(Long userId, String status, Pageable pageable);

    Page<Notice> getNotices(String keyword, String type, Pageable pageable);

    ExemptionApplication createExemptionApplication(ExemptionApplication application, Long userId);
} 