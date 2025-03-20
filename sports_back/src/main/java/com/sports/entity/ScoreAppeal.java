package com.sports.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "score_appeal")
public class ScoreAppeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_record_id", nullable = false)
    private TestRecord testRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private Double originalScore;

    @Column(nullable = false)
    private Double expectedScore;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false)
    private String status = "PENDING";  // PENDING-待审核, APPROVED-已通过, REJECTED-已驳回

    private String reviewComment;

    @Column(nullable = false)
    private LocalDateTime createTime;

    private LocalDateTime reviewTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;
} 