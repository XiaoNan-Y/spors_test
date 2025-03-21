package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.Feedback;
import com.sports.service.FeedbackService;
import com.sports.dto.FeedbackDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin/feedback")
@CrossOrigin
public class AdminFeedbackController {
    
    private static final Logger log = LoggerFactory.getLogger(AdminFeedbackController.class);
    
    @Autowired
    private FeedbackService feedbackService;

    // 获取反馈列表
    @GetMapping
    public Result getFeedbacks(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        try {
            log.info("Getting feedbacks - type: {}, status: {}, page: {}, size: {}", 
                    type, status, page, size);
            
            Page<FeedbackDTO> feedbacks = feedbackService.getAdminFeedbacks(
                type, status, PageRequest.of(page - 1, size));
                
            log.info("Found {} feedbacks", feedbacks.getTotalElements());
            
            return Result.success(feedbacks);
        } catch (Exception e) {
            log.error("获取反馈列表失败", e);
            return Result.error("获取反馈列表失败：" + e.getMessage());
        }
    }

    // 回复反馈
    @PutMapping("/{id}/reply")
    public Result replyFeedback(
        @PathVariable Long id,
        @RequestBody Feedback feedback,
        @RequestAttribute Long userId
    ) {
        try {
            FeedbackDTO updatedFeedback = feedbackService.replyFeedback(id, feedback.getReply(), userId);
            return Result.success(updatedFeedback);
        } catch (Exception e) {
            log.error("回复反馈失败", e);
            return Result.error("回复反馈失败：" + e.getMessage());
        }
    }

    // 更新反馈状态
    @PutMapping("/{id}/status")
    public Result updateStatus(
        @PathVariable Long id,
        @RequestParam String status,
        @RequestAttribute Long userId
    ) {
        try {
            FeedbackDTO updatedFeedback = feedbackService.updateFeedbackStatus(id, status, userId);
            return Result.success(updatedFeedback);
        } catch (Exception e) {
            log.error("更新反馈状态失败", e);
            return Result.error("更新反馈状态失败：" + e.getMessage());
        }
    }
} 