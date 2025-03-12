package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestRecord;
import com.sports.repository.TestRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

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
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<TestRecord> records = testRecordRepository.findAllWithFilters(
                sportsItemId, 
                status,
                keyword,
                pageRequest
            );
            
            log.debug("Found {} records", records.getTotalElements());
            return Result.success(records);
        } catch (Exception e) {
            log.error("Failed to get test records", e);
            return Result.error("获取记录列表失败: " + e.getMessage());
        }
    }
} 