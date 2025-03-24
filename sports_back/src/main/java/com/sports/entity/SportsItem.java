package com.sports.entity;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sports_item")
public class SportsItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    private String unit;
    private String type;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "excellent_score")
    private Double excellentScore;
    
    @Column(name = "pass_score")
    private Double passScore;
    
    @Column(name = "standard_requirement")
    private String standardRequirement;
    
    @Column(name = "test_method")
    private String testMethod;
    
    private String location;
    private String standard;
    private String status;
    
    @Column(name = "test_time")
    private String testTime;
    
    private String deadline;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "score_rule")
    private String scoreRule;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    public SportsItem() {
    }

    public SportsItem(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}