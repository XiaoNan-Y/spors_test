package com.sports.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Entity
@Table(name = "exemption_application")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ExemptionApplication {
    
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

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "status")
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "apply_time")
    private LocalDateTime applyTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "attachment_url")
    private String attachmentUrl;  // 证明材料URL

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "review_comment")
    private String reviewComment;

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reviewer_name")
    private String reviewerName;

    @Column(name = "sports_item_name")
    private String sportsItemName;

    @Column(name = "type")
    private String type; // EXEMPTION(免测) 或 RETEST(重测)

    @Column(name = "admin_review_comment")
    private String adminReviewComment;

    @Column(name = "admin_review_time")
    private LocalDateTime adminReviewTime;

    @Column(name = "teacher_review_comment")
    private String teacherReviewComment;

    @Column(name = "teacher_review_time")
    private LocalDateTime teacherReviewTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sports_item_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SportsItem sportsItem;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        applyTime = now;
        updateTime = now;
        status = "PENDING";
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
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public SportsItem getSportsItem() {
        return sportsItem;
    }

    public void setSportsItem(SportsItem sportsItem) {
        this.sportsItem = sportsItem;
    }

    public Long getSportsItemId() {
        return sportsItem != null ? sportsItem.getId() : null;
    }

    public void setSportsItemId(Long sportsItemId) {
        if (sportsItemId != null) {
            if (this.sportsItem == null) {
                this.sportsItem = new SportsItem();
            }
            this.sportsItem.setId(sportsItemId);
        } else {
            this.sportsItem = null;
        }
    }

    @JsonProperty("sportsItem")
    public Map<String, Object> getSportsItemInfo() {
        Map<String, Object> info = new HashMap<>();
        if (sportsItem != null) {
            info.put("id", sportsItem.getId());
            info.put("name", sportsItem.getName());
            info.put("unit", sportsItem.getUnit());
        }
        return info;
    }
} 