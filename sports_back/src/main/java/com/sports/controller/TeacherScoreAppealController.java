package com.sports.controller;

import com.sports.common.Result;
import com.sports.dto.ScoreAppealDTO;
import com.sports.service.ScoreAppealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/teacher/score-appeals")
@CrossOrigin
public class TeacherScoreAppealController {
    
    private static final Logger log = LoggerFactory.getLogger(TeacherScoreAppealController.class);
    
    @Autowired
    private ScoreAppealService appealService;

    // 获取申诉列表
    @GetMapping
    public Result getAppeals(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestAttribute Long userId
    ) {
        try {
            log.info("获取教师申诉列表 - 教师ID: {}, 状态: {}, 页码: {}, 大小: {}", userId, status, page, size);
            
            Page<ScoreAppealDTO> appeals = appealService.getTeacherAppeals(
                userId, status, PageRequest.of(page - 1, size));
                
            log.info("成功获取申诉列表，共 {} 条记录", appeals.getTotalElements());
            
            return Result.success(appeals);
        } catch (Exception e) {
            log.error("获取申诉列表失败", e);
            return Result.error("获取申诉列表失败: " + e.getMessage());
        }
    }

    // 审核申诉
    @PutMapping("/{id}/review")
    public Result reviewAppeal(
        @PathVariable Long id,
        @RequestBody ScoreAppealDTO appealDTO,
        @RequestAttribute Long userId
    ) {
        try {
            log.info("审核申诉 - 申诉ID: {}, 教师ID: {}, 状态: {}", id, userId, appealDTO.getStatus());
            
            ScoreAppealDTO updated = appealService.reviewAppeal(id, appealDTO, userId);
            
            log.info("申诉审核成功 - 申诉ID: {}", id);
            
            return Result.success(updated);
        } catch (Exception e) {
            log.error("审核申诉失败", e);
            return Result.error("审核申诉失败: " + e.getMessage());
        }
    }
} 