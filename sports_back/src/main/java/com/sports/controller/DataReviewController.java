package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.TestRecord;
import com.sports.repository.TestRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    @GetMapping("/test-records/export")
    public ResponseEntity<byte[]> exportTestRecords(
            @RequestParam(required = false) Long sportsItemId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        try {
            log.info("Exporting test records - sportsItemId: {}, status: {}, keyword: {}", 
                    sportsItemId, status, keyword);
                    
            // 处理空字符串参数
            status = (status != null && status.trim().isEmpty()) ? null : status;
            keyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;
            
            // 获取数据
            List<TestRecord> records = testRecordRepository.findAllForExport(
                sportsItemId,
                status,
                keyword
            );
            
            // 生成Excel文件
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("体测数据");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] columnHeaders = {"序号", "学号", "姓名", "班级", "测试项目", "成绩", "状态", "创建时间"};
            for (int i = 0; i < columnHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnHeaders[i]);
            }
            
            // 填充数据
            int rowNum = 1;
            for (TestRecord record : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(record.getStudentNumber() != null ? record.getStudentNumber() : "");
                row.createCell(2).setCellValue(record.getStudentName() != null ? record.getStudentName() : "");
                row.createCell(3).setCellValue(record.getClassName() != null ? record.getClassName() : "");
                row.createCell(4).setCellValue(record.getSportsItem() != null ? record.getSportsItem().getName() : "");
                row.createCell(5).setCellValue(record.getScore() != null ? record.getScore() : 0);
                row.createCell(6).setCellValue(getStatusText(record.getStatus()));
                row.createCell(7).setCellValue(formatDateTime(record.getCreatedAt()));
            }
            
            // 自动调整列宽
            for (int i = 0; i < columnHeaders.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 写入输出流
            workbook.write(outputStream);
            workbook.close();
            
            // 设置响应头
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String filename = "体测数据_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
            responseHeaders.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(outputStream.toByteArray(), responseHeaders, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to export test records: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 辅助方法
    private String getStatusText(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "待审核";
            case "APPROVED": return "已通过";
            case "REJECTED": return "已驳回";
            default: return status;
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
} 