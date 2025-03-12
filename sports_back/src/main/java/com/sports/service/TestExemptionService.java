package com.sports.service;

import com.sports.entity.TestExemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TestExemptionService {
    Page<TestExemption> getExemptionList(String type, String status, String studentNumber, Pageable pageable);
    TestExemption submitApplication(TestExemption exemption);
    TestExemption teacherReview(Long id, String status, String comment);
    TestExemption adminReview(Long id, String status, String comment);
} 