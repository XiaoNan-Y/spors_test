package com.sports.service.impl;

import com.sports.dto.FeedbackDTO;
import com.sports.entity.Feedback;
import com.sports.entity.User;
import com.sports.repository.FeedbackRepository;
import com.sports.repository.UserRepository;
import com.sports.service.FeedbackService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public FeedbackDTO submitFeedback(Feedback feedback) {
        // 设置初始状态和时间
        feedback.setStatus("PENDING");
        feedback.setCreateTime(LocalDateTime.now());
        feedback.setUpdateTime(LocalDateTime.now());
        
        // 保存反馈
        Feedback saved = feedbackRepository.save(feedback);
        
        // 转换为DTO
        return convertToDTO(saved);
    }

    @Override
    public Page<FeedbackDTO> getStudentFeedbacks(Long studentId, String type, Pageable pageable) {
        Page<Feedback> feedbacks;
        if (type != null && !type.isEmpty()) {
            feedbacks = feedbackRepository.findByStudentIdAndType(studentId, type, pageable);
        } else {
            feedbacks = feedbackRepository.findByStudentId(studentId, pageable);
        }
        
        return feedbacks.map(this::convertToDTO);
    }

    @Override
    public FeedbackDTO getFeedbackById(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("反馈不存在"));
        return convertToDTO(feedback);
    }

    @Override
    @Transactional
    public void deleteFeedback(Long id, Long studentId) {
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("反馈不存在"));
            
        if (!feedback.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("无权删除此反馈");
        }
        
        if (!"PENDING".equals(feedback.getStatus())) {
            throw new RuntimeException("只能删除待处理的反馈");
        }
        
        feedbackRepository.delete(feedback);
    }

    private FeedbackDTO convertToDTO(Feedback feedback) {
        FeedbackDTO dto = new FeedbackDTO();
        BeanUtils.copyProperties(feedback, dto);
        
        if (feedback.getStudent() != null) {
            dto.setStudentName(feedback.getStudent().getRealName());
            dto.setStudentNumber(feedback.getStudent().getStudentNumber());
        }
        
        if (feedback.getReplyBy() != null) {
            dto.setReplyByName(feedback.getReplyBy().getRealName());
        }
        
        return dto;
    }
} 