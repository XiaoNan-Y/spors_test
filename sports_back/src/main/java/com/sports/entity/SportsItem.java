package com.sports.entity;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "sports_item")
public class SportsItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 50)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false, length = 20)
    private String unit;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "excellent_score")
    private Double excellentScore;  // 优秀分数线

    @Column(name = "pass_score")
    private Double passScore;      // 及格分数线

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