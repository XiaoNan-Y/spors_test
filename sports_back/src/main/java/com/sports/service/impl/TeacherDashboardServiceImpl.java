package com.sports.service.impl;

import com.sports.dto.TeacherDashboardDTO;
import com.sports.repository.*;
import com.sports.service.TeacherDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class TeacherDashboardServiceImpl implements TeacherDashboardService {

    @Autowired
    private TestRecordRepository testRecordRepository;

    @Autowired
    private ExemptionApplicationRepository exemptionApplicationRepository;

    @Autowired
    private NoticeRepository noticeRepository;
    
    @Autowired
    private ClassRepository classRepository;

    @Override
    public TeacherDashboardDTO getDashboardStats() {
        TeacherDashboardDTO stats = new TeacherDashboardDTO();
        
        // 获取当前时间和一周前的时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minus(7, ChronoUnit.DAYS);
        LocalDateTime monthAgo = now.minus(30, ChronoUnit.DAYS);

        // 测试完成率和及格率
        Double completionRate = testRecordRepository.calculateTestCompletionRate();
        Double passRate = testRecordRepository.calculatePassRate();
        stats.setTestCompletionRate(completionRate != null ? completionRate : 0.0);
        stats.setPassRate(passRate != null ? passRate : 0.0);

        // 成绩管理相关统计
        stats.setPendingRecords(testRecordRepository.countPendingRecords());
        stats.setWeeklyRecords(testRecordRepository.countRecordsSince(weekAgo));
        stats.setMonthlyTests(testRecordRepository.countRecordsBetween(monthAgo, now));

        // 审核统计
        stats.setPendingReviews(exemptionApplicationRepository.countPendingApplications());
        stats.setWeeklyReviewed(exemptionApplicationRepository.countReviewedSince(weekAgo));

        // 通知统计
        stats.setLatestNotices(noticeRepository.countActiveNotices());
        stats.setWeeklyNotices(noticeRepository.countNoticesSince(weekAgo));

        // 班级和学生统计
        stats.setClassCount(testRecordRepository.countDistinctClasses());
        stats.setTotalStudents(testRecordRepository.countDistinctStudents());
        stats.setActiveClasses(classRepository.countActiveClasses());
        stats.setStudentCount(testRecordRepository.countDistinctStudents());
        
        return stats;
    }
} 