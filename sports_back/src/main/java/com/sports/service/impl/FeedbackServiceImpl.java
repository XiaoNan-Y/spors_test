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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackServiceImpl.class);

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

    @Override
    public Page<FeedbackDTO> getAdminFeedbacks(String type, String status, Pageable pageable) {
        try {
            Page<Feedback> feedbacks;
            
            // 根据条件查询
            if (type != null && !type.isEmpty() && status != null && !status.isEmpty()) {
                feedbacks = feedbackRepository.findByTypeAndStatus(type, status, pageable);
            } else if (type != null && !type.isEmpty()) {
                feedbacks = feedbackRepository.findByType(type, pageable);
            } else if (status != null && !status.isEmpty()) {
                feedbacks = feedbackRepository.findByStatus(status, pageable);
            } else {
                feedbacks = feedbackRepository.findAll(pageable);
            }
            
            // 转换为DTO
            return feedbacks.map(feedback -> {
                FeedbackDTO dto = new FeedbackDTO();
                BeanUtils.copyProperties(feedback, dto);
                
                // 设置学生信息
                if (feedback.getStudent() != null) {
                    dto.setStudentName(feedback.getStudent().getRealName());
                    dto.setStudentNumber(feedback.getStudent().getStudentNumber());
                    dto.setClassName(feedback.getStudent().getClassName());
                }
                
                // 设置回复人信息
                if (feedback.getReplyBy() != null) {
                    dto.setReplyByName(feedback.getReplyBy().getRealName());
                }
                
                return dto;
            });
        } catch (Exception e) {
            log.error("获取管理员反馈列表失败", e);
            throw new RuntimeException("获取反馈列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public FeedbackDTO replyFeedback(Long id, String reply, Long adminId) {
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("反馈不存在"));
            
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new RuntimeException("管理员不存在"));
            
        feedback.setReply(reply);
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setReplyBy(admin);
        feedback.setStatus("REPLIED");
        
        Feedback updated = feedbackRepository.save(feedback);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public FeedbackDTO updateFeedbackStatus(Long id, String status, Long adminId) {
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("反馈不存在"));
            
        feedback.setStatus(status);
        feedback.setUpdateTime(LocalDateTime.now());
        
        Feedback updated = feedbackRepository.save(feedback);
        return convertToDTO(updated);
    }

    private FeedbackDTO convertToDTO(Feedback feedback) {
        FeedbackDTO dto = new FeedbackDTO();
        BeanUtils.copyProperties(feedback, dto);
        
        if (feedback.getStudent() != null) {
            dto.setStudentName(feedback.getStudent().getRealName());
            dto.setStudentNumber(feedback.getStudent().getStudentNumber());
            dto.setClassName(feedback.getStudent().getClassName());
        }
        
        if (feedback.getReplyBy() != null) {
            dto.setReplyByName(feedback.getReplyBy().getRealName());
        }
        
        return dto;
    }
} 