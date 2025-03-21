package com.sports.service;

import com.sports.dto.ScoreAppealDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScoreAppealService {
    // 教师获取申诉列表
    Page<ScoreAppealDTO> getTeacherAppeals(Long teacherId, String status, Pageable pageable);
    
    // 教师审核申诉
    ScoreAppealDTO reviewAppeal(Long id, ScoreAppealDTO appealDTO, Long teacherId);
    
    // 学生提交申诉
    ScoreAppealDTO submitAppeal(ScoreAppealDTO appealDTO, Long studentId);
    
    // 学生获取自己的申诉列表
    Page<ScoreAppealDTO> getStudentAppeals(Long studentId, String status, Pageable pageable);
} 