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
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "TestRecord.withSportsItem",
        attributeNodes = {
            @NamedAttributeNode("sportsItem"),
            @NamedAttributeNode("student")
        }
    )
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TestRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_number", length = 50)
    private String studentNumber;

    @Column(name = "sports_item_id")
    private Long sportsItemId;

    @Column(nullable = false)
    private Double score;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(length = 20)
    private String status;

    @Column(name = "review_comment")
    private String reviewComment;

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @Column(name = "student_name", length = 20)
    private String studentName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_number", referencedColumnName = "student_number", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sports_item_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SportsItem sportsItem;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public SportsItem getSportsItem() {
        return sportsItem;
    }

    public void setSportsItem(SportsItem sportsItem) {
        this.sportsItem = sportsItem;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
        if (student != null) {
            this.studentNumber = student.getStudentNumber();
            this.studentName = student.getRealName();
            this.className = student.getClassName();
        }
    }
} 