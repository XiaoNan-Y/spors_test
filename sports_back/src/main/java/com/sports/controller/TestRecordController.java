package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestRecord;
import com.sports.service.TestRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin/test-record")
public class TestRecordController {

    private static final Logger log = LoggerFactory.getLogger(TestRecordController.class);

    @Autowired
    private TestRecordService testRecordService;

    @GetMapping("/review")
    public Result getRecordList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long sportsItemId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            log.debug("Fetching test records with status: {}, sportsItemId: {}", 
                      status, sportsItemId);
                  
            Page<TestRecord> records = testRecordService.getRecordList(
                status, sportsItemId, null, null,
                PageRequest.of(pageNum - 1, pageSize)
            );
            
            log.debug("Found {} records", records.getTotalElements());
            return Result.success(records);
        } catch (Exception e) {
            log.error("Error fetching test records", e);
            return Result.error("获取记录列表失败: " + e.getMessage());
        }
    }


    @PutMapping("/review")
    public Result reviewRecord(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(String.valueOf(params.get("id")));
            String status = String.valueOf(params.get("status"));
            String comment = String.valueOf(params.get("comment"));
            // 从当前登录用户中获取审核人ID，或从请求参数中获取
            Long reviewerId = Long.valueOf(String.valueOf(params.get("reviewerId"))); // 添加审核人ID
        
            TestRecord record = testRecordService.reviewRecord(id, status, comment, reviewerId);
            return Result.success(record);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审核失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createRecord(@RequestBody TestRecord record) {
        try {
            // 确保设置了必要的字段
            if (record.getStudentNumber() == null || record.getSportsItemId() == null) {
                return ResponseEntity.badRequest().body("学号和体测项目ID不能为空");
            }

            record.setStatus("PENDING");  // 设置初始状态为待审核
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            
            TestRecord savedRecord = testRecordService.save(record);
            return ResponseEntity.ok(savedRecord);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建记录失败: " + e.getMessage());
        }
    }

    @PostMapping("/check-abnormal")
    public Result checkAbnormalScore(@RequestBody TestRecord record) {
        try {
            boolean isAbnormal = testRecordService.checkAbnormalScore(record);
            String reason = isAbnormal ? testRecordService.getAbnormalReason(record) : null;
            return Result.success(new HashMap<String, Object>() {{
                put("isAbnormal", isAbnormal);
                put("reason", reason);
            }});
        } catch (Exception e) {
            return Result.error("检查异常失败: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public Result importData(@RequestParam("file") MultipartFile file) {
        try {
            List<TestRecord> records = testRecordService.importFromExcel(file);
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportData(HttpServletResponse response,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) Long sportsItemId) {
        try {
            testRecordService.exportToExcel(response, status, sportsItemId);
        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result updateRecord(@PathVariable Long id, @RequestBody TestRecord record) {
        try {
            record.setId(id);
            TestRecord updated = testRecordService.updateRecord(record);
            return Result.success(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("修改失败: " + e.getMessage());
        }
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            testRecordService.generateTemplate(response);
        } catch (Exception e) {
            throw new RuntimeException("下载模板失败: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistoryRecords(
            @RequestParam String studentNumber,
            @RequestParam Long sportsItemId,
            @RequestParam(required = false) Long excludeId) {
        try {
            List<TestRecord> records = testRecordService.getHistoryRecords(studentNumber, sportsItemId, excludeId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取历史记录失败：" + e.getMessage());
        }
    }

    @PutMapping("/modify")
    public Result modifyReview(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(String.valueOf(params.get("id")));
            String status = String.valueOf(params.get("status"));
            String comment = String.valueOf(params.get("comment"));
            Long reviewerId = Long.valueOf(String.valueOf(params.get("reviewerId")));
        
            TestRecord record = testRecordService.modifyReview(id, status, comment, reviewerId);
            return Result.success(record);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("修改失败: " + e.getMessage());
        }
    }
} 