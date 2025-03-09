package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestRecord;
import com.sports.service.TestRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/test-record")
public class TestRecordController {

    @Autowired
    private TestRecordService testRecordService;

    @GetMapping("/review")
    public Result getRecordList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long sportsItemId,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Page<TestRecord> records = testRecordService.getRecordList(
                status, teacherId, sportsItemId, null, null,
                PageRequest.of(pageNum - 1, pageSize)
            );
            
            System.out.println("Retrieved records: " + records.getContent().size());
            return Result.success(records);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取记录列表失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result addRecord(@RequestBody Map<String, Object> params) {
        try {
            // 添加日志
            System.out.println("Received params: " + params);
            
            TestRecord record = new TestRecord();
            
            // 设置关联对象
            record.setStudentId(Long.valueOf(String.valueOf(params.get("studentId"))));
            record.setSportsItemId(Long.valueOf(String.valueOf(params.get("sportsItemId"))));
            record.setTeacherId(Long.valueOf(String.valueOf(params.get("teacherId"))));
            
            // 设置成绩和时间
            record.setScore(Double.valueOf(String.valueOf(params.get("score"))));
            String testTimeStr = String.valueOf(params.get("testTime"));
            record.setTestTime(LocalDateTime.parse(testTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 设置初始状态
            record.setStatus("PENDING");
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            
            TestRecord saved = testRecordService.save(record);
            return Result.success(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加记录失败: " + e.getMessage());
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
                          @RequestParam(required = false) Long teacherId,
                          @RequestParam(required = false) Long sportsItemId) {
        try {
            testRecordService.exportToExcel(response, status, teacherId, sportsItemId);
        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
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
} 