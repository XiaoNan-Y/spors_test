package com.sports.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notice")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private String type;  // TEST_SCHEDULE, SCORE_RELEASE, SYSTEM_MAINTENANCE, OTHER

    @Column(nullable = false)
    private String priority;  // HIGH, NORMAL

    @Column(nullable = false)
    private Integer status;  // 0-未发布, 1-已发布

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "create_by")
    private User createBy;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}