package com.sports.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "test_exemption")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ExemptionApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_number")
    private String studentNumber;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "class_name")
    private String className;

    @Column(name = "type")
    private String type; // EXEMPTION(免测) 或 RETEST(重测)

    @Column(length = 500)
    private String reason;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "status", length = 20)
    private String status;

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

    @Column(name = "sports_item_id")
    private Long sportsItemId;

    @JsonIgnore
    @Transient
    private Long studentId;

    @Column(name = "apply_time")
    private LocalDateTime applyTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        applyTime = LocalDateTime.now();
        if (status == null) {
            status = "PENDING_TEACHER";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setStudent(User student) {
        this.student = student;
        if (student != null) {
            this.studentId = student.getId();
            this.studentNumber = student.getStudentNumber();
            this.studentName = student.getRealName();
            this.className = student.getClassName();
        }
    }

    public Long getStudentId() {
        return student != null ? student.getId() : studentId;
    }
} 