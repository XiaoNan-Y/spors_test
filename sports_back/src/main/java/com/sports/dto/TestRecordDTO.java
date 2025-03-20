package com.sports.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestRecordDTO {
    private Long id;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;  // 对应前端的测试时间
    
    private String sportsItemName;    // 体育项目名称
    private Double score;             // 成绩
    private String grade;             // 等级
    private String status;            // 状态
    private String reviewComment;     // 审核意见
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime; // 审核时间
    
    private String className;         // 班级
    private String studentName;       // 学生姓名
    private String studentNumber;     // 学号
} 