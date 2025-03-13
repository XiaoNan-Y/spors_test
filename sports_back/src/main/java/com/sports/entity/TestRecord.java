package com.sports.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "test_record")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TestRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Student student;

    @Column(name = "sports_item_id")
    private Long sportsItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sports_item_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SportsItem sportsItem;

    @Column(length = 20)
    private String className;

    @Column(length = 20)
    private String studentName;

    @Column(length = 20)
    private String studentNumber;

    private Double score;

    private String status;

    @Column(name = "review_comment")
    private String reviewComment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (student != null) {
            this.studentName = student.getRealName();
            this.studentNumber = student.getStudentNumber();
            this.className = student.getClassName();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (student != null) {
            this.studentName = student.getRealName();
            this.studentNumber = student.getStudentNumber();
            this.className = student.getClassName();
        }
    }

    public Long getSportsItemId() {
        return sportsItemId;
    }

    public void setSportsItemId(Long sportsItemId) {
        this.sportsItemId = sportsItemId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public SportsItem getSportsItem() {
        return sportsItem;
    }

    public void setSportsItem(SportsItem sportsItem) {
        this.sportsItem = sportsItem;
    }
} 