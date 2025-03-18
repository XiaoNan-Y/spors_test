package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestRecord;
import com.sports.repository.TestRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class DataReviewController {

    private static final Logger log = LoggerFactory.getLogger(DataReviewController.class);

    @Autowired
    private TestRecordRepository testRecordRepository;

    @GetMapping("/test-records")
    public Result getTestRecords(
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
            return Result.error("获取测试记录失败：" + e.getMessage());
        }
    }

    @PutMapping("/test-records/{id}/review")
    public Result reviewTestRecord(
            @PathVariable Long id,
            @RequestBody TestRecord record) {
        try {
            log.info("Reviewing record with id: {}", id);
            TestRecord existing = testRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

            String status = record.getStatus();
            String comment = record.getReviewComment();
            
            log.debug("Updating record - status: {}, comment: {}", status, comment);

            existing.setStatus(status);
            existing.setReviewComment(comment);
            existing.setReviewTime(record.getReviewTime());
            existing.setUpdatedAt(LocalDateTime.now());

            TestRecord saved = testRecordRepository.save(existing);
            log.info("Record reviewed successfully");
            
            return Result.success(saved);
        } catch (Exception e) {
            log.error("Review failed: ", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }
} 