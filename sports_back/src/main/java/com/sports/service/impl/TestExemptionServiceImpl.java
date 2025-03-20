package com.sports.service.impl;

import com.sports.entity.ExemptionApplication;
import com.sports.entity.User;
import com.sports.repository.ExemptionApplicationRepository;
import com.sports.repository.UserRepository;
import com.sports.service.TestExemptionService;
import com.sports.dto.ExemptionApplicationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                 
        Page<ExemptionApplication> result = new PageImpl<>(
            exemptionApplicationRepository.findAllWithFilters(type, keyword, pageable),
            pageable,
            exemptionApplicationRepository.countWithFilters(type, keyword)
        );
            
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

    @Override
    public Page<ExemptionApplicationDTO> getAllApplications(
            String className, String type, String status, String keyword, Pageable pageable) {
        try {
            // 获取总数
            long total = exemptionApplicationRepository.countWithFilters(type, keyword);
            
            // 获取当前页数据
            List<ExemptionApplication> applications = exemptionApplicationRepository.findAllWithFilters(
                type,  // 类型
                keyword,  // 关键字
                pageable  // 分页信息
            );
            
            // 转换为DTO
            List<ExemptionApplicationDTO> dtoList = applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return new PageImpl<>(dtoList, pageable, total);
        } catch (Exception e) {
            log.error("获取申请列表失败", e);
            throw new RuntimeException("获取申请列表失败", e);
        }
    }

    private ExemptionApplicationDTO convertToDTO(ExemptionApplication application) {
        ExemptionApplicationDTO dto = new ExemptionApplicationDTO();
        BeanUtils.copyProperties(application, dto);
        
        // 设置体育项目信息
        if (application.getSportsItem() != null) {
            dto.setSportsItemId(application.getSportsItem().getId());
            dto.setSportsItemName(application.getSportsItem().getName());
        }
        
        return dto;
    }
} 