package com.sports.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notice")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;  // TEST_SCHEDULE, SCORE_RELEASE, SYSTEM_MAINTENANCE, OTHER

    @Column(nullable = false)
    private String priority;  // HIGH, NORMAL, LOW

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    private Integer status = 1;  // 1: 启用, 0: 禁用

    @Column(name = "create_by")
    private Long createBy;

    @CreationTimestamp
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}