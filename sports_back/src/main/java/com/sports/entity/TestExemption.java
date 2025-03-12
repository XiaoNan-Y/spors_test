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

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private String type; // EXEMPTION(免测) 或 RETEST(重测)

    @Column(nullable = false)
    private String status; // PENDING_TEACHER(待教师审核), REJECTED_TEACHER(教师驳回), PENDING_ADMIN(待管理员审核), APPROVED(通过), REJECTED(驳回)

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    @Column(name = "teacher_review_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime teacherReviewTime;

    @Column(name = "teacher_review_comment")
    private String teacherReviewComment;

    @Column(name = "admin_review_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime adminReviewTime;

    @Column(name = "admin_review_comment")
    private String adminReviewComment;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
} 