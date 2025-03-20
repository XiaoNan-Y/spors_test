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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    @GetMapping("/exemptions")
    public Result getExemptions(
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestAttribute Long userId
    ) {
        try {
            Page<ExemptionApplicationDTO> applications = exemptionService.getStudentApplications(
                userId, type, PageRequest.of(page, size));
            
            // 构造前端需要的分页格式
            Map<String, Object> result = new HashMap<>();
            result.put("content", applications.getContent());
            result.put("totalElements", applications.getTotalElements());
            result.put("totalPages", applications.getTotalPages());
            result.put("number", applications.getNumber());
            result.put("size", applications.getSize());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取免测/重测申请列表失败", e);
            return Result.error("获取申请列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/exemptions")
    public Result submitExemption(
        @RequestBody ExemptionApplication application,
        @RequestAttribute Long userId
    ) {
        try {
            User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            application.setStudent(student);
            
            ExemptionApplicationDTO savedApplication = exemptionService.submitApplication(application);
            return Result.success(savedApplication);
        } catch (Exception e) {
            log.error("提交免测/重测申请失败", e);
            return Result.error("提交申请失败：" + e.getMessage());
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
            // 检查是否是本人的申请
            ExemptionApplicationDTO existingDTO = exemptionService.getApplicationById(id);
            if (!existingDTO.getStudentId().equals(userId)) {
                return Result.error("无权修改此申请");
            }
            
            // 检查申请状态是否允许修改
            if (!existingDTO.getStatus().startsWith("PENDING")) {
                return Result.error("只能修改待审核的申请");
            }
            
            // 设置必要的字段
            application.setId(id);
            application.setStudentId(userId);
            application.setStatus(existingDTO.getStatus());
            
            // 更新申请
            ExemptionApplicationDTO updated = exemptionService.updateApplication(application);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("修改免测/重测申请失败", e);
            return Result.error("修改申请失败：" + e.getMessage());
        }
    }
} 