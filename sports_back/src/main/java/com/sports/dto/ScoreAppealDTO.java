package com.sports.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScoreAppealDTO {
    private Long id;
    private Long testRecordId;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private String className;
    private String testItem;
    private String sportsItemName;
    private Double originalScore;
    private Double expectedScore;
    private String reason;
    private String status;
    private String reviewComment;
    private LocalDateTime createTime;
    private LocalDateTime reviewTime;
    private String reviewByName;
    private String reviewerName;
} 