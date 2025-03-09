package com.sports.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
<<<<<<< HEAD
=======
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
>>>>>>> 9aa70dfb89326d075348adb786f9fca620904233

@Data
@Entity
@Table(name = "test_record")
public class TestRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
<<<<<<< HEAD

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sports_item_id", nullable = false)
    private SportsItem sportsItem;

    @Column(nullable = false)
    private Double score;

    @Column(name = "test_time", nullable = false)
    private LocalDateTime testTime;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "review_comment")
    private String reviewComment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @Column(name = "is_abnormal")
    private Boolean isAbnormal = false;

    @Column(name = "abnormal_reason")
    private String abnormalReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

=======
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User teacher;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sports_item_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SportsItem sportsItem;
    
    private Double score;
    
    @Column(name = "test_time")
    private LocalDateTime testTime;
    
    public static final String STATUS_PENDING = "PENDING";    // 待审核
    public static final String STATUS_APPROVED = "APPROVED";  // 已通过
    public static final String STATUS_REJECTED = "REJECTED";  // 已驳回
    
    @Column(name = "status")
    private String status = STATUS_PENDING;
    
    @Column(name = "remark")
    private String remark;
    
    @Column(name = "review_comment")
    private String reviewComment;
    
    @Column(name = "review_time")
    private LocalDateTime reviewTime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
>>>>>>> 9aa70dfb89326d075348adb786f9fca620904233
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
<<<<<<< HEAD

=======
    
>>>>>>> 9aa70dfb89326d075348adb786f9fca620904233
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
<<<<<<< HEAD
}
=======
} 
>>>>>>> 9aa70dfb89326d075348adb786f9fca620904233
