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
} 