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

    @Column(name = "student_number")
    private String studentNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_number", referencedColumnName = "student_number", insertable = false, updatable = false)
    private User student;

    @Column(name = "class_name")
    private String className;

    @Column(name = "sports_item_id")
    private Long sportsItemId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sports_item_id", referencedColumnName = "id", insertable = false, updatable = false)
    private SportsItem sportsItem;

    @Column(name = "type")
    private String type; // EXEMPTION: 免测, RETEST: 重测

    @Column(name = "reason")
    private String reason;

    @Column(name = "status")
    private String status; // PENDING_TEACHER: 待教师审核, PENDING_ADMIN: 待管理员审核, 
                          // APPROVED: 已通过, REJECTED_TEACHER: 教师驳回, REJECTED: 最终驳回

    @Column(name = "review_comment")
    private String reviewComment;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "teacher_review_comment")
    private String teacherReviewComment;

    @Column(name = "admin_review_comment")
    private String adminReviewComment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "apply_time")
    private LocalDateTime applyTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "teacher_review_time")
    private LocalDateTime teacherReviewTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "admin_review_time")
    private LocalDateTime adminReviewTime;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 