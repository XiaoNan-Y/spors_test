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
} 