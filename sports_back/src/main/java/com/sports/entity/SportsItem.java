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
    
    private String name;
    
    private String description;
    
    private String unit;
    
    private String type;
    
    @Column(name = "is_active")
    private Boolean isActive;

    public SportsItem() {
    }

    public SportsItem(Long id) {
        this.id = id;
    }
}