package com.sports.dto;

import lombok.Data;

@Data
public class ClassStatisticsDTO {
    private String className;        // 班级名称
    private Long totalCount;         // 总人数
    private Double averageScore;     // 平均分
    private Double excellentRate;    // 优秀率
    private Double passRate;         // 及格率
    private Long excellentCount;     // 优秀人数
    private Long passCount;          // 及格人数

    public ClassStatisticsDTO(String className, Long totalCount, Double averageScore, 
                            Long excellentCount, Long passCount) {
        this.className = className;
        this.totalCount = totalCount;
        this.averageScore = averageScore;
        this.excellentCount = excellentCount;
        this.passCount = passCount;
        
        // 计算优秀率和及格率
        this.excellentRate = totalCount > 0 ? (excellentCount * 100.0 / totalCount) : 0.0;
        this.passRate = totalCount > 0 ? (passCount * 100.0 / totalCount) : 0.0;
    }
} 