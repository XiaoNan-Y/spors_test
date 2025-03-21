package com.sports.service;

import com.sports.entity.Feedback;
import com.sports.dto.FeedbackDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedbackService {
    FeedbackDTO submitFeedback(Feedback feedback);
    
    Page<FeedbackDTO> getStudentFeedbacks(Long studentId, String type, Pageable pageable);
    
    FeedbackDTO getFeedbackById(Long id);
    
    void deleteFeedback(Long id, Long studentId);
    
    // 管理员获取反馈列表
    Page<FeedbackDTO> getAdminFeedbacks(String type, String status, Pageable pageable);
    
    // 回复反馈
    FeedbackDTO replyFeedback(Long id, String reply, Long adminId);
    
    // 更新反馈状态
    FeedbackDTO updateFeedbackStatus(Long id, String status, Long adminId);
} 