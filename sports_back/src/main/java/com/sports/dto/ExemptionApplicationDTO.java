package com.sports.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExemptionApplicationDTO {
    private Long id;
    private Long studentId;
    private String studentNumber;
    private String studentName;
    private String className;
    private String type;  // EXEMPTION-免测, RETEST-重测
    private String reason;
    private String status;  // PENDING_TEACHER-待教师审核, PENDING_ADMIN-待管理员审核, APPROVED-已通过, REJECTED_TEACHER-教师驳回, REJECTED_ADMIN-管理员驳回
    private String teacherReviewComment;
    private LocalDateTime teacherReviewTime;
    private String adminReviewComment;
    private LocalDateTime adminReviewTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String attachmentUrl;
    private Long sportsItemId;
    private String sportsItemName;

    // 添加体育项目相关字段
    private String sportsItemUnit;
    private String sportsItemDescription;

    // 添加学生相关字段
    private String studentGrade;
    private String studentMajor;

    // 添加状态显示文本
    public String getStatusText() {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "待审核";
            case "APPROVED": return "已通过";
            case "REJECTED": return "已驳回";
            case "PENDING_TEACHER": return "待教师审核";
            case "PENDING_ADMIN": return "待管理员审核";
            case "REJECTED_TEACHER": return "教师已驳回";
            default: return status;
        }
    }

    // 添加类型显示文本
    public String getTypeText() {
        if (type == null) return "";
        switch (type) {
            case "EXEMPTION": return "免测";
            case "RETEST": return "重测";
            default: return type;
        }
    }
} 