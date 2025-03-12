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
public class TestRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_number")
    private String studentNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_number", referencedColumnName = "student_number", insertable = false, updatable = false)
    private User student;

    @Column(name = "sports_item_id")
    private Long sportsItemId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sports_item_id", referencedColumnName = "id", insertable = false, updatable = false)
    private SportsItem sportsItem;

    private Double score;

    private String status;

    @Column(name = "class_name")
    private String className;

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

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }
} 