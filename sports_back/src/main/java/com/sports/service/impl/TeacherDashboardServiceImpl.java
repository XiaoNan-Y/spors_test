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

    @Override
    public TeacherDashboardDTO getDashboardStats() {
        TeacherDashboardDTO stats = new TeacherDashboardDTO();
        
        try {
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
            Integer pendingRecords = testRecordRepository.countPendingRecords();
            stats.setPendingRecords(pendingRecords != null ? pendingRecords : 0);
            
            Integer weeklyRecords = testRecordRepository.countRecordsSince(weekAgo);
            stats.setWeeklyRecords(weeklyRecords != null ? weeklyRecords : 0);
            
            Integer monthlyTests = testRecordRepository.countRecordsBetween(monthAgo, now);
            stats.setMonthlyTests(monthlyTests != null ? monthlyTests : 0);

            // 审核统计
            Integer pendingReviews = exemptionApplicationRepository.countPendingApplications();
            stats.setPendingReviews(pendingReviews != null ? pendingReviews : 0);
            
            Integer weeklyReviewed = exemptionApplicationRepository.countReviewedSince(weekAgo);
            stats.setWeeklyReviewed(weeklyReviewed != null ? weeklyReviewed : 0);

            // 通知统计
            Integer latestNotices = noticeRepository.countActiveNotices();
            stats.setLatestNotices(latestNotices != null ? latestNotices : 0);
            
            Integer weeklyNotices = noticeRepository.countNoticesSince(weekAgo);
            stats.setWeeklyNotices(weeklyNotices != null ? weeklyNotices : 0);

            // 班级和学生统计 - 直接从 test_record 表获取
            Integer classCount = testRecordRepository.countDistinctClasses();
            stats.setClassCount(classCount != null ? classCount : 0);
            
            Integer totalStudents = testRecordRepository.countDistinctStudents();
            stats.setTotalStudents(totalStudents != null ? totalStudents : 0);
            
            // 不再使用 ClassRepository
            stats.setActiveClasses(classCount);
            stats.setStudentCount(totalStudents);

        } catch (Exception e) {
            e.printStackTrace();
            // 确保即使出错也返回一个有效的对象
            stats = new TeacherDashboardDTO();
        }
        
        return stats;
    }
} 