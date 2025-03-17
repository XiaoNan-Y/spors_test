package com.sports.dto;

import lombok.Data;

@Data
public class TeacherDashboardDTO {
    private Integer pendingReviews; // 待审核申请数
    private Integer monthlyTests; // 本月测试人数
    private Integer testCompletion; // 测试完成率
    private Integer classCount; // 班级数量
    private Integer activeClasses; // 活跃班级数
    private Integer studentCount; // 学生总数
    private Integer reviewTrend; // 审核趋势
    private Double testParticipationRate; // 参测率
}