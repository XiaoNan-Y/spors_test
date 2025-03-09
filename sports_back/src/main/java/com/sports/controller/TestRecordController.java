package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestRecord;
import com.sports.service.TestRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

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
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

            PageRequest pageRequest = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.DESC, "testTime"));
                
            Page<TestRecord> records = testRecordService.getRecordList(
                status, teacherId, sportsItemId, start, end, pageRequest
            );

            return Result.success(records);
        } catch (Exception e) {
            log.error("获取记录列表失败", e);
            return Result.error("获取记录列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result addRecord(@RequestBody TestRecord record) {
        try {
            // 检查必要字段
            if (record.getStudent() == null || record.getStudent().getId() == null) {
                return Result.error("学生信息不能为空");
            }
            if (record.getTeacher() == null || record.getTeacher().getId() == null) {
                return Result.error("教师信息不能为空");
            }
            if (record.getSportsItem() == null || record.getSportsItem().getId() == null) {
                return Result.error("测试项目不能为空");
            }
            if (record.getScore() == null) {
                return Result.error("成绩不能为空");
            }
            if (record.getTestTime() == null) {
                return Result.error("测试时间不能为空");
            }

            TestRecord saved = testRecordService.save(record);
            return Result.success(saved);
        } catch (Exception e) {
            log.error("添加记录失败", e);
            return Result.error("添加记录失败: " + e.getMessage());
        }
    }

    // ... 其他方法保持不变 ...
}