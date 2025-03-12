package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.SportsItem;
import com.sports.entity.TestRecord;
import com.sports.service.SportsItemService;
import com.sports.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private SportsItemService sportsItemService;

    // 获取体育项目列表
    @GetMapping("/sports-items")
    public Result getSportsItems() {
        try {
            List<SportsItem> items = sportsItemService.getAllActiveItems();
            return Result.success(items);
        } catch (Exception e) {
            return Result.error("获取体育项目列表失败：" + e.getMessage());
        }
    }

    // 获取教师负责的班级列表
    @GetMapping("/classes")
    public Result getClasses() {
        try {
            // TODO: 从认证信息中获取教师ID
            Long teacherId = 1L;
            List<String> classes = teacherService.getTeacherClasses(teacherId);
            return Result.success(classes);
        } catch (Exception e) {
            return Result.error("获取班级列表失败：" + e.getMessage());
        }
    }

    // 获取成绩记录列表
    @GetMapping("/test-records")
    public Result getTestRecords(
            @RequestParam(required = false) Long sportsItemId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentNumber,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // TODO: 从认证信息中获取教师ID
            Long teacherId = 1L;
            Page<TestRecord> page = teacherService.getTestRecords(
                teacherId, sportsItemId, className, studentNumber,
                PageRequest.of(pageNum - 1, pageSize)
            );
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("获取成绩记录失败：" + e.getMessage());
        }
    }

    // 录入成绩
    @PostMapping("/test-records")
    public Result addTestRecord(@RequestBody TestRecord record) {
        try {
            // TODO: 从认证信息中获取教师ID
            Long teacherId = 1L;
            TestRecord saved = teacherService.addTestRecord(teacherId, record);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("录入成绩失败：" + e.getMessage());
        }
    }

    // 修改成绩
    @PutMapping("/test-records/{id}")
    public Result updateTestRecord(@PathVariable Long id, @RequestBody TestRecord record) {
        try {
            // TODO: 从认证信息中获取教师ID
            Long teacherId = 1L;
            record.setId(id);
            TestRecord updated = teacherService.updateTestRecord(teacherId, record);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("修改成绩失败：" + e.getMessage());
        }
    }

    // 删除成绩
    @DeleteMapping("/test-records/{id}")
    public Result deleteTestRecord(@PathVariable Long id) {
        try {
            // TODO: 从认证信息中获取教师ID
            Long teacherId = 1L;
            teacherService.deleteTestRecord(teacherId, id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除成绩失败：" + e.getMessage());
        }
    }
} 