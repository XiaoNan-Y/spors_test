package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.SportsItem;
import com.sports.entity.TestRecord;
import com.sports.entity.ExemptionApplication;
import com.sports.entity.User;
import com.sports.repository.TestRecordRepository;
import com.sports.repository.ExemptionApplicationRepository;
import com.sports.repository.UserRepository;
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
import com.sports.dto.TeacherDashboardDTO;
import com.sports.dto.ExemptionApplicationDTO;
import com.sports.service.ExemptionService;

import java.net.URLEncoder;
import java.util.Base64;

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
    private final UserRepository userRepository;
    private final ExemptionApplicationRepository exemptionApplicationRepository;

    @Autowired
    private ExemptionService exemptionService;

    public TeacherController(TestRecordRepository testRecordRepository, ExemptionApplicationRepository exemptionApplicationRepository, UserRepository userRepository) {
        this.testRecordRepository = testRecordRepository;
        this.exemptionApplicationRepository = exemptionApplicationRepository;
        this.userRepository = userRepository;
    }

    // 获取教师仪表盘统计数据
    @GetMapping("/dashboard-stats")
    public Result getDashboardStats() {
        try {
            TeacherDashboardDTO stats = new TeacherDashboardDTO();
            
            // 获取待审核免测申请数
            Integer pendingReviews = exemptionApplicationRepository.countPendingApplications();
            stats.setPendingReviews(pendingReviews);
            
            // 获取班级数量
            List<String> classes = userRepository.findDistinctClassNames();
            stats.setClassCount(classes.size());
            
            // 计算测试完成率和参测率
            long totalStudents = userRepository.countByUserType(User.TYPE_STUDENT);
            long testedStudents = testRecordRepository.countDistinctStudentNumber();
            
            // 测试完成率
            double completionRate = totalStudents > 0 ? (testedStudents * 100.0) / totalStudents : 0;
            stats.setTestCompletion((int) Math.round(completionRate));
            
            // 参测率（保留一位小数）
            stats.setTestParticipationRate(Math.round(completionRate * 10.0) / 10.0);
            
            // 获取本月测试人数
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            long monthlyTestCount = testRecordRepository.countByCreatedAtAfter(startOfMonth);
            stats.setMonthlyTests((int) monthlyTestCount);
            
            // 设置学生总数
            stats.setStudentCount((int) totalStudents);
            
            // 计算活跃班级数（有测试记录的班级）
            long activeClassCount = testRecordRepository.countDistinctClassName();
            stats.setActiveClasses((int) activeClassCount);
            
            // 计算审核趋势（与上月相比的百分比变化）
            LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
            Integer lastMonthReviews = exemptionApplicationRepository.countReviewedSince(startOfLastMonth);
            Integer thisMonthReviews = exemptionApplicationRepository.countReviewedSince(startOfMonth);
            
            // 计算环比增长率
            int reviewTrend;
            if (lastMonthReviews > 0) {
                reviewTrend = (int) ((thisMonthReviews - lastMonthReviews) * 100 / lastMonthReviews);
            } else if (thisMonthReviews > 0) {
                reviewTrend = 100; // 上月为0，本月有数据，增长100%
            } else {
                reviewTrend = 0; // 上月和本月都为0，增长0%
            }
            stats.setReviewTrend(reviewTrend);

            log.info("Dashboard统计数据: {}", stats);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败", e);
            return Result.error("获取仪表盘统计数据失败：" + e.getMessage());
        }
    }

    // 获取体育项目列表
    @GetMapping("/sports-items")
    public Result getActiveSportsItems() {
        try {
            List<SportsItem> items = sportsItemService.findActiveItems();
            return Result.success(items);
        } catch (Exception e) {
            log.error("获取体育项目列表失败", e);
            return Result.error("获取体育项目列表失败：" + e.getMessage());
        }
    }

    // 获取教师负责的班级列表
    @GetMapping("/classes")
    public Result getClassList() {
        try {
            List<String> classes = userRepository.findDistinctClassNames();
            return Result.success(classes);
        } catch (Exception e) {
            log.error("获取班级列表失败", e);
            return Result.error("获取班级列表失败: " + e.getMessage());
        }
    }

    // 获取成绩记录列表
    @GetMapping("/test-records")
    public Result getTestRecords(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Long sportsItemId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String studentNumber,  // 添加这个参数
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<TestRecord> records = testRecordRepository.findByFiltersForTeacher(
                className,
                sportsItemId,
                status,
                studentNumber,  // 添加这个参数
                pageRequest
            );
            return Result.success(records);
        } catch (Exception e) {
            log.error("Failed to get test records", e);
            return Result.error("获取记录列表失败: " + e.getMessage());
        }
    }

    // 录入成绩
    @PostMapping("/test-records")
    public Result addTestRecord(@RequestBody TestRecord record) {
        try {
            log.info("开始录入成绩");
            log.debug("录入数据: {}", record);
            
            // 从学号查询学生信息
            User student = userRepository.findOne((root, query, cb) -> {
                return cb.and(
                    cb.equal(root.get("studentNumber"), record.getStudentNumber()),
                    cb.equal(root.get("userType"), User.TYPE_STUDENT)
                );
            }).orElseThrow(() -> new RuntimeException("未找到学生信息"));
            
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
            record.setUpdateTime(now);
            
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
    public Result updateTestRecord(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            log.info("开始更新成绩记录 - id: {}, updates: {}", id, updates);
            
            // 查找记录
            TestRecord record = testRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在，ID: " + id));
            
            log.debug("找到待更新记录: {}", record);
            
            // 更新成绩
            if (updates.containsKey("score")) {
                Double newScore = Double.parseDouble(updates.get("score").toString());
                log.info("更新成绩 {} -> {}", record.getScore(), newScore);
                record.setScore(newScore);
                
                // 如果成绩为0，自动将状态设置为未测试
                if (newScore == 0) {
                    log.info("成绩为0，状态自动设置为未测试");
                    record.setStatus("PENDING");
                } else if (updates.containsKey("status")) {
                    String newStatus = updates.get("status").toString();
                    log.info("更新状态 {} -> {}", record.getStatus(), newStatus);
                    record.setStatus(newStatus);
                }
            }
            
            // 更新时间
            record.setUpdateTime(LocalDateTime.now());
            
            // 保存更新
            TestRecord savedRecord = testRecordRepository.save(record);
            log.info("成绩记录更新成功 - id: {}", savedRecord.getId());
            
            // 构建返回数据
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedRecord.getId());
            response.put("score", savedRecord.getScore());
            response.put("status", savedRecord.getStatus());
            response.put("updateTime", savedRecord.getUpdateTime());
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("更新成绩记录失败 - id: " + id, e);
            return Result.error("更新失败：" + e.getMessage());
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
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        try {
            log.info("开始查询学生成绩记录");
            
            // 处理空值
            className = (className != null && className.trim().isEmpty()) ? null : className;
            status = (status != null && status.trim().isEmpty()) ? null : status;
            String studentNumber = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;

            log.debug("处理后的查询参数: className={}, sportsItemId={}, status={}, studentNumber={}, page={}, size={}", 
                className, sportsItemId, status, studentNumber, page, size);

            PageRequest pageRequest = PageRequest.of(page, size);
            Page<TestRecord> records = testRecordRepository.findByFiltersForTeacher(
                className,
                sportsItemId,
                status,
                studentNumber,
                pageRequest
            );

            log.info("查询结果: 总记录数={}, 当前页记录数={}", 
                records.getTotalElements(), records.getContent().size());

            if (records.isEmpty()) {
                log.warn("未找到符合条件的记录");
            } else {
                log.debug("第一条记录信息: studentName={}, className={}, score={}", 
                    records.getContent().get(0).getStudentName(),
                    records.getContent().get(0).getClassName(),
                    records.getContent().get(0).getScore());
            }

            return Result.success(records);
        } catch (Exception e) {
            log.error("获取学生成绩记录失败", e);
            return Result.error("获取学生成绩记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取重测申请列表
     */
    @GetMapping("/retest-applications")
    public Result getRetestApplications(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Getting retest applications - keyword: {}, page: {}, size: {}", 
                    keyword, page, size);
            Page<ExemptionApplication> applications = exemptionService
                .getTeacherRetestApplications(keyword, PageRequest.of(page, size));
            
            // 添加调试日志
            log.debug("Found {} applications", applications.getTotalElements());
            applications.getContent().forEach(app -> {
                log.debug("Application: id={}, studentName={}, status={}", 
                    app.getId(), app.getStudentName(), app.getStatus());
            });
            
            return Result.success(applications);
        } catch (Exception e) {
            log.error("获取重测申请列表失败", e);
            return Result.error("获取重测申请列表失败：" + e.getMessage());
        }
    }

    /**
     * 教师审核重测申请
     */
    @PostMapping("/retest-applications/{id}/review")
    public Result reviewRetestApplication(
        @PathVariable Long id,
        @RequestBody Map<String, String> params) {
        try {
            log.info("Reviewing retest application - id: {}, params: {}", id, params);
            
            String status = params.get("status");
            String comment = params.get("comment");
            Long reviewerId = Long.valueOf(params.get("reviewerId"));
            
            ExemptionApplication reviewed = exemptionService
                .teacherReview(id, status, comment, reviewerId);
                
            log.info("Review completed - status: {}", reviewed.getStatus());
            return Result.success(reviewed);
        } catch (Exception e) {
            log.error("审核失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    @GetMapping("/exemptions/class-list")
    public Result getExemptionsClassList() {
        try {
            List<String> classList = exemptionService.getDistinctClassNames();
            return Result.success(classList);
        } catch (Exception e) {
            log.error("Failed to get class list", e);
            return Result.error("获取班级列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/test-records/export")
    public ResponseEntity<?> exportTestRecords(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Long sportsItemId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String studentNumber) {
        log.info("开始导出学生成绩记录");
        
        Workbook workbook = null;
        ByteArrayOutputStream outputStream = null;

        try {
            // 查询数据
            List<TestRecord> records = testRecordRepository.findByFiltersForExport(
                className, 
                sportsItemId, 
                status,
                studentNumber
            );

            log.info("查询到 {} 条记录", records.size());
            if (records.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "没有找到符合条件的记录");
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
            }

            workbook = new XSSFWorkbook();
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
                    formatDateTime(record.getUpdateTime())
                };

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

            outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] content = outputStream.toByteArray();

            // 文件名处理
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String statusText = "";
            if (status != null && !status.isEmpty()) {
                switch (status) {
                    case "APPROVED":
                        statusText = "已测试";
                        break;
                    case "PENDING":
                        statusText = "未测试";
                        break;
                    case "EXEMPTED":
                        statusText = "免测";
                        break;
                    default:
                        statusText = "全部";
                }
            } else {
                statusText = "全部";
            }
            
            String fileName = String.format("学生成绩记录_%s_%s.xlsx", statusText, dateStr);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            
            // 使用 Base64 编码
            String encodedFileName = Base64.getEncoder().encodeToString(fileName.getBytes("UTF-8"));
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=" + encodedFileName + ".xlsx");
            headers.setContentLength(content.length);

            return ResponseEntity.ok()
                .headers(headers)
                .body(content);

        } catch (Exception e) {
            log.error("导出失败: ", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "导出失败：" + e.getMessage());
            return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                log.error("关闭资源失败: ", e);
            }
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