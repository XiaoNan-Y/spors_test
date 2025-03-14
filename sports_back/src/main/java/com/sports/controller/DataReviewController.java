package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestRecord;
import com.sports.repository.TestRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/data-review")
@CrossOrigin
public class DataReviewController {

    private static final Logger log = LoggerFactory.getLogger(DataReviewController.class);

    private final TestRecordRepository testRecordRepository;

    public DataReviewController(TestRecordRepository testRecordRepository) {
        this.testRecordRepository = testRecordRepository;
    }

    @GetMapping("/list")
    public Result getList(
        @RequestParam(required = false) Long sportsItemId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Received parameters - sportsItemId: {}, status: {}, keyword: {}, page: {}, size: {}", 
                    sportsItemId, status, keyword, page, size);
                    
            // 处理空字符串参数
            status = (status != null && status.trim().isEmpty()) ? null : status;
            keyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;
            
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<TestRecord> records = testRecordRepository.findAllWithFilters(
                sportsItemId, 
                status,
                keyword,
                pageRequest
            );
            
            log.info("Query result - total elements: {}, total pages: {}", 
                    records.getTotalElements(), records.getTotalPages());
            
            if (records.isEmpty()) {
                log.warn("No records found with the given filters");
            }
            
            return Result.success(records);
        } catch (Exception e) {
            log.error("Failed to get test records: ", e);
            return Result.error("获取记录列表失败: " + e.getMessage());
        }
    }

    // 添加审核方法
    @PutMapping("/review/{id}")
    public Result reviewRecord(@PathVariable Long id, @RequestBody Map<String, String> reviewData) {
        try {
            log.info("Reviewing record with id: {}", id);
            TestRecord record = testRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

            String status = reviewData.get("status");
            String comment = reviewData.get("comment");
            
            log.debug("Updating record - status: {}, comment: {}", status, comment);

            record.setStatus(status);
            record.setReviewComment(comment);
            record.setReviewTime(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());

            TestRecord savedRecord = testRecordRepository.save(record);
            log.info("Record reviewed successfully");
            
            return Result.success(savedRecord);
        } catch (Exception e) {
            log.error("Review failed: ", e);
            return Result.error("审核失败: " + e.getMessage());
        }
    }
} 