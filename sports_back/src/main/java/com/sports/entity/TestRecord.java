package com.sports.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "test_record")
public class TestRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    @ManyToOne
    @JoinColumn(name = "sports_item_id", nullable = false)
    private SportsItem sportsItem;
    
    @Column(nullable = false)
    private Double score; // 成绩
    
    @Column(nullable = false)
    private LocalDateTime testTime; // 测试时间
    
    private String remark; // 备注
} 