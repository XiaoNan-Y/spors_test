package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.ExemptionApplication;
import com.sports.entity.RetestApplication;
import com.sports.entity.User;
import com.sports.repository.RetestApplicationRepository;
import com.sports.repository.UserRepository;
import com.sports.service.ExemptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/exemptions")
@CrossOrigin
public class ExemptionController {

    @Autowired
    private ExemptionService exemptionService;

    @Autowired
    private RetestApplicationRepository retestApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    // 学生提交申请
    @PostMapping("/student/submit")
    public Result submitApplication(@RequestBody ExemptionApplication application) {
        try {
            // 验证免测申请必须上传证明材料
            if ("EXEMPTION".equals(application.getType()) && 
                (application.getAttachmentUrl() == null || application.getAttachmentUrl().isEmpty())) {
                return Result.error("免测申请必须上传证明材料");
            }
            
            application.setStatus("PENDING");
            application.setApplyTime(LocalDateTime.now());
            application.setUpdateTime(LocalDateTime.now());
            
            ExemptionApplication saved = exemptionService.submitApplication(application);
            return Result.success(saved);
        } catch (Exception e) {
            log.error("提交申请失败", e);
            return Result.error("提交申请失败：" + e.getMessage());
        }
    }

    // 学生提交重测申请
    @PostMapping("/student/submit-retest")
    public Result submitRetestApplication(@RequestBody RetestApplication application) {
        try {
            // 验证必填字段
            if (application.getSportsItemId() == null) {
                return Result.error("重测申请必须选择体育项目");
            }
            
            // 设置申请状态和时间
            application.setStatus("PENDING");
            application.setApplyTime(LocalDateTime.now());
            application.setUpdateTime(LocalDateTime.now());
            
            // 获取学生信息
            User student = userRepository.findById(application.getStudentId())
                .orElseThrow(() -> new RuntimeException("学生信息不存在"));
                
            // 设置学生相关信息
            application.setStudentNumber(student.getStudentNumber());
            application.setStudentName(student.getRealName());
            application.setClassName(student.getClassName());
            
            RetestApplication saved = retestApplicationRepository.save(application);
            return Result.success(saved);
        } catch (Exception e) {
            log.error("提交重测申请失败", e);
            return Result.error("提交重测申请失败：" + e.getMessage());
        }
    }

    // 学生获取自己的申请列表
    @GetMapping("/student/list")
    public Result getStudentApplications(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Getting applications for student: {}", studentId);
            
            // 添加调试日志
            log.debug("Query params - studentId: {}, page: {}, size: {}", studentId, page, size);
            
            Page<ExemptionApplication> applications = exemptionService
                .getStudentApplications(studentId, PageRequest.of(page, size));
            
            // 添加调试日志
            log.debug("Found applications: {}", applications.getContent());
            
            // 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("content", applications.getContent());
            result.put("totalElements", applications.getTotalElements());
            result.put("totalPages", applications.getTotalPages());
            result.put("size", applications.getSize());
            result.put("number", applications.getNumber());
            
            return Result.success(result);
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
            log.info("Getting exemption applications - keyword: {}, status: {}, page: {}, size: {}", 
                    keyword, status, page, size);
            
            // 添加调试日志
            log.debug("Query parameters - keyword: '{}', status: '{}'", keyword, status);
            
            Page<ExemptionApplication> applications = exemptionService
                .getAdminExemptionApplications(keyword, status, PageRequest.of(page, size));
            
            // 添加调试日志
            log.debug("Found {} applications", applications.getTotalElements());
            applications.getContent().forEach(app -> 
                log.debug("Application: id={}, studentName={}, status={}, applyTime={}", 
                    app.getId(), app.getStudentName(), app.getStatus(), app.getApplyTime())
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", applications.getContent());
            result.put("totalElements", applications.getTotalElements());
            result.put("totalPages", applications.getTotalPages());
            result.put("size", applications.getSize());
            result.put("number", applications.getNumber());
            
            return Result.success(result);
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

    // 导出免测申请数据
    @GetMapping("/admin/export")
    public ResponseEntity<byte[]> exportExemptions() {
        try {
            byte[] excelBytes = exemptionService.exportApprovedExemptions();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "免测申请数据.xlsx");
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("导出免测申请数据失败", e);
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/student/{id}")
    public Result deleteApplication(
        @PathVariable Long id,
        @RequestParam Long studentId
    ) {
        try {
            exemptionService.deleteApplication(id, studentId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除申请失败：" + e.getMessage());
        }
    }

    @GetMapping("/retest-applications/student/list")
    public Result getStudentRetestApplications(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Getting retest applications for student: {}", studentId);
            
            Page<RetestApplication> applications = retestApplicationRepository.findByStudentId(
                studentId, 
                PageRequest.of(page, size)
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", applications.getContent());
            result.put("totalElements", applications.getTotalElements());
            result.put("totalPages", applications.getTotalPages());
            result.put("size", applications.getSize());
            result.put("number", applications.getNumber());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取重测申请列表失败", e);
            return Result.error("获取重测申请列表失败：" + e.getMessage());
        }
    }
} 