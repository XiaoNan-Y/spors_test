package com.sports.service.impl;

import com.sports.dto.ExemptionApplicationDTO;
import com.sports.entity.ExemptionApplication;
import com.sports.entity.User;
import com.sports.repository.ExemptionApplicationRepository;
import com.sports.repository.UserRepository;
import com.sports.service.ExemptionService;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class ExemptionServiceImpl implements ExemptionService {

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
    public ExemptionApplication submitApplication(ExemptionApplication application) {
        application.setStatus("PENDING");
        application.setApplyTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        return exemptionRepository.save(application);
    }

    @Override
    public Page<ExemptionApplication> getStudentApplications(Long studentId, Pageable pageable) {
        return exemptionRepository.findByStudentId(studentId, pageable);
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
    public ExemptionApplication adminReview(Long id, String status, String comment, Long reviewerId) {
        ExemptionApplication application = exemptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("申请不存在"));

        if (!"EXEMPTION".equals(application.getType())) {
            throw new RuntimeException("只能审核免测申请");
        }

        application.setStatus(status);
        application.setAdminReviewComment(comment);
        application.setAdminReviewTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        
        return exemptionRepository.save(application);
    }

    @Override
    @Transactional
    public ExemptionApplication teacherReview(Long id, String status, String comment, Long reviewerId) {
        ExemptionApplication application = exemptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("申请不存在"));

        if (!"RETEST".equals(application.getType())) {
            throw new RuntimeException("只能审核重测申请");
        }

        application.setStatus(status);
        application.setTeacherReviewComment(comment);
        application.setTeacherReviewTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        
        return exemptionRepository.save(application);
    }

    @Override
    public ExemptionApplication getApplicationById(Long id) {
        return exemptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("申请不存在"));
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
        try {
            // 构建查询条件
            List<ExemptionApplication> applications;
            if (className != null && !className.isEmpty()) {
                // 如果指定了班级，则按班级筛选
                applications = exemptionRepository.findAllWithFiltersNoPage(
                    className, type, "PENDING_ADMIN", keyword);
            } else {
                // 否则查询所有待管理员审核的申请
                applications = exemptionRepository.findAllWithFiltersNoPage(
                    null, type, "PENDING_ADMIN", keyword);
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
            log.error("获取管理员待审核申请失败", e);
            throw new RuntimeException("获取管理员待审核申请失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getDistinctClassNames() {
        try {
            return userRepository.findDistinctClassName();
        } catch (Exception e) {
            log.error("获取班级列表失败", e);
            throw new RuntimeException("获取班级列表失败: " + e.getMessage());
        }
    }

    @Override
    public Page<ExemptionApplication> getAdminExemptionApplications(
            String keyword, String status, Pageable pageable) {
        try {
            if (status != null && !status.isEmpty()) {
                // 如果指定了状态，则按状态筛选
                return exemptionRepository.findByTypeAndStatusAndKeyword(
                    "EXEMPTION", status, keyword, pageable);
            } else {
                // 否则返回所有状态的申请
                return exemptionRepository.findByTypeAndKeyword(
                    "EXEMPTION", keyword, pageable);
            }
        } catch (Exception e) {
            log.error("获取管理员免测申请列表失败", e);
            throw new RuntimeException("获取管理员免测申请列表失败: " + e.getMessage());
        }
    }

    @Override
    public Page<ExemptionApplication> getTeacherRetestApplications(String keyword, Pageable pageable) {
        try {
            return exemptionRepository.findPendingRetestApplications(keyword, pageable);
        } catch (Exception e) {
            log.error("获取教师待审核重测申请列表失败", e);
            throw new RuntimeException("获取教师待审核重测申请列表失败: " + e.getMessage());
        }
    }

    @Override
    public Page<ExemptionApplication> getTeacherExemptionApplications(String keyword, Pageable pageable) {
        try {
            return exemptionRepository.findPendingExemptionApplications(keyword, pageable);
        } catch (Exception e) {
            log.error("获取教师免测申请列表失败", e);
            throw new RuntimeException("获取教师免测申请列表失败: " + e.getMessage());
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