package com.sports.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "sports_item")
public class SportsItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private String unit; // 计量单位：秒、米、个等
    
    @Column(nullable = false)
    private String type; // 类型：田赛、径赛等
    
    private Boolean isActive = true; // 是否启用
} 