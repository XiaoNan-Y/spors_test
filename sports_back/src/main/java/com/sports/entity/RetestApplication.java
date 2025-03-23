package com.sports.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "retest_application")
public class RetestApplication {
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

    @Column(name = "sports_item_name")
    private String sportsItemName;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "status")
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "apply_time")
    private LocalDateTime applyTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "teacher_review_comment")
    private String teacherReviewComment;

    @Column(name = "teacher_review_time")
    private LocalDateTime teacherReviewTime;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reviewer_name")
    private String reviewerName;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        applyTime = now;
        updateTime = now;
        status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
} 