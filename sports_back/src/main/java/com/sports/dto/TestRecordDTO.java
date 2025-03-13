package com.sports.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestRecordDTO {
    private Long id;
    private String studentName;
    private String studentNumber;
    private String className;
    private String sportsItemName;
    private Double score;
    private String status;
    private String reviewComment;
    private LocalDateTime reviewTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 