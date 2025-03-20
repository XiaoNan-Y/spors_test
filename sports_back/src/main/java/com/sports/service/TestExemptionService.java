package com.sports.service;

import com.sports.entity.ExemptionApplication;
import com.sports.dto.ExemptionApplicationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TestExemptionService {
    Page<ExemptionApplication> getApplications(String type, String keyword, Pageable pageable);
    ExemptionApplication submitApplication(ExemptionApplication application);
    ExemptionApplication teacherReview(Long id, String status, String comment);
    ExemptionApplication adminReview(Long id, String status, String comment);
    Page<ExemptionApplicationDTO> getAllApplications(String className, String type, String status, String keyword, Pageable pageable);
} 