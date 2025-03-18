package com.sports.controller;

import com.sports.entity.ExemptionApplication;
import com.sports.common.Result;
import com.sports.service.TestExemptionService;
import com.sports.repository.ExemptionApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exemptions")
@CrossOrigin
public class TestExemptionController {

    private static final Logger log = LoggerFactory.getLogger(TestExemptionController.class);

    @Autowired
    private TestExemptionService testExemptionService;

    @Autowired
    private ExemptionApplicationRepository exemptionApplicationRepository;

    @GetMapping
    public Result<Page<ExemptionApplication>> getApplications(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        log.info("Received request - type: {}, keyword: {}, page: {}, size: {}", 
                 type, keyword, page, size);
        
        try {
            Page<ExemptionApplication> applications = testExemptionService.getApplications(
                type, keyword, PageRequest.of(page, size));
                
            log.info("Found {} applications, total {} pages", 
                    applications.getContent().size(), 
                    applications.getTotalPages());
                    
            // 添加详细的数据日志
            applications.getContent().forEach(app -> 
                log.debug("Application: id={}, studentName={}, type={}, status={}", 
                         app.getId(), app.getStudentName(), app.getType(), app.getStatus())
            );
            
            return Result.success(applications);
        } catch (Exception e) {
            log.error("Error getting applications", e);
            return Result.error("获取申请列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/classes")
    public Result getClassList() {
        try {
            List<String> classNames = exemptionApplicationRepository.findDistinctClassNames();
            return Result.success(classNames);
        } catch (Exception e) {
            return Result.error("获取班级列表失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/review")
    public Result review(
            @PathVariable Long id,
            @RequestBody ExemptionApplication exemption) {
        try {
            ExemptionApplication existing = exemptionApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("申请不存在"));

            existing.setStatus(exemption.getStatus());
            existing.setTeacherReviewComment(exemption.getTeacherReviewComment());
            existing.setTeacherReviewTime(LocalDateTime.now());
            existing.setUpdateTime(LocalDateTime.now());

            ExemptionApplication saved = exemptionApplicationRepository.save(existing);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }
} 