package com.sports.service;

import com.sports.entity.TestRecord;
import com.sports.entity.ExemptionApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
} 