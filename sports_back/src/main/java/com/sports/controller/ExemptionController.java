package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.ExemptionApplication;
import com.sports.service.ExemptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/exemptions")
@CrossOrigin
public class ExemptionController {

    @Autowired
    private ExemptionService exemptionService;

    // 学生提交申请
    @PostMapping("/student/submit")
    public Result submitApplication(@RequestBody ExemptionApplication application) {
        try {
            ExemptionApplication saved = exemptionService.submitApplication(application);
            return Result.success(saved);
        } catch (Exception e) {
            log.error("提交申请失败", e);
            return Result.error("提交申请失败：" + e.getMessage());
        }
    }

    // 学生获取自己的申请列表
    @GetMapping("/student/list")
    public Result getStudentApplications(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ExemptionApplication> applications = exemptionService
                .getStudentApplications(studentId, PageRequest.of(page, size));
            return Result.success(applications);
        } catch (Exception e) {
            log.error("获取申请列表失败", e);
            return Result.error("获取申请列表失败：" + e.getMessage());
        }
    }

    // 管理员获取免测申请列表
    @GetMapping("/admin/list")
    public Result getAdminExemptions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ExemptionApplication> applications = exemptionService
                .getAdminExemptionApplications(keyword, status, PageRequest.of(page, size));
            return Result.success(applications);
        } catch (Exception e) {
            log.error("获取免测申请列表失败", e);
            return Result.error("获取免测申请列表失败：" + e.getMessage());
        }
    }

    // 管理员审核免测申请
    @PostMapping("/admin/review/{id}")
    public Result adminReview(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        try {
            String status = (String) params.get("status");
            String comment = (String) params.get("comment");
            Long reviewerId = Long.valueOf(params.get("reviewerId").toString());
            
            ExemptionApplication reviewed = exemptionService.adminReview(id, status, comment, reviewerId);
            return Result.success(reviewed);
        } catch (Exception e) {
            log.error("审核失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    // 教师获取重测申请列表
    @GetMapping("/teacher/list")
    public Result getTeacherExemptions(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ExemptionApplication> applications = exemptionService
                .getTeacherRetestApplications(keyword, PageRequest.of(page, size));
            return Result.success(applications);
        } catch (Exception e) {
            log.error("获取重测申请列表失败", e);
            return Result.error("获取重测申请列表失败：" + e.getMessage());
        }
    }

    // 教师审核重测申请
    @PostMapping("/teacher/review/{id}")
    public Result teacherReview(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam String comment,
            @RequestParam Long reviewerId) {
        try {
            ExemptionApplication reviewed = exemptionService.teacherReview(id, status, comment, reviewerId);
            return Result.success(reviewed);
        } catch (Exception e) {
            log.error("审核失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    // 获取申请详情
    @GetMapping("/{id}")
    public Result getApplicationDetail(@PathVariable Long id) {
        try {
            ExemptionApplication application = exemptionService.getApplicationById(id);
            return Result.success(application);
        } catch (Exception e) {
            log.error("获取申请详情失败", e);
            return Result.error("获取申请详情失败：" + e.getMessage());
        }
    }
} 