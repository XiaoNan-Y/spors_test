package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestExemption;
import com.sports.repository.TestExemptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exemptions")
@CrossOrigin
public class TestExemptionController {

    private static final Logger log = LoggerFactory.getLogger(TestExemptionController.class);

    @Autowired
    private TestExemptionRepository exemptionRepository;

    @GetMapping
    public Result getList(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String studentNumber,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            log.info("开始查询免测申请列表");
            log.debug("查询参数详情:");
            log.debug("- className=[{}], length={}", className, className != null ? className.length() : 0);
            log.debug("- type=[{}], length={}", type, type != null ? type.length() : 0);
            log.debug("- status=[{}], length={}", status, status != null ? status.length() : 0);
            log.debug("- studentNumber=[{}], length={}", studentNumber, studentNumber != null ? studentNumber.length() : 0);
            log.debug("- keyword=[{}], length={}", keyword, keyword != null ? keyword.length() : 0);
            log.debug("- pageNum={}, pageSize={}", pageNum, pageSize);
            
            Page<TestExemption> exemptions = exemptionRepository.findByFilters(
                className, type, status, studentNumber, keyword, 
                PageRequest.of(pageNum, pageSize));
            
            // 转换为简单的数据传输对象，确保包含学生姓名
            Page<Map<String, Object>> simplifiedRecords = exemptions.map(exemption -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", exemption.getId());
                map.put("studentName", exemption.getStudentName());  // 确保包含学生姓名
                map.put("studentNumber", exemption.getStudentNumber());
                map.put("className", exemption.getClassName());
                map.put("type", exemption.getType());
                map.put("reason", exemption.getReason());
                map.put("status", exemption.getStatus());
                map.put("teacherReviewComment", exemption.getTeacherReviewComment());
                map.put("teacherReviewTime", exemption.getTeacherReviewTime());
                map.put("adminReviewComment", exemption.getAdminReviewComment());
                map.put("adminReviewTime", exemption.getAdminReviewTime());
                map.put("createdAt", exemption.getCreatedAt());
                return map;
            });
            
            log.info("查询结果: 总条数={}, 当前页数据量={}", 
                exemptions.getTotalElements(), exemptions.getContent().size());
            
            if (exemptions.isEmpty()) {
                log.warn("查询结果为空，检查数据库中是否存在数据");
                long total = exemptionRepository.countAll();
                log.info("数据库中总记录数: {}", total);
                
                if (total > 0) {
                    log.warn("数据库中有数据但查询结果为空，可能是查询条件限制导致");
                    // 打印一条原始数据作为参考
                    exemptionRepository.findAll(PageRequest.of(0, 1))
                        .getContent()
                        .forEach(e -> log.debug("数据库样本数据: {}", e));
                }
            }
            
            return Result.success(simplifiedRecords);
        } catch (Exception e) {
            log.error("查询免测申请列表失败", e);
            return Result.error("获取申请列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/classes")
    public Result getClassList() {
        try {
            List<String> classNames = exemptionRepository.findDistinctClassNames()
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

    @PutMapping("/{id}/review")
    public Result review(
            @PathVariable Long id,
            @RequestBody TestExemption exemption) {
        try {
            TestExemption existing = exemptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("申请不存在"));

            existing.setStatus(exemption.getStatus());
            existing.setReviewComment(exemption.getReviewComment());
            existing.setReviewerId(exemption.getReviewerId());
            existing.setReviewerName(exemption.getReviewerName());
            existing.setReviewTime(LocalDateTime.now());
            existing.setUpdateTime(LocalDateTime.now());

            exemptionRepository.save(existing);
            return Result.success(existing);
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }
} 