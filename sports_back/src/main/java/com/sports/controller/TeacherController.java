package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.SportsItem;
import com.sports.entity.TestRecord;
import com.sports.entity.TestExemption;
import com.sports.entity.Student;
import com.sports.repository.TestRecordRepository;
import com.sports.repository.TestExemptionRepository;
import com.sports.repository.StudentRepository;
import com.sports.service.SportsItemService;
import com.sports.service.TeacherService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;

import com.sports.dto.ClassStatisticsDTO;

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
    private final StudentRepository studentRepository;

    public TeacherController(TestRecordRepository testRecordRepository, TestExemptionRepository testExemptionRepository, StudentRepository studentRepository) {
        this.testRecordRepository = testRecordRepository;
        this.testExemptionRepository = testExemptionRepository;
        this.studentRepository = studentRepository;
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
            // 从成绩记录中获取所有不重复的班级
            List<String> classNames = testRecordRepository.findDistinctClassNames();
            log.debug("获取到的班级列表: {}", classNames);
            return Result.success(classNames);
        } catch (Exception e) {
            log.error("获取班级列表失败", e);
            return Result.error("获取班级列表失败：" + e.getMessage());
        }
    }

    // 获取成绩记录列表
    @GetMapping("/test-records")
    public Result getTestRecords(
        @RequestParam(required = false) String className,
        @RequestParam(required = false) Long sportsItemId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String studentNumber,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        try {
            log.info("Getting test records with filters: className={}, sportsItemId={}, status={}, studentNumber={}, page={}, size={}", 
                     className, sportsItemId, status, studentNumber, page, size);
                 
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<TestRecord> records = testRecordRepository.findByFilters(
                className,
                sportsItemId,
                status,
                studentNumber,
                pageRequest
            );
            
            log.info("Found {} records", records.getTotalElements());
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取成绩列表失败", e);
            return Result.error("获取成绩列表失败：" + e.getMessage());
        }
    }

    // 录入成绩
    @PostMapping("/test-records")
    public Result addTestRecord(@RequestBody TestRecord record) {
        try {
            log.info("开始录入成绩");
            log.debug("录入数据: {}", record);
            
            // 从学号查询学生信息
            Student student = studentRepository.findByStudentNumber(record.getStudentNumber())
                .orElseThrow(() -> new RuntimeException("未找到学生信息"));
            
            // 设置学生相关信息
            record.setStudentNumber(student.getStudentNumber());
            record.setStudentName(student.getRealName());
            record.setClassName(student.getClassName());
            
            // 设置默认状态
            if (record.getStatus() == null) {
                record.setStatus("PENDING");
            }
            
            // 设置时间
            LocalDateTime now = LocalDateTime.now();
            record.setCreatedAt(now);
            record.setUpdatedAt(now);
            
            TestRecord saved = testRecordRepository.save(record);
            log.info("成绩录入成功，ID={}, 学生姓名={}", saved.getId(), saved.getStudentName());
            
            return Result.success(saved);
        } catch (Exception e) {
            log.error("录入成绩失败", e);
            return Result.error("录入成绩失败：" + e.getMessage());
        }
    }

    // 修改成绩
    @PutMapping("/test-records/{id}")
    @Transactional
    public Result updateTestRecord(@PathVariable Long id, @RequestBody TestRecord record) {
        try {
            log.info("开始更新成绩记录, id={}", id);
            
            // 1. 获取现有记录，使用 EntityGraph 确保关联实体被加载
            TestRecord existing = testRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成绩记录不存在"));
            
            // 2. 只更新必要的字段
            existing.setScore(record.getScore());
            existing.setSportsItemId(record.getSportsItemId());
            existing.setStatus("PENDING");
            existing.setUpdatedAt(LocalDateTime.now());
            
            // 3. 保存更新
            TestRecord updated = testRecordRepository.save(existing);
            
            // 4. 构造返回数据，只包含必要字段
            Map<String, Object> response = new HashMap<>();
            response.put("id", updated.getId());
            response.put("studentNumber", updated.getStudentNumber());
            response.put("studentName", updated.getStudentName());
            response.put("className", updated.getClassName());
            response.put("sportsItemId", updated.getSportsItemId());
            response.put("score", updated.getScore());
            response.put("status", updated.getStatus());
            response.put("updatedAt", updated.getUpdatedAt());
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("更新成绩失败: {}", e.getMessage());
            return Result.error("更新成绩失败: " + e.getMessage());
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
            log.info("开始查询学生成绩记录");
            log.debug("查询参数: className={}, sportsItemId={}, keyword={}, page={}, size={}", 
                className, sportsItemId, keyword, page, size);

            PageRequest pageRequest = PageRequest.of(page, size);
            Page<TestRecord> records = testRecordRepository.findByFiltersForTeacher(
                className,
                sportsItemId,
                keyword,
                pageRequest
            );

            log.info("查询结果: 总记录数={}, 当前页记录数={}", 
                records.getTotalElements(), records.getContent().size());

            return Result.success(records);
        } catch (Exception e) {
            log.error("获取学生成绩记录失败", e);
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

    @PutMapping("/exemptions/{id}/modify")
    public Result modifyExemption(
            @PathVariable Long id,
            @RequestBody TestExemption exemption) {
        try {
            log.info("开始修改审核，id={}", id);
            TestExemption existing = testExemptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("申请不存在"));

            // 更新审核状态和意见
            existing.setStatus(exemption.getStatus());
            existing.setTeacherReviewComment(exemption.getTeacherReviewComment());
            existing.setTeacherReviewTime(LocalDateTime.now());
            existing.setUpdateTime(LocalDateTime.now());

            // 如果状态改为待审核，清除管理员的审核信息
            if ("PENDING".equals(exemption.getStatus())) {
                existing.setAdminReviewComment(null);
                existing.setAdminReviewTime(null);
            }

            log.debug("修改审核信息：status={}, comment={}", 
                exemption.getStatus(), exemption.getTeacherReviewComment());
            
            TestExemption saved = testExemptionRepository.save(existing);
            log.info("修改审核成功");
            
            return Result.success(saved);
        } catch (Exception e) {
            log.error("修改审核失败", e);
            return Result.error("修改审核失败：" + e.getMessage());
        }
    }

    @GetMapping("/exemptions/export")
    public ResponseEntity<byte[]> exportExemptions(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        try {
            // 查询数据
            List<TestExemption> exemptions = testExemptionRepository.findByFiltersForExport(
                className, type, status, null, keyword);

            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("免测重测申请记录");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] columnHeaders = {"序号", "学号", "学生姓名", "班级", "申请类型", "申请原因", 
                              "状态", "教师审核意见", "教师审核时间", "管理员审核意见", "管理员审核时间"};
            
            // 设置列宽
            sheet.setColumnWidth(0, 2500);  // 序号
            sheet.setColumnWidth(1, 4000);  // 学号
            sheet.setColumnWidth(2, 4000);  // 学生姓名
            sheet.setColumnWidth(3, 4000);  // 班级
            sheet.setColumnWidth(4, 3000);  // 申请类型
            sheet.setColumnWidth(5, 8000);  // 申请原因
            sheet.setColumnWidth(6, 3000);  // 状态
            sheet.setColumnWidth(7, 8000);  // 教师审核意见
            sheet.setColumnWidth(8, 6000);  // 教师审核时间
            sheet.setColumnWidth(9, 8000);  // 管理员审核意见
            sheet.setColumnWidth(10, 6000); // 管理员审核时间

            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // 设置标题
            for (int i = 0; i < columnHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (TestExemption exemption : exemptions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(exemption.getStudentNumber());
                row.createCell(2).setCellValue(exemption.getStudentName());
                row.createCell(3).setCellValue(exemption.getClassName());
                row.createCell(4).setCellValue("EXEMPTION".equals(exemption.getType()) ? "免测" : "重测");
                row.createCell(5).setCellValue(exemption.getReason());
                row.createCell(6).setCellValue(getStatusText(exemption.getStatus()));
                row.createCell(7).setCellValue(exemption.getTeacherReviewComment());
                row.createCell(8).setCellValue(formatDateTime(exemption.getTeacherReviewTime()));
                row.createCell(9).setCellValue(exemption.getAdminReviewComment());
                row.createCell(10).setCellValue(formatDateTime(exemption.getAdminReviewTime()));
            }

            // 导出文件
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            // 设置响应头
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String fileName = String.format("免测重测申请记录_%s.xlsx", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            responseHeaders.setContentDispositionFormData("attachment", 
                new String(fileName.getBytes("UTF-8"), "ISO-8859-1"));

            return ResponseEntity
                .ok()
                .headers(responseHeaders)
                .body(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("导出失败", e);
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    @GetMapping("/test-records/export")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportTestRecords(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Long sportsItemId,
            @RequestParam(required = false) String keyword) {
        log.info("开始导出学生成绩记录");
        log.debug("导出参数: className={}, sportsItemId={}, keyword={}", 
            className, sportsItemId, keyword);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            // 查询数据
            List<TestRecord> records = testRecordRepository.findByFiltersForExport(
                className, sportsItemId, keyword);
            
            if (records.isEmpty()) {
                throw new RuntimeException("没有找到符合条件的记录");
            }
            
            log.info("查询到 {} 条记录准备导出", records.size());

            Sheet sheet = workbook.createSheet("学生成绩记录");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] columnHeaders = {
                "序号", "学号", "学生姓名", "班级", "测试项目", "成绩", "单位", 
                "状态", "审核意见", "审核时间", "创建时间", "更新时间"
            };
            
            // 设置列宽
            int[] columnWidths = {2500, 4000, 4000, 4000, 4000, 3000, 2500, 3000, 8000, 6000, 6000, 6000};
            for (int i = 0; i < columnWidths.length; i++) {
                sheet.setColumnWidth(i, columnWidths[i]);
            }

            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // 设置标题
            for (int i = 0; i < columnHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建数据样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);

            // 填充数据
            int rowNum = 1;
            for (TestRecord record : records) {
                Row row = sheet.createRow(rowNum++);
                
                // 使用数组来简化单元格赋值
                Object[] rowData = {
                    rowNum - 1,
                    record.getStudentNumber(),
                    record.getStudentName(),
                    record.getClassName(),
                    record.getSportsItem() != null ? record.getSportsItem().getName() : "",
                    record.getScore(),
                    record.getSportsItem() != null ? record.getSportsItem().getUnit() : "",
                    getStatusText(record.getStatus()),
                    record.getReviewComment(),
                    formatDateTime(record.getReviewTime()),
                    formatDateTime(record.getCreatedAt()),
                    formatDateTime(record.getUpdatedAt())
                };
                
                // 填充每个单元格
                for (int i = 0; i < rowData.length; i++) {
                    Cell cell = row.createCell(i);
                    if (rowData[i] != null) {
                        if (rowData[i] instanceof Number) {
                            cell.setCellValue(((Number) rowData[i]).doubleValue());
                        } else {
                            cell.setCellValue(rowData[i].toString());
                        }
                    }
                    cell.setCellStyle(dataStyle);
                }
            }

            // 写入文件
            workbook.write(outputStream);
            byte[] content = outputStream.toByteArray();

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            String fileName = String.format("学生成绩记录_%s.xlsx", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            headers.setContentDispositionFormData("attachment", 
                new String(fileName.getBytes("UTF-8"), "ISO-8859-1"));

            log.info("导出成功，文件名: {}", fileName);
            return ResponseEntity
                .ok()
                .headers(headers)
                .body(content);

        } catch (Exception e) {
            log.error("导出失败", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/statistics/class")
    public Result getClassStatistics(
        @RequestParam(required = false) String className,
        @RequestParam(required = false) Long sportsItemId,
        @RequestParam(required = false) String status
    ) {
        try {
            log.info("开始获取班级统计数据: className={}, sportsItemId={}, status={}", 
                className, sportsItemId, status);
            
            // 先检查数据库中是否有记录
            long totalRecords = testRecordRepository.count();
            long approvedRecords = testRecordRepository.countByStatus("APPROVED");
            log.info("数据库中总记录数: {}, 已审核记录数: {}", totalRecords, approvedRecords);
            
            // 使用 repository 层的方法来获取统计数据
            List<Object[]> rawStats = testRecordRepository.getClassStatistics(
                className, 
                sportsItemId,
                status == null ? "APPROVED" : status  // 默认统计已审核通过的记录
            );
            log.info("获取到原始统计数据: {} 条记录", rawStats.size());
            
            // 打印每一行数据
            rawStats.forEach(row -> {
                log.debug("统计数据行: className={}, totalCount={}, avgScore={}, excellentCount={}, passCount={}", 
                    row[0], row[1], row[2], row[3], row[4]);
            });
            
            // 手动转换数据
            List<ClassStatisticsDTO> stats = rawStats.stream()
                .map(row -> {
                    ClassStatisticsDTO dto = new ClassStatisticsDTO(
                        (String) row[0],                  // className
                        ((Number) row[1]).longValue(),    // totalCount
                        ((Number) row[2]).doubleValue(),  // averageScore
                        ((Number) row[3]).longValue(),    // excellentCount
                        ((Number) row[4]).longValue()     // passCount
                    );
                    log.debug("转换后的DTO: {}", dto);
                    return dto;
                })
                .collect(Collectors.toList());
            
            log.info("返回 {} 条统计记录", stats.size());
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("获取班级统计数据失败", e);
            return Result.error("获取班级统计数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/student/{studentNumber}")
    public Result getStudentStatistics(@PathVariable String studentNumber) {
        try {
            List<TestRecord> records = testRecordRepository.findStudentRecords(studentNumber);
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取学生统计数据失败", e);
            return Result.error("获取学生统计数据失败: " + e.getMessage());
        }
    }

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