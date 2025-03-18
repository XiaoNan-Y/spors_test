package com.sports.dto;

import lombok.Data;
import java.util.List;

@Data
public class TeacherDashboardDTO {
    // 班级统计
    private Integer classCount;
    private Integer testCompletion;
    private Double testCompletionRate;
    private Double passRate;
    private Double testParticipationRate;
    
    // 成绩管理
    private Integer pendingRecords;
    private Integer weeklyRecords;
    private Integer monthlyTests;
    
    // 审核统计
    private Integer pendingReviews;
    private Integer weeklyReviewed;
    private Integer reviewTrend;
    
    // 学生统计
    private Integer totalStudents;
    private Integer studentCount;
    private Integer activeClasses;
    
    // 通知统计
    private Integer latestNotices;
    private Integer weeklyNotices;
    
    // 近期活动
    private List<ActivityDTO> recentActivities;

    @Data
    public static class ActivityDTO {
        private String name;
        private String date;
    }
} 