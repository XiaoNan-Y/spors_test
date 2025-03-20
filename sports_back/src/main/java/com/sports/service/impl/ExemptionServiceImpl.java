package com.sports.service.impl;

import com.sports.dto.ExemptionApplicationDTO;
import com.sports.entity.ExemptionApplication;
import com.sports.entity.User;
import com.sports.repository.ExemptionApplicationRepository;
import com.sports.repository.UserRepository;
import com.sports.service.ExemptionService;
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
import java.util.stream.Collectors;

@Service
public class ExemptionServiceImpl implements ExemptionService {

    private static final Logger log = LoggerFactory.getLogger(ExemptionServiceImpl.class);

    @Autowired
    private ExemptionApplicationRepository exemptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public ExemptionApplicationDTO submitApplication(ExemptionApplication application) {
        // 设置初始状态和时间
        application.setStatus("PENDING_TEACHER");
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        
        ExemptionApplication saved = exemptionRepository.save(application);
        return convertToDTO(saved);
    }

    @Override
    public Page<ExemptionApplicationDTO> getStudentApplications(Long studentId, String type, Pageable pageable) {
        // 获取分页数据
        Page<ExemptionApplication> applications = exemptionRepository.findByStudentId(studentId, pageable);
        
        // 转换为DTO
        List<ExemptionApplicationDTO> dtoList = applications.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 创建新的分页对象
        return new PageImpl<>(dtoList, pageable, applications.getTotalElements());
    }

    @Override
    public Page<ExemptionApplicationDTO> getAllApplications(
            String className, String type, String status, String keyword, Pageable pageable) {
        try {
            // 获取总数
            long total = exemptionRepository.countWithFilters(type, keyword);
            
            // 获取当前页数据
            List<ExemptionApplication> applications = exemptionRepository.findAllWithFilters(
                type, keyword, pageable);
            
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

    @Override
    @Transactional
    public ExemptionApplicationDTO teacherReview(Long id, String status, String reviewComment, Long reviewerId) {
        ExemptionApplication application = exemptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        // 检查状态是否允许教师审核
        if (!application.getStatus().equals("PENDING_TEACHER")) {
            throw new RuntimeException("当前状态不允许教师审核");
        }
        
        // 更新审核信息
        application.setStatus(status);
        application.setTeacherReviewComment(reviewComment);
        application.setTeacherReviewTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        
        // 如果是通过，则状态改为待管理员审核
        if (status.equals("APPROVED")) {
            application.setStatus("PENDING_ADMIN");
        }
        
        ExemptionApplication updated = exemptionRepository.save(application);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public ExemptionApplicationDTO adminReview(Long id, String status, String reviewComment, Long reviewerId) {
        ExemptionApplication application = exemptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        application.setStatus(status);
        application.setAdminReviewComment(reviewComment);
        application.setAdminReviewTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        
        ExemptionApplication updated = exemptionRepository.save(application);
        return convertToDTO(updated);
    }

    @Override
    public ExemptionApplicationDTO getApplicationById(Long id) {
        ExemptionApplication application = exemptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        return convertToDTO(application);
    }

    @Override
    @Transactional
    public void deleteApplication(Long id, Long studentId) {
        ExemptionApplication application = exemptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        if (!application.getStudentId().equals(studentId)) {
            throw new RuntimeException("无权删除此申请");
        }
        
        if (!application.getStatus().startsWith("PENDING")) {
            throw new RuntimeException("只能删除待审核的申请");
        }
        
        exemptionRepository.delete(application);
    }

    @Override
    @Transactional
    public ExemptionApplicationDTO updateApplication(ExemptionApplication application) {
        // 更新时间
        application.setUpdateTime(LocalDateTime.now());
        
        ExemptionApplication updated = exemptionRepository.save(application);
        return convertToDTO(updated);
    }

    @Override
    public Page<ExemptionApplicationDTO> getTeacherPendingApplications(
            String className, String type, String keyword, Pageable pageable) {
        return getAllApplications(className, type, "PENDING_TEACHER", keyword, pageable);
    }

    @Override
    public Page<ExemptionApplicationDTO> getAdminPendingApplications(
            String className, String type, String keyword, Pageable pageable) {
        return getAllApplications(className, type, "PENDING_ADMIN", keyword, pageable);
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