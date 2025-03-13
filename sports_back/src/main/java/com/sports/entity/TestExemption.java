package com.sports.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "test_exemption")
public class TestExemption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "sports_item_id")
    private Long sportsItemId;

    @Column(length = 20)
    private String className;

    @Column(length = 20)
    private String studentName;

    @Column(length = 20)
    private String studentNumber;

    @Column(length = 20)
    private String type; // EXEMPTION-免测, RETEST-重测

    @Column(length = 500)
    private String reason;

    @Column(length = 100)
    private String attachmentUrl; // 附件URL

    @Column(length = 20)
    private String status; // PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝

    @Column(length = 500)
    private String reviewComment; // 审核意见

    @Column(name = "teacher_review_comment")
    private String teacherReviewComment; // 教师审核意见

    @Column(name = "admin_review_comment")
    private String adminReviewComment; // 管理员审核意见

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "apply_time")
    private LocalDateTime applyTime; // 申请时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "review_time")
    private LocalDateTime reviewTime; // 审核时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "teacher_review_time")
    private LocalDateTime teacherReviewTime; // 教师审核时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "admin_review_time")
    private LocalDateTime adminReviewTime; // 管理员审核时间

    @Column(name = "reviewer_id")
    private Long reviewerId; // 审核人ID

    @Column(name = "reviewer_name")
    private String reviewerName; // 审核人姓名

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        applyTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
} 