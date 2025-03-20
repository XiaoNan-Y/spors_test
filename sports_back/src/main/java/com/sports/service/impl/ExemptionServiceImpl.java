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
import java.util.ArrayList;

@Service
public class ExemptionServiceImpl implements ExemptionService {

    private static final Logger log = LoggerFactory.getLogger(ExemptionServiceImpl.class);

    @Autowired
    private ExemptionApplicationRepository exemptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ExemptionApplication> getExemptionApplications(String keyword, Pageable pageable) {
        try {
            Page<ExemptionApplication> applications = exemptionRepository.findByStudentNameContainingOrStudentNumberContaining(
                keyword != null ? keyword.trim() : "", 
                keyword != null ? keyword.trim() : "", 
                pageable
            );
            
            // 手动初始化懒加载的关联对象
            applications.getContent().forEach(app -> {
                if (app.getSportsItem() != null) {
                    app.getSportsItem().getId();  // 触发懒加载
                    app.getSportsItem().getName();
                }
                if (app.getStudent() != null) {
                    app.getStudent().getId();  // 触发懒加载
                    app.getStudent().getRealName();
                }
            });
            
            return applications;
        } catch (Exception e) {
            log.error("获取免测申请列表失败", e);
            throw new RuntimeException("获取免测申请列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ExemptionApplicationDTO submitApplication(ExemptionApplication application) {
        application.setStatus("PENDING_TEACHER");
        ExemptionApplication saved = exemptionRepository.save(application);
        return convertToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExemptionApplicationDTO> getStudentApplications(Long studentId, String type, Pageable pageable) {
        Page<ExemptionApplication> applications = exemptionRepository.findByStudentId(studentId, pageable);
        List<ExemptionApplicationDTO> dtos = applications.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, applications.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExemptionApplicationDTO> getAllApplications(
            String className, String type, String status, String keyword, Pageable pageable) {
        List<ExemptionApplication> applications = exemptionRepository.findAllWithFilters(type, keyword, pageable);
        long total = exemptionRepository.countWithFilters(type, keyword);
        
        List<ExemptionApplicationDTO> dtos = applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        return new PageImpl<>(dtos, pageable, total);
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
        try {
            // 构建查询条件
            List<ExemptionApplication> applications;
            if (className != null && !className.isEmpty()) {
                // 如果指定了班级，则按班级筛选
                applications = exemptionRepository.findAllWithFiltersNoPage(
                    className, type, "PENDING_TEACHER", keyword);
            } else {
                // 否则查询所有待教师审核的申请
                applications = exemptionRepository.findAllWithFiltersNoPage(
                    null, type, "PENDING_TEACHER", keyword);
            }
            
            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), applications.size());
            
            List<ExemptionApplication> pageContent = start < end ? 
                applications.subList(start, end) : 
                new ArrayList<>();
                
            // 转换为DTO
            List<ExemptionApplicationDTO> dtos = pageContent.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
            return new PageImpl<>(dtos, pageable, applications.size());
        } catch (Exception e) {
            log.error("获取教师待审核申请失败", e);
            throw new RuntimeException("获取教师待审核申请失败: " + e.getMessage());
        }
    }

    @Override
    public Page<ExemptionApplicationDTO> getAdminPendingApplications(
            String className, String type, String keyword, Pageable pageable) {
        return getAllApplications(className, type, "PENDING_ADMIN", keyword, pageable);
    }

    @Override
    public List<String> getDistinctClassNames() {
        return exemptionRepository.findDistinctClassNames();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExemptionApplication> getTeacherExemptionApplications(String keyword, Pageable pageable) {
        try {
            Page<ExemptionApplication> applications = exemptionRepository.findTeacherPendingApplications(
                keyword != null ? keyword.trim() : "", 
                keyword != null ? keyword.trim() : "", 
                pageable
            );
            
            // 手动初始化懒加载的关联对象
            applications.getContent().forEach(app -> {
                if (app.getSportsItem() != null) {
                    app.getSportsItem().getId();  // 触发懒加载
                    app.getSportsItem().getName();
                }
                if (app.getStudent() != null) {
                    app.getStudent().getId();  // 触发懒加载
                    app.getStudent().getRealName();
                }
            });
            
            return applications;
        } catch (Exception e) {
            log.error("获取教师待审核申请列表失败", e);
            throw new RuntimeException("获取教师待审核申请列表失败: " + e.getMessage());
        }
    }

    private ExemptionApplicationDTO convertToDTO(ExemptionApplication application) {
        if (application == null) return null;
        
        ExemptionApplicationDTO dto = new ExemptionApplicationDTO();
        BeanUtils.copyProperties(application, dto, "sportsItem");
        
        if (application.getSportsItem() != null) {
            dto.setSportsItemId(application.getSportsItem().getId());
            dto.setSportsItemName(application.getSportsItem().getName());
        }
        
        return dto;
    }
} 