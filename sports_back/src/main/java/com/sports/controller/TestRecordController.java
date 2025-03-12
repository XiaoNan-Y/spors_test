package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestRecord;
import com.sports.service.TestRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/test-record")
public class TestRecordController {
    
    private static final Logger log = LoggerFactory.getLogger(TestRecordController.class);
    
    @Autowired
    private TestRecordService testRecordService;

    @GetMapping
    public Result getList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long sportsItemId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Page<TestRecord> page = testRecordService.getRecordList(
                status, 
                sportsItemId,
                PageRequest.of(pageNum - 1, pageSize)
            );
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取测试记录列表失败", e);
            return Result.error("获取测试记录列表失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result add(@RequestBody TestRecord record) {
        try {
            // 设置初始状态和时间
            record.setStatus("PENDING");
            record.setTestTime(LocalDateTime.now());
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            
            TestRecord saved = testRecordService.save(record);
            return Result.success(saved);
        } catch (Exception e) {
            log.error("添加测试记录失败", e);
            return Result.error("添加测试记录失败：" + e.getMessage());
        }
    }

    @PutMapping("/review")
    public Result review(@RequestBody TestRecord record) {
        try {
            TestRecord reviewed = testRecordService.reviewRecord(
                record.getId(),
                record.getStatus(),
                record.getReviewComment(),
                1L  // TODO: 从认证信息中获取reviewerId
            );
            return Result.success(reviewed);
        } catch (Exception e) {
            log.error("审核测试记录失败", e);
            return Result.error("审核测试记录失败：" + e.getMessage());
        }
    }

    @GetMapping("/history")
    public Result getHistory(
            @RequestParam String studentNumber,
            @RequestParam Long sportsItemId,
            @RequestParam(required = false) Long excludeId) {
        try {
            List<TestRecord> history = testRecordService.getHistoryRecords(
                studentNumber,
                sportsItemId,
                excludeId
            );
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取历史记录失败", e);
            return Result.error("获取历史记录失败：" + e.getMessage());
        }
    }

    @PostMapping("/import")
    public Result importData(@RequestParam("file") MultipartFile file) {
        try {
            List<TestRecord> imported = testRecordService.importFromExcel(file);
            return Result.success(imported);
        } catch (Exception e) {
            log.error("导入数据失败", e);
            return Result.error("导入数据失败：" + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportData(
            HttpServletResponse response,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long sportsItemId) throws IOException {
        try {
            testRecordService.exportToExcel(response, status, sportsItemId);
        } catch (Exception e) {
            log.error("导出数据失败", e);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        try {
            testRecordService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载模板失败", e);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"msg\":\"下载模板失败：" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/modify")
    public Result modifyReview(@RequestBody TestRecord record) {
        try {
            TestRecord modified = testRecordService.modifyReview(
                record.getId(),
                record.getStatus(),
                record.getReviewComment(),
                1L  // TODO: 从认证信息中获取reviewerId
            );
            return Result.success(modified);
        } catch (Exception e) {
            log.error("修改审核记录失败", e);
            return Result.error("修改审核记录失败：" + e.getMessage());
        }
    }
} 