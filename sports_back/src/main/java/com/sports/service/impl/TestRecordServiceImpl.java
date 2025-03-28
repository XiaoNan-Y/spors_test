package com.sports.service.impl;

import com.sports.entity.TestRecord;
import com.sports.entity.SportsItem;
import com.sports.dto.ClassStatisticsDTO;
import com.sports.repository.TestRecordRepository;
import com.sports.repository.SportsItemRepository;
import com.sports.service.TestRecordService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.JoinType;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestRecordServiceImpl implements TestRecordService {

    private static final Logger log = LoggerFactory.getLogger(TestRecordServiceImpl.class);

    @Autowired
    private TestRecordRepository testRecordRepository;

    @Autowired
    private SportsItemRepository sportsItemRepository;

    @Override
    public Page<TestRecord> getRecordList(String status, Long sportsItemId, Pageable pageable) {
        try {
            return testRecordRepository.findAllWithStudent(status, sportsItemId, pageable);
        } catch (Exception e) {
            log.error("获取记录列表失败", e);
            throw new RuntimeException("获取记录列表失败: " + e.getMessage());
        }
    }

    @Override
    public TestRecord save(TestRecord record) {
        // 设置测试状态 - 教师端关注
        if (record.getId() == null) {
            // 新记录，根据是否有成绩设置状态
            if (record.getScore() != null && record.getScore() > 0) {
                record.setStatus("TESTED");  // 已测试
            } else {
                record.setStatus("NOT_TESTED");  // 未测试
            }
            
            // 新记录的审核状态始终为待审核 - 管理员端关注
            record.setReviewStatus("PENDING");
        }
        
        // 设置创建/更新时间
        LocalDateTime now = LocalDateTime.now();
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(now);
        }
        record.setUpdatedAt(now);
        
        return testRecordRepository.save(record);
    }

    @Override
    @Transactional
    public TestRecord updateRecord(TestRecord record) {
        TestRecord existing = testRecordRepository.findById(record.getId())
            .orElseThrow(() -> new RuntimeException("记录不存在"));
            
        existing.setScore(record.getScore());
        existing.setClassName(record.getClassName());
        
        // 更新测试状态
        if (record.getScore() != null && record.getScore() > 0) {
            existing.setStatus("TESTED");  // 已测试
        }
        
        // 更新后重置审核状态为待审核
        existing.setReviewStatus("PENDING");
        existing.setUpdatedAt(LocalDateTime.now());
        
        return testRecordRepository.save(existing);
    }

    @Override
    @Transactional
    public TestRecord reviewRecord(Long id, String reviewStatus, String reviewComment, Long reviewerId) {
        TestRecord record = testRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("记录不存在"));

        // 检查是否为待审核状态
        if (!"PENDING".equals(record.getReviewStatus())) {
            throw new RuntimeException("只能审核待审核状态的记录");
        }

        // 检查成绩是否异常
        boolean isAbnormal = checkAbnormalScore(record);
        if (isAbnormal && "APPROVED".equals(reviewStatus)) {
            throw new RuntimeException("异常成绩不能直接通过审核：" + getAbnormalReason(record));
        }

        // 更新审核信息 - 只修改审核状态，不影响测试状态
        record.setReviewStatus(reviewStatus);
        record.setReviewComment(reviewComment);
        record.setReviewTime(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        return testRecordRepository.save(record);
    }

    @Override
    public boolean checkAbnormalScore(TestRecord record) {
        if (record == null || record.getSportsItemId() == null || record.getScore() == null) {
            return true;
        }

        // 获取体测项目信息
        Optional<SportsItem> sportsItemOpt = sportsItemRepository.findById(record.getSportsItemId());
        if (!sportsItemOpt.isPresent()) {
            return true;
        }
        SportsItem sportsItem = sportsItemOpt.get();

        double score = record.getScore();

        // 根据不同项目类型设置合理范围
        switch (sportsItem.getName()) {
            case "100米跑":
                return score < 10 || score > 20;
            case "1000米跑":
                return score < 180 || score > 600;
            case "立定跳远":
                return score < 1.5 || score > 3.0;
            case "引体向上":
                return score < 0 || score > 30;
            case "仰卧起坐":
                return score < 0 || score > 80;
            default:
                return score < 0;
        }
    }

    @Override
    public String getAbnormalReason(TestRecord record) {
        if (record == null || record.getSportsItemId() == null || record.getScore() == null) {
            return "记录数据不完整";
        }

        // 获取体测项目信息
        Optional<SportsItem> sportsItemOpt = sportsItemRepository.findById(record.getSportsItemId());
        if (!sportsItemOpt.isPresent()) {
            return "体测项目不存在";
        }
        SportsItem sportsItem = sportsItemOpt.get();

        double score = record.getScore();

        switch (sportsItem.getName()) {
            case "100米跑":
                if (score < 10) {
                    return "100米跑成绩异常：成绩小于10秒，可能存在记录错误";
                } else if (score > 20) {
                    return "100米跑成绩异常：成绩大于20秒，可能存在记录错误";
                }
                break;
            case "1000米跑":
                if (score < 180) {
                    return "1000米跑成绩异常：成绩小于3分钟，可能存在记录错误";
                } else if (score > 600) {
                    return "1000米跑成绩异常：成绩大于10分钟，可能存在记录错误";
                }
                break;
            case "立定跳远":
                if (score < 1.5) {
                    return "立定跳远成绩异常：成绩小于1.5米，可能存在记录错误";
                } else if (score > 3.0) {
                    return "立定跳远成绩异常：成绩大于3.0米，可能存在记录错误";
                }
                break;
            case "引体向上":
                if (score < 0) {
                    return "引体向上成绩异常：成绩不能为负数";
                } else if (score > 30) {
                    return "引体向上成绩异常：成绩大于30个，可能存在记录错误";
                }
                break;
            case "仰卧起坐":
                if (score < 0) {
                    return "仰卧起坐成绩异常：成绩不能为负数";
                } else if (score > 80) {
                    return "仰卧起坐成绩异常：成绩大于80个，可能存在记录错误";
                }
                break;
        }
        return null;
    }

    @Override
    public List<TestRecord> importFromExcel(MultipartFile file) throws Exception {
        List<TestRecord> records = new ArrayList<>();
        // TODO: 实现Excel文件解析和数据导入
        // 1. 读取Excel文件
        // 2. 验证数据格式
        // 3. 转换为TestRecord对象
        // 4. 保存到数据库
        return records;
    }

    @Override
    public void exportToExcel(HttpServletResponse response, String status, Long sportsItemId) throws IOException {
        List<TestRecord> records;
        if (status != null && sportsItemId != null) {
            records = testRecordRepository.findByStatusAndSportsItemId(status, sportsItemId);
        } else if (status != null) {
            records = testRecordRepository.findByStatus(status);
        } else if (sportsItemId != null) {
            records = testRecordRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("sportsItemId"), sportsItemId)
            );
        } else {
            records = testRecordRepository.findAll();
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("测试记录");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] columns = {"学号", "姓名", "班级", "测试项目", "成绩", "状态", "审核意见", "创建时间"};
        
        // 设置标题行样式
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        // 写入标题
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // 写入数据
        int rowNum = 1;
        for (TestRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(record.getStudentNumber());
            row.createCell(1).setCellValue(record.getStudent() != null ? record.getStudent().getRealName() : "");
            row.createCell(2).setCellValue(record.getClassName());
            row.createCell(3).setCellValue(record.getSportsItem() != null ? record.getSportsItem().getName() : "");
            row.createCell(4).setCellValue(record.getScore());
            row.createCell(5).setCellValue(record.getStatus());
            row.createCell(6).setCellValue(record.getReviewComment());
            row.createCell(7).setCellValue(
                record.getCreatedAt() != null ? 
                record.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : ""
            );
        }

        // 自动调整列宽
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=test_records.xlsx");

        // 写入响应
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @Override
    public void generateTemplate(HttpServletResponse response) throws Exception {
        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=template.xlsx");

        // TODO: 实现模板生成
        // 1. 创建Excel工作簿
        // 2. 添加表头和说明
        // 3. 输出到响应流
    }

    @Override
    @Transactional
    public List<TestRecord> saveAll(List<TestRecord> records) {
        // 批量保存前进行数据验证
        for (TestRecord record : records) {
            if (record.getStudentNumber() == null || record.getSportsItemId() == null || 
                record.getScore() == null || record.getClassName() == null) {
                throw new RuntimeException("记录数据不完整");
            }

            // 检查成绩是否异常
            boolean isAbnormal = checkAbnormalScore(record);
            if (isAbnormal) {
                String reason = getAbnormalReason(record);
                record.setReviewComment("系统自动标记：" + reason);
            }
        }
        
        return testRecordRepository.saveAll(records);
    }

    @Override
    public List<TestRecord> getHistoryRecords(String studentNumber, Long sportsItemId, Long excludeId) {
        try {
            Specification<TestRecord> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                predicates.add(cb.equal(root.get("studentNumber"), studentNumber));
                predicates.add(cb.equal(root.get("sportsItemId"), sportsItemId));
                
                if (excludeId != null) {
                    predicates.add(cb.notEqual(root.get("id"), excludeId));
                }
                
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            
            return testRecordRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "testTime"));
        } catch (Exception e) {
            throw new RuntimeException("获取历史记录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public TestRecord modifyReview(Long id, String status, String comment, Long reviewerId) {
        TestRecord record = testRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("记录不存在"));

        // 检查成绩是否异常
        boolean isAbnormal = checkAbnormalScore(record);
        if (isAbnormal && "APPROVED".equals(status)) {
            throw new RuntimeException("异常成绩不能直接通过审核：" + getAbnormalReason(record));
        }

        record.setStatus(status);
        record.setReviewComment(comment);
        record.setReviewTime(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        return testRecordRepository.save(record);
    }

    @Override
    public Page<TestRecord> getTestRecords(String className, Long sportsItemId, 
                                         String status, String studentNumber, 
                                         Pageable pageable) {
        try {
            log.info("Getting test records with filters: className={}, sportsItemId={}, status={}, studentNumber={}", 
                     className, sportsItemId, status, studentNumber);
                     
            Page<TestRecord> records = testRecordRepository.findByFiltersWithReviewStatus(
                className,
                sportsItemId,
                status,
                studentNumber,
                pageable
            );
            
            log.debug("Found {} records", records.getTotalElements());
            return records;
        } catch (Exception e) {
            log.error("Error getting test records", e);
            throw new RuntimeException("获取测试记录失败: " + e.getMessage());
        }
    }

    @Override
    public List<ClassStatisticsDTO> getClassStatistics(String className, Long sportsItemId) {
        try {
            log.info("Getting class statistics: className={}, sportsItemId={}", className, sportsItemId);
            
            List<Object[]> rawStats = testRecordRepository.getClassStatistics(className, sportsItemId, "APPROVED");
            
            return rawStats.stream()
                .map(row -> new ClassStatisticsDTO(
                    (String) row[0],                  // className
                    ((Number) row[1]).longValue(),    // totalCount
                    ((Number) row[2]).doubleValue(),  // averageScore
                    ((Number) row[3]).longValue(),    // excellentCount
                    ((Number) row[4]).longValue()     // passCount
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取班级统计数据失败", e);
            throw new RuntimeException("获取班级统计数据失败: " + e.getMessage());
        }
    }
} 