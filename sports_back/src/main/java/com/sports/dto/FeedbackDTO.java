package com.sports.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedbackDTO {
    private Long id;
    private String studentName;
    private String studentNumber;
    private String type;
    private String title;
    private String content;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String reply;
    private String replyByName;
    private LocalDateTime replyTime;
    private String className;
} 