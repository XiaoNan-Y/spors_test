package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.SportsItem;
import com.sports.entity.TestRecord;
import com.sports.entity.TestExemption;
import com.sports.repository.TestRecordRepository;
import com.sports.repository.TestExemptionRepository;
import com.sports.service.SportsItemService;
import com.sports.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin
public class TeacherController {

    private static final Logger log = LoggerFactory.getLogger(TeacherController.class);
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private SportsItemService sportsItemService;

    private final TestRecordRepository testRecordRepository;
    private final TestExemptionRepository testExemptionRepository;

    public TeacherController(TestRecordRepository testRecordRepository, TestExemptionRepository testExemptionRepository) {
        this.testRecordRepository = testRecordRepository;
        this.testExemptionRepository = testExemptionRepository;
    }

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
    public Result getClassList() {
        try {
            List<String> classNames = testExemptionRepository.findDistinctClassNames()
                .stream()
                .distinct()
                .filter(className -> className != null && !className.isEmpty())
                .sorted()
                .collect(Collectors.toList());
            return Result.success(classNames);
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

    @GetMapping("/student-records")
    public Result getStudentRecords(
        @RequestParam(required = false) String className,
        @RequestParam(required = false) Long sportsItemId,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<TestRecord> records = testRecordRepository.findByFiltersForTeacher(
                className,
                sportsItemId,
                keyword,
                pageRequest
            );
            
            log.debug("Found {} records for class {}", records.getTotalElements(), className);
            return Result.success(records);
        } catch (Exception e) {
            log.error("Failed to get student records", e);
            return Result.error("获取学生成绩记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/exemptions")
    public Result getExemptionList(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.debug("查询参数: className={}, type={}, status={}, keyword={}, page={}, size={}",
                className, type, status, keyword, page, size);
            
            Page<TestExemption> exemptions = testExemptionRepository.findByFilters(
                className,
                type,
                status,
                null,
                keyword,
                PageRequest.of(page, size)
            );
            
            log.debug("查询结果: 总数={}, 当前页数据量={}", 
                exemptions.getTotalElements(), exemptions.getContent().size());
            
            return Result.success(exemptions);
        } catch (Exception e) {
            log.error("获取申请列表失败", e);
            return Result.error("获取申请列表失败：" + e.getMessage());
        }
    }

    @PutMapping("/exemptions/{id}/review")
    public Result reviewExemption(
            @PathVariable Long id,
            @RequestBody TestExemption exemption) {
        try {
            TestExemption existing = testExemptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("申请不存在"));

            existing.setStatus(exemption.getStatus());
            existing.setTeacherReviewComment(exemption.getTeacherReviewComment());
            existing.setTeacherReviewTime(LocalDateTime.now());
            existing.setUpdateTime(LocalDateTime.now());

            testExemptionRepository.save(existing);
            return Result.success(existing);
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }
} 