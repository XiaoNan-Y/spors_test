package com.sports.service;

import com.sports.entity.ExemptionApplication;
import com.sports.dto.ExemptionApplicationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ExemptionService {
    /**
     * 提交免测/重测申请
     */
    ExemptionApplicationDTO submitApplication(ExemptionApplication application);
    
    /**
     * 获取学生的申请列表
     */
    Page<ExemptionApplication> getStudentApplications(Long studentId, Pageable pageable);
    
    /**
     * 获取所有申请列表(用于教师/管理员查看)
     */
    Page<ExemptionApplicationDTO> getAllApplications(
        String className, 
        String type, 
        String status, 
        String keyword, 
        Pageable pageable
    );
    
    /**
     * 教师审核申请
     */
    ExemptionApplicationDTO teacherReview(
        Long id, 
        String status, 
        String reviewComment, 
        Long reviewerId
    );

    /**
     * 管理员审核申请
     */
    ExemptionApplicationDTO adminReview(
        Long id,
        String status,
        String reviewComment,
        Long reviewerId
    );
    
    /**
     * 获取申请详情
     */
    ExemptionApplicationDTO getApplicationById(Long id);
    
    /**
     * 删除申请(仅允许删除待审核的申请)
     */
    void deleteApplication(Long id, Long studentId);

    /**
     * 更新申请
     */
    ExemptionApplicationDTO updateApplication(ExemptionApplication application);

    /**
     * 获取教师待审核的申请列表
     */
    Page<ExemptionApplicationDTO> getTeacherPendingApplications(
        String className,
        String type,
        String keyword,
        Pageable pageable
    );

    /**
     * 获取管理员待审核的申请列表
     */
    Page<ExemptionApplicationDTO> getAdminPendingApplications(
        String className,
        String type,
        String keyword,
        Pageable pageable
    );

    Page<ExemptionApplication> getExemptionApplications(String keyword, Pageable pageable);

    List<String> getDistinctClassNames();

    Page<ExemptionApplication> getTeacherExemptionApplications(String keyword, Pageable pageable);
} 