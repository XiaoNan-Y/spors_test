package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestExemption;
import com.sports.service.TestExemptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exemptions")
public class TestExemptionController {

    @Autowired
    private TestExemptionService testExemptionService;

    @GetMapping
    public Result getList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String studentNumber,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Page<TestExemption> page = testExemptionService.getExemptionList(
                type, status, studentNumber, PageRequest.of(pageNum - 1, pageSize)
            );
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("获取列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/student")
    public Result submitApplication(@RequestBody TestExemption exemption) {
        try {
            TestExemption saved = testExemptionService.submitApplication(exemption);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("提交申请失败：" + e.getMessage());
        }
    }

    @PutMapping("/teacher/review")
    public Result teacherReview(@RequestBody TestExemption exemption) {
        try {
            TestExemption reviewed = testExemptionService.teacherReview(
                exemption.getId(),
                exemption.getStatus(),
                exemption.getTeacherReviewComment()
            );
            return Result.success(reviewed);
        } catch (Exception e) {
            return Result.error("教师审核失败：" + e.getMessage());
        }
    }

    @PutMapping("/admin/review")
    public Result adminReview(@RequestBody TestExemption exemption) {
        try {
            TestExemption reviewed = testExemptionService.adminReview(
                exemption.getId(),
                exemption.getStatus(),
                exemption.getAdminReviewComment()
            );
            return Result.success(reviewed);
        } catch (Exception e) {
            return Result.error("管理员审核失败：" + e.getMessage());
        }
    }
} 