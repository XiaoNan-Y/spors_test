package com.sports.dto;

import lombok.Data;
import java.util.List;

@Data
public class SportsItemStandardDTO {
    private Long id;
    private String name;
    private String description;
    private String notes;
    private List<StandardDetail> standards;
    
    @Data
    public static class StandardDetail {
        private String grade;
        private String scoreRange;
        private String requirement;
    }
} 