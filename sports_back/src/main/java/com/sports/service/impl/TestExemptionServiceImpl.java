package com.sports.service.impl;

import com.sports.entity.TestExemption;
import com.sports.repository.TestExemptionRepository;
import com.sports.service.TestExemptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TestExemptionServiceImpl implements TestExemptionService {

    @Autowired
    private TestExemptionRepository testExemptionRepository;

    @Override
    public Page<TestExemption> getExemptionList(String type, String status, String studentNumber, Pageable pageable) {
        Specification<TestExemption> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (type != null && !type.isEmpty()) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (studentNumber != null && !studentNumber.isEmpty()) {
                predicates.add(cb.equal(root.get("studentNumber"), studentNumber));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return testExemptionRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public TestExemption submitApplication(TestExemption exemption) {
        exemption.setStatus("PENDING_TEACHER");
        exemption.setApplyTime(LocalDateTime.now());
        exemption.setCreatedAt(LocalDateTime.now());
        exemption.setUpdatedAt(LocalDateTime.now());
        return testExemptionRepository.save(exemption);
    }

    @Override
    @Transactional
    public TestExemption teacherReview(Long id, String status, String comment) {
        TestExemption exemption = testExemptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("申请不存在"));
            
        exemption.setStatus(status);
        exemption.setTeacherReviewComment(comment);
        exemption.setTeacherReviewTime(LocalDateTime.now());
        exemption.setUpdatedAt(LocalDateTime.now());
        
        return testExemptionRepository.save(exemption);
    }

    @Override
    @Transactional
    public TestExemption adminReview(Long id, String status, String comment) {
        TestExemption exemption = testExemptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("申请不存在"));
            
        exemption.setStatus(status);
        exemption.setAdminReviewComment(comment);
        exemption.setAdminReviewTime(LocalDateTime.now());
        exemption.setUpdatedAt(LocalDateTime.now());
        
        return testExemptionRepository.save(exemption);
    }
} 