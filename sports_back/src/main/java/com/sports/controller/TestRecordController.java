package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestRecord;
import com.sports.entity.User;
import com.sports.entity.SportsItem;
import com.sports.service.TestRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/test-records")
public class TestRecordController {
    @Autowired
    private TestRecordService testRecordService;
    
    @GetMapping("/review")
    public Result getRecordList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long sportsItemId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Fetching records with params: status={}, teacherId={}, sportsItemId={}, startDate={}, endDate={}, page={}, size={}",
                    status, teacherId, sportsItemId, startDate, endDate, page, size);
            
            LocalDate start = null;
            LocalDate end = null;
            
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    start = LocalDate.parse(startDate);
                } catch (Exception e) {
                    log.warn("Invalid startDate format: {}", startDate);
                }
            }
            
            if (endDate != null && !endDate.isEmpty()) {
                try {
                    end = LocalDate.parse(endDate);
                } catch (Exception e) {
                    log.warn("Invalid endDate format: {}", endDate);
                }
            }
            
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "testTime"));
            Page<TestRecord> records = testRecordService.getRecordList(
                status, teacherId, sportsItemId, start, end, pageRequest
            );
            
            log.info("Found {} records", records.getTotalElements());
            return Result.success(records);
            
        } catch (Exception e) {
            log.error("Error fetching records: ", e);
            return Result.error("获取记录列表失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/review")
    public Result reviewRecord(@RequestBody Map<String, Object> params) {
        try {
            log.info("Reviewing record with params: {}", params);
            
            Long recordId = Long.parseLong(params.get("id").toString());
            String status = (String) params.get("status");
            String reviewComment = (String) params.get("reviewComment");
            Long reviewerId = Long.parseLong(params.get("reviewerId").toString());
            
            TestRecord updated = testRecordService.reviewRecord(recordId, status, reviewComment, reviewerId);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("Review record failed:", e);
            return Result.error("审核失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result updateRecord(@PathVariable Long id, @RequestBody TestRecord record) {
        try {
            log.info("Updating record with id: {} and data: {}", id, record);
            
            // 验证必要字段
            if (record.getStudent() == null || record.getStudent().getId() == null) {
                return Result.error("学生信息不能为空");
            }
            if (record.getSportsItem() == null || record.getSportsItem().getId() == null) {
                return Result.error("测试项目不能为空");
            }
            if (record.getTeacher() == null || record.getTeacher().getId() == null) {
                return Result.error("教师信息不能为空");
            }
            if (record.getScore() == null) {
                return Result.error("成绩不能为空");
            }
            if (record.getTestTime() == null) {
                return Result.error("测试时间不能为空");
            }
            
            record.setId(id);
            TestRecord updated = testRecordService.updateRecord(record);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("Update record failed:", e);
            return Result.error("修改记录失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result addRecord(@RequestBody TestRecord record) {
        try {
            log.info("Received record data: {}", record);
            
            // 验证必要字段
            if (record.getStudent() == null || record.getStudent().getId() == null) {
                return Result.error("学生信息不能为空");
            }
            if (record.getSportsItem() == null || record.getSportsItem().getId() == null) {
                return Result.error("测试项目不能为空");
            }
            if (record.getTeacher() == null || record.getTeacher().getId() == null) {
                return Result.error("教师信息不能为空");
            }
            if (record.getScore() == null) {
                return Result.error("成绩不能为空");
            }
            if (record.getTestTime() == null) {
                return Result.error("测试时间不能为空");
            }

            // 设置初始状态和时间
            record.setStatus("PENDING");
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            
            // 设置关联实体
            record.setStudent(new User(record.getStudent().getId()));
            record.setTeacher(new User(record.getTeacher().getId()));
            record.setSportsItem(new SportsItem(record.getSportsItem().getId()));
            
            log.info("Saving record with data: {}", record);
            TestRecord saved = testRecordService.save(record);
            log.info("Record saved successfully: {}", saved);
            
            return Result.success(saved);
        } catch (Exception e) {
            log.error("Error adding record:", e);
            return Result.error("添加记录失败: " + e.getMessage());
        }
    }
} 