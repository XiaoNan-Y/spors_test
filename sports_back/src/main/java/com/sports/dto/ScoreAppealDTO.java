package com.sports.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScoreAppealDTO {
    private Long id;
    private Long testRecordId;
    private String studentName;
    private String studentNumber;
    private String sportsItemName;
    private Double originalScore;
    private Double expectedScore;
    private String reason;
    private String status;
    private String reviewComment;
    private LocalDateTime createTime;
    private LocalDateTime reviewTime;
    private String reviewerName;
} 