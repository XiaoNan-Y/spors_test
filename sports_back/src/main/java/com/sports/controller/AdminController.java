package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.ExemptionApplication;
import com.sports.entity.User;
import com.sports.entity.TestRecord;
import com.sports.service.ExemptionService;
import com.sports.service.ExcelService;
import com.sports.service.TestRecordService;
import com.sports.repository.TestRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {
    
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private ExemptionService exemptionService;
    
    @Autowired
    private ExcelService excelService;
    
    @Autowired
    private TestRecordRepository testRecordRepository;
    
    @Autowired
    private TestRecordService testRecordService;
    
    @GetMapping("/exemptions")
    public Result getExemptions(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Getting exemption applications - keyword: {}, page: {}, size: {}", 
                    keyword, page, size);
            Page<ExemptionApplication> exemptions = exemptionService.getExemptionApplications(
                keyword, PageRequest.of(page, size));
            
            // 构造返回数据，只返回需要的字段
            List<Map<String, Object>> content = exemptions.getContent().stream()
                .map(app -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", app.getId());
                    item.put("studentId", app.getStudentId());
                    item.put("studentNumber", app.getStudentNumber());
                    item.put("studentName", app.getStudentName());
                    item.put("className", app.getClassName());
                    item.put("type", app.getType());
                    item.put("reason", app.getReason());
                    item.put("status", app.getStatus());
                    item.put("teacherReviewComment", app.getTeacherReviewComment());
                    item.put("adminReviewComment", app.getAdminReviewComment());
                    item.put("createdAt", app.getCreatedAt());
                    item.put("applyTime", app.getApplyTime());
                    
                    // 添加体育项目信息
                    if (app.getSportsItem() != null) {
                        item.put("sportsItemId", app.getSportsItem().getId());
                        item.put("sportsItemName", app.getSportsItem().getName());
                    }
                    
                    return item;
                })
                .collect(Collectors.toList());
            
            // 构造分页信息
            Map<String, Object> result = new HashMap<>();
            result.put("content", content);
            result.put("totalElements", exemptions.getTotalElements());
            result.put("totalPages", exemptions.getTotalPages());
            result.put("size", exemptions.getSize());
            result.put("number", exemptions.getNumber());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to get exemption applications", e);
            return Result.error("获取免测申请列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/users/student/import")
    public Result importStudents(@RequestParam("file") MultipartFile file) {
        try {
            List<User> users = excelService.importStudents(file);
            return Result.success(users);
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    @PostMapping("/users/teacher/import")
    public Result importTeachers(@RequestParam("file") MultipartFile file) {
        try {
            List<User> users = excelService.importTeachers(file);
            return Result.success(users);
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/class-names")
    public Result getClassNames() {
        try {
            // 从测试记录中获取所有不同的班级名称
            List<String> classNames = testRecordRepository.findDistinctClassNames();
            
            // 如果没有记录，返回一个空列表
            if (classNames == null || classNames.isEmpty()) {
                // 可以添加一些默认班级
                classNames = Arrays.asList("计科1班", "计科2班", "软工1班", "软工2班");
            }
            
            return Result.success(classNames);
        } catch (Exception e) {
            log.error("获取班级列表失败", e);
            return Result.error("获取班级列表失败：" + e.getMessage());
        }
    }

    // 审核成绩记录
    @PutMapping("/records/{id}/review")
    public Result reviewRecord(
        @PathVariable Long id,
        @RequestParam String status,
        @RequestParam(required = false) String comment,
        @RequestAttribute Long userId
    ) {
        try {
            // 将参数 status 传递给 reviewStatus
            TestRecord updated = testRecordService.reviewRecord(id, status, comment, userId);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }
} 