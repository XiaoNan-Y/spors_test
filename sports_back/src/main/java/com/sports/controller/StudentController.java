package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestRecord;
import com.sports.entity.ExemptionApplication;
import com.sports.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    
    private static final Logger log = LoggerFactory.getLogger(StudentController.class);
    
    @Autowired
    private StudentService studentService;

    @GetMapping("/dashboard/stats")
    public Result getDashboardStats(@RequestAttribute Long userId) {
        try {
            Map<String, Object> stats = studentService.getDashboardStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取学生统计数据失败", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }

    @GetMapping("/test-records")
    public Result getTestRecords(
            @RequestAttribute Long userId,
            @RequestParam(required = false) Long sportsItemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<TestRecord> records = studentService.getTestRecords(
                userId, sportsItemId, PageRequest.of(page, size));
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取测试记录失败", e);
            return Result.error("获取测试记录失败：" + e.getMessage());
        }
    }

    @PostMapping("/exemption")
    public Result submitExemption(@RequestBody ExemptionApplication application) {
        try {
            ExemptionApplication saved = studentService.submitExemption(application);
            return Result.success(saved);
        } catch (Exception e) {
            log.error("提交免测申请失败", e);
            return Result.error("提交申请失败：" + e.getMessage());
        }
    }

    @GetMapping("/exemption")
    public Result getExemptions(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ExemptionApplication> applications = studentService.getExemptions(
                userId, PageRequest.of(page, size));
            return Result.success(applications);
        } catch (Exception e) {
            log.error("获取免测申请列表失败", e);
            return Result.error("获取申请列表失败：" + e.getMessage());
        }
    }
} 