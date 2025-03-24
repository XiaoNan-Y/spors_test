package com.sports.dto;

import lombok.Data;
import java.util.List;

@Data
public class SportsItemStandardDTO {
    private Long id;
    private String name;
    private String description;
    private String unit;
    private String type;
    private String testMethod;
    private String location;
    private String testTime;
    private String standard;
    private Boolean isActive;
    private Double excellentScore;
    private Double passScore;
    private String standardRequirement;
    private String status;
    private String deadline;
    private String scoreRule;
} 