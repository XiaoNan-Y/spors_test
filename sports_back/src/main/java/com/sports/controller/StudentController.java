package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestRecord;
import com.sports.entity.ExemptionApplication;
import com.sports.entity.Notice;
import com.sports.entity.Feedback;
import com.sports.entity.User;
import com.sports.service.StudentService;
import com.sports.service.FeedbackService;
import com.sports.dto.TestRecordDTO;
import com.sports.dto.ScoreAppealDTO;
import com.sports.dto.FeedbackDTO;
import com.sports.repository.UserRepository;
import com.sports.service.ExemptionService;
import com.sports.dto.ExemptionApplicationDTO;
import com.sports.repository.SportsItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sports.entity.SportsItem;
import com.sports.repository.ExemptionApplicationRepository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentController {
    
    private static final Logger log = LoggerFactory.getLogger(StudentController.class);
    
    @Autowired
    private StudentService studentService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExemptionService exemptionService;

    @Autowired
    private SportsItemRepository sportsItemRepository;

    @Autowired
    private ExemptionApplicationRepository exemptionRepository;

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

    @GetMapping("/notices")
    public Result getNotices(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("获取通知列表 - keyword: {}, type: {}, page: {}, size: {}", 
                    keyword, type, page, size);
            Page<Notice> notices = studentService.getNotices(keyword, type, PageRequest.of(page, size));
            
            // 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("content", notices.getContent());
            result.put("totalElements", notices.getTotalElements());
            result.put("totalPages", notices.getTotalPages());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取通知列表失败", e);
            return Result.error("获取通知列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/feedback")
    public Result submitFeedback(@RequestBody Feedback feedback, @RequestAttribute Long userId) {
        try {
            User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            feedback.setStudent(student);
            
            FeedbackDTO savedFeedback = feedbackService.submitFeedback(feedback);
            return Result.success(savedFeedback);
        } catch (Exception e) {
            return Result.error("提交反馈失败：" + e.getMessage());
        }
    }

    @GetMapping("/feedback")
    public Result getFeedbacks(
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestAttribute Long userId
    ) {
        try {
            Page<FeedbackDTO> feedbacks = feedbackService.getStudentFeedbacks(
                userId, type, PageRequest.of(page, size));
            return Result.success(feedbacks);
        } catch (Exception e) {
            return Result.error("获取反馈列表失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/feedback/{id}")
    public Result deleteFeedback(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            feedbackService.deleteFeedback(id, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除反馈失败：" + e.getMessage());
        }
    }

    @PostMapping("/exemptions")
    public Result createExemption(
        @RequestBody ExemptionApplication application,
        @RequestAttribute Long userId
    ) {
        try {
            // 获取用户信息
            User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            log.info("Creating exemption application - type: {}, sportsItemId: {}, userId: {}", 
                application.getType(), application.getSportsItemId(), userId);

            // 设置必要字段 - 移到前面来确保所有必要字段都被设置
            application.setStudentId(userId);
            application.setStudentNumber(student.getStudentNumber());
            application.setStudentName(student.getRealName());
            application.setClassName(student.getClassName());
            application.setStatus("PENDING_TEACHER");
            application.setApplyTime(LocalDateTime.now());
            application.setUpdateTime(LocalDateTime.now());

            // 如果是重测申请，验证并设置体育项目
            if ("RETEST".equals(application.getType())) {
                Long sportsItemId = application.getSportsItemId();
                log.info("Received sportsItemId: {}", sportsItemId);
                
                if (sportsItemId == null) {
                    return Result.error("重测申请必须选择体育项目");
                }
                
                try {
                    SportsItem sportsItem = sportsItemRepository.findById(sportsItemId)
                        .orElseThrow(() -> new RuntimeException("体育项目不存在"));
                    application.setSportsItem(sportsItem);
                    application.setSportsItemName(sportsItem.getName());
                    log.info("Set sports item: {}", sportsItem.getName());
                } catch (Exception e) {
                    log.error("Error setting sports item", e);
                    return Result.error("设置体育项目失败：" + e.getMessage());
                }
            }

            // 保存申请
            ExemptionApplication saved = exemptionRepository.save(application);
            log.info("Saved application with ID: {}", saved.getId());

            // 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("id", saved.getId());
            result.put("type", saved.getType());
            result.put("reason", saved.getReason());
            result.put("status", saved.getStatus());
            result.put("applyTime", saved.getApplyTime());
            result.put("studentName", saved.getStudentName());
            result.put("studentNumber", saved.getStudentNumber());
            result.put("className", saved.getClassName());
            
            // 添加体育项目信息
            if (saved.getSportsItem() != null) {
                result.put("sportsItemId", saved.getSportsItem().getId());
                result.put("sportsItemName", saved.getSportsItem().getName());
            }

            return Result.success(result);
        } catch (Exception e) {
            log.error("创建免测/重测申请失败", e);
            return Result.error("创建申请失败：" + e.getMessage());
        }
    }

    @GetMapping("/exemptions")
    public Result getExemptions(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestAttribute Long userId
    ) {
        try {
            log.info("Getting exemptions for student: {}, page: {}, size: {}", userId, page, size);
            
            // 获取用户信息
            User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 获取所有申请记录
            List<ExemptionApplication> allApplications = exemptionRepository.findAllByStudentId(userId);
            log.info("Found {} applications for student {}", allApplications.size(), userId);
            
            // 手动分页
            int start = page * size;
            int end = Math.min(start + size, allApplications.size());
            List<ExemptionApplication> pageContent = start < allApplications.size() 
                ? allApplications.subList(start, end) 
                : new ArrayList<>();
            
            // 处理结果
            List<Map<String, Object>> content = pageContent.stream().map(app -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", app.getId());
                item.put("type", app.getType());
                item.put("reason", app.getReason());
                item.put("status", app.getStatus());
                item.put("applyTime", app.getApplyTime());
                
                // 设置学生信息
                item.put("studentName", app.getStudentName() != null ? app.getStudentName() : student.getRealName());
                item.put("studentNumber", app.getStudentNumber() != null ? app.getStudentNumber() : student.getStudentNumber());
                item.put("className", app.getClassName() != null ? app.getClassName() : student.getClassName());
                
                // 设置审核意见
                item.put("reviewComment", app.getReviewComment());
                item.put("teacherReviewComment", app.getTeacherReviewComment());
                item.put("adminReviewComment", app.getAdminReviewComment());
                
                // 设置体育项目信息
                if (app.getSportsItem() != null) {
                    item.put("sportsItemId", app.getSportsItem().getId());
                    item.put("sportsItemName", app.getSportsItem().getName());
                }
                
                return item;
            }).collect(Collectors.toList());

            // 构造分页结果
            Map<String, Object> result = new HashMap<>();
            result.put("content", content);
            result.put("totalElements", allApplications.size());
            result.put("totalPages", (int) Math.ceil((double) allApplications.size() / size));
            result.put("number", page);
            result.put("size", size);

            return Result.success(result);
        } catch (Exception e) {
            log.error("Error getting exemption applications", e);
            return Result.error("获取申请列表失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/exemptions/{id}")
    public Result deleteExemption(
        @PathVariable Long id,
        @RequestAttribute Long userId
    ) {
        try {
            exemptionService.deleteApplication(id, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除免测/重测申请失败", e);
            return Result.error("删除申请失败：" + e.getMessage());
        }
    }

    @PutMapping("/exemptions/{id}")
    public Result updateExemption(
        @PathVariable Long id,
        @RequestBody ExemptionApplication application,
        @RequestAttribute Long userId
    ) {
        try {
            // 获取现有申请
            ExemptionApplication existing = exemptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("申请不存在"));

            // 检查是否是本人的申请
            if (!existing.getStudentId().equals(userId)) {
                return Result.error("无权修改此申请");
            }
            
            // 检查申请状态是否允许修改
            if (!existing.getStatus().startsWith("PENDING")) {
                return Result.error("只能修改待审核的申请");
            }

            // 获取用户信息
            User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 保留原有的基本信息
            application.setId(id);
            application.setStudentId(userId);
            application.setStudentNumber(student.getStudentNumber());
            application.setStudentName(student.getRealName());
            application.setClassName(student.getClassName());
            application.setStatus(existing.getStatus());
            application.setApplyTime(existing.getApplyTime());
            application.setUpdateTime(LocalDateTime.now());
            
            // 如果是重测申请，处理体育项目
            if ("RETEST".equals(application.getType()) && application.getSportsItemId() != null) {
                SportsItem sportsItem = sportsItemRepository.findById(application.getSportsItemId())
                    .orElseThrow(() -> new RuntimeException("体育项目不存在"));
                application.setSportsItem(sportsItem);
                application.setSportsItemName(sportsItem.getName());
            }
            
            // 保存更新
            ExemptionApplication updated = exemptionRepository.save(application);
            
            // 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("id", updated.getId());
            result.put("type", updated.getType());
            result.put("reason", updated.getReason());
            result.put("status", updated.getStatus());
            result.put("applyTime", updated.getApplyTime());
            result.put("studentName", updated.getStudentName());
            result.put("studentNumber", updated.getStudentNumber());
            result.put("className", updated.getClassName());
            
            if (updated.getSportsItem() != null) {
                result.put("sportsItemId", updated.getSportsItem().getId());
                result.put("sportsItemName", updated.getSportsItem().getName());
            }

            return Result.success(result);
        } catch (Exception e) {
            log.error("修改免测/重测申请失败", e);
            return Result.error("修改申请失败：" + e.getMessage());
        }
    }

    @GetMapping("/sports-items")
    public Result getSportsItems() {
        try {
            List<SportsItem> items = sportsItemRepository.findByIsActiveTrue();
            return Result.success(items);
        } catch (Exception e) {
            log.error("获取体育项目列表失败", e);
            return Result.error("获取体育项目列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result getStudentInfo(@RequestAttribute Long userId) {
        try {
            User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Map<String, Object> info = new HashMap<>();
            info.put("real_name", student.getRealName());
            info.put("class_name", student.getClassName());
            info.put("student_number", student.getStudentNumber());
            
            return Result.success(info);
        } catch (Exception e) {
            log.error("获取学生信息失败", e);
            return Result.error("获取学生信息失败：" + e.getMessage());
        }
    }
} 