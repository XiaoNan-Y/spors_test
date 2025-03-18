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

    @Column(name = "student_number")
    private String studentNumber;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "class_name")
    private String className;

    @Column(name = "sports_item_id")
    private Long sportsItemId;

    private String type;
    private String reason;
    private String status;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "apply_time")
    private LocalDateTime applyTime;

    @Column(name = "teacher_review_comment")
    private String teacherReviewComment;

    @Column(name = "teacher_review_time")
    private LocalDateTime teacherReviewTime;

    @Column(name = "admin_review_comment")
    private String adminReviewComment;

    @Column(name = "admin_review_time")
    private LocalDateTime adminReviewTime;

    @Column(name = "review_comment")
    private String reviewComment;

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reviewer_name")
    private String reviewerName;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        updateTime = now;
        createdAt = now;
        updatedAt = now;
        applyTime = now;
        if (status == null) {
            status = "PENDING_TEACHER";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
} 