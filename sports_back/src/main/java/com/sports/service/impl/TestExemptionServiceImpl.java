package com.sports.service.impl;

import com.sports.entity.ExemptionApplication;
import com.sports.entity.User;
import com.sports.repository.ExemptionApplicationRepository;
import com.sports.repository.UserRepository;
import com.sports.service.TestExemptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TestExemptionServiceImpl implements TestExemptionService {

    private static final Logger log = LoggerFactory.getLogger(TestExemptionServiceImpl.class);

    @Autowired
    private ExemptionApplicationRepository exemptionApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<ExemptionApplication> getApplications(String type, String keyword, Pageable pageable) {
        log.info("Getting applications with type: {}, keyword: {}, page: {}, size: {}", 
                 type, keyword, pageable.getPageNumber(), pageable.getPageSize());
                 
        Page<ExemptionApplication> result = exemptionApplicationRepository.findAllWithFilters(
            type, keyword, pageable);
            
        log.info("Found {} applications", result.getTotalElements());
        
        return result;
    }

    @Override
    @Transactional
    public ExemptionApplication submitApplication(ExemptionApplication application) {
        // 根据学号查找学生
        if (application.getStudentNumber() != null) {
            Optional<User> studentOpt = userRepository.findByStudentNumber(application.getStudentNumber());
            if (studentOpt.isPresent()) {
                application.setStudent(studentOpt.get());
            }
        }
        
        application.setStatus("PENDING_TEACHER");
        application.setCreatedAt(LocalDateTime.now());
        return exemptionApplicationRepository.save(application);
    }

    @Override
    @Transactional
    public ExemptionApplication teacherReview(Long id, String status, String comment) {
        ExemptionApplication application = exemptionApplicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("申请不存在"));
            
        application.setStatus(status);
        application.setTeacherReviewComment(comment);
        application.setTeacherReviewTime(LocalDateTime.now());
        
        return exemptionApplicationRepository.save(application);
    }

    @Override
    @Transactional
    public ExemptionApplication adminReview(Long id, String status, String comment) {
        ExemptionApplication application = exemptionApplicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("申请不存在"));
            
        application.setStatus(status);
        application.setAdminReviewComment(comment);
        application.setAdminReviewTime(LocalDateTime.now());
        
        return exemptionApplicationRepository.save(application);
    }
} 