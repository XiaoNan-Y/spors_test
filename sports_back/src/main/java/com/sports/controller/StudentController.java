package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestRecord;
import com.sports.entity.ExemptionApplication;
import com.sports.service.StudentService;
import com.sports.dto.TestRecordDTO;
import com.sports.dto.ScoreAppealDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
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
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute(required = false) Long userId) {
        try {
            log.info("Getting test records - userId: {}, status: {}, page: {}, size: {}", 
                    userId, status, page, size);
                    
            if (userId == null) {
                return Result.error("未提供有效的用户ID");
            }
            
            if (page < 1) {
                return Result.error("页码必须大于0");
            }
            if (size <= 0) {
                return Result.error("每页大小必须大于0");
            }
            
            Page<TestRecordDTO> records = studentService.getStudentTestRecords(
                userId, status, PageRequest.of(page - 1, size));
            
            log.info("Found {} records", records.getTotalElements());
            
            if (records.getContent().isEmpty() && page > 1) {
                return Result.error("没有更多数据了");
            }
            
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

    @GetMapping("/test-records/appealable")
    public Result getAppealableRecords(@RequestAttribute Long userId) {
        try {
            log.info("获取可申诉记录请求 - userId: {}", userId);
            List<TestRecordDTO> records = studentService.getAppealableRecords(userId);
            log.info("找到 {} 条可申诉记录", records.size());
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取可申诉记录失败 - userId: {}", userId, e);
            return Result.error("获取可申诉记录失败：" + e.getMessage());
        }
    }

    @PostMapping("/appeals")
    public Result submitAppeal(@RequestBody ScoreAppealDTO appealDTO, @RequestAttribute Long userId) {
        try {
            ScoreAppealDTO saved = studentService.submitAppeal(appealDTO, userId);
            return Result.success(saved);
        } catch (Exception e) {
            log.error("提交申诉失败", e);
            return Result.error("提交申诉失败：" + e.getMessage());
        }
    }

    @GetMapping("/appeals")
    public Result getAppeals(
            @RequestAttribute Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("获取学生申诉列表 - userId: {}, status: {}, page: {}, size: {}", 
                    userId, status, page, size);
            Page<ScoreAppealDTO> appeals = studentService.getStudentAppeals(
                userId, status, PageRequest.of(page, size));
            return Result.success(appeals.getContent());
        } catch (Exception e) {
            log.error("获取申诉列表失败", e);
            return Result.error("获取申诉列表失败：" + e.getMessage());
        }
    }
} 