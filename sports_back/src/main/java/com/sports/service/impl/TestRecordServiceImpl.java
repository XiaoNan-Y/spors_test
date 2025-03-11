package com.sports.service.impl;

import com.sports.entity.TestRecord;
import com.sports.entity.SportsItem;
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

@Service
@Transactional
public class TestRecordServiceImpl implements TestRecordService {

    @Autowired
    private TestRecordRepository testRecordRepository;

    @Autowired
    private SportsItemRepository sportsItemRepository;

    @Override
    public Page<TestRecord> getRecordList(String status, Long teacherId, Long sportsItemId,
                                        LocalDate startDate, LocalDate endDate, Pageable pageable) {
        try {
            Specification<TestRecord> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                if (status != null && !status.isEmpty()) {
                    predicates.add(cb.equal(root.get("status"), status));
                }
                
                if (teacherId != null) {
                    predicates.add(cb.equal(root.get("teacherId"), teacherId));
                }
                
                if (sportsItemId != null) {
                    predicates.add(cb.equal(root.get("sportsItemId"), sportsItemId));
                }
                
                if (startDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("testTime"), startDate.atStartOfDay()));
                }
                
                if (endDate != null) {
                    predicates.add(cb.lessThan(root.get("testTime"), endDate.plusDays(1).atStartOfDay()));
                }

                // 添加关联查询
                if (query.getResultType() == TestRecord.class) {
                    root.fetch("student", JoinType.LEFT);
                    root.fetch("teacher", JoinType.LEFT);
                    root.fetch("sportsItem", JoinType.LEFT);
                }
                
                return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
            };

            // 添加调试日志
            System.out.println("Executing query with spec: " + spec);
            
            return testRecordRepository.findAll(spec, pageable);
        } catch (Exception e) {
            e.printStackTrace(); // 添加错误堆栈跟踪
            throw new RuntimeException("获取记录列表失败: " + e.getMessage());
        }
    }

    @Override
    public TestRecord save(TestRecord record) {
        // 添加验证
        if (record.getStudentId() == null || record.getSportsItemId() == null || 
            record.getTeacherId() == null || record.getScore() == null) {
            throw new IllegalArgumentException("必填字段不能为空");
        }
        
        // 保存记录
        return testRecordRepository.save(record);
    }

    @Override
    @Transactional
    public TestRecord updateRecord(TestRecord record) {
        TestRecord existing = testRecordRepository.findById(record.getId())
            .orElseThrow(() -> new RuntimeException("记录不存在"));
            
        existing.setScore(record.getScore());
        existing.setTestTime(record.getTestTime());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return testRecordRepository.save(existing);
    }

    @Override
    @Transactional
    public TestRecord reviewRecord(Long id, String status, String reviewComment, Long reviewerId) {
        TestRecord record = testRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("记录不存在"));

        // 检查是否为待审核状态
        if (!"PENDING".equals(record.getStatus()) && !"REJECTED".equals(record.getStatus())) {
            throw new RuntimeException("只能审核待审核或已驳回状态的记录");
        }

        // 检查成绩是否异常
        boolean isAbnormal = checkAbnormalScore(record);
        if (isAbnormal && "APPROVED".equals(status)) {
            throw new RuntimeException("异常成绩不能直接通过审核：" + getAbnormalReason(record));
        }

        // 更新审核信息
        record.setStatus(status);
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
    public void exportToExcel(HttpServletResponse response, String status, Long teacherId, Long sportsItemId) throws Exception {
        // 查询数据
        Specification<TestRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (teacherId != null) {
                predicates.add(cb.equal(root.get("teacherId"), teacherId));
            }
            if (sportsItemId != null) {
                predicates.add(cb.equal(root.get("sportsItemId"), sportsItemId));
            }
            
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
        
        List<TestRecord> records = testRecordRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "testTime"));

        // 创建工作簿和工作表
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("测试记录");

        // 设置列宽
        sheet.setColumnWidth(0, 15 * 256);  // 测试时间
        sheet.setColumnWidth(1, 12 * 256);  // 学生姓名
        sheet.setColumnWidth(2, 12 * 256);  // 学号
        sheet.setColumnWidth(3, 15 * 256);  // 测试项目
        sheet.setColumnWidth(4, 10 * 256);  // 成绩
        sheet.setColumnWidth(5, 10 * 256);  // 状态
        sheet.setColumnWidth(6, 12 * 256);  // 教师姓名
        sheet.setColumnWidth(7, 15 * 256);  // 审核时间
        sheet.setColumnWidth(8, 20 * 256);  // 审核意见

        // 创建标题行样式
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);

        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // 创建标题行
        String[] headers = {"测试时间", "学生姓名", "学号", "测试项目", "成绩", "状态", "教师姓名", "审核时间", "审核意见"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 创建数据行样式
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);

        // 写入数据
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int rowNum = 1;
        for (TestRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            
            // 测试时间
            row.createCell(0).setCellValue(record.getTestTime().format(formatter));
            // 学生姓名
            row.createCell(1).setCellValue(record.getStudent() != null ? record.getStudent().getRealName() : "");
            // 学号
            row.createCell(2).setCellValue(record.getStudent() != null ? record.getStudent().getUsername() : "");
            // 测试项目
            row.createCell(3).setCellValue(record.getSportsItem() != null ? record.getSportsItem().getName() : "");
            // 成绩
            row.createCell(4).setCellValue(record.getScore() != null ? record.getScore() : 0.0);
            // 状态
            row.createCell(5).setCellValue(getStatusText(record.getStatus()));
            // 教师姓名
            row.createCell(6).setCellValue(record.getTeacher() != null ? record.getTeacher().getRealName() : "");
            // 审核时间
            row.createCell(7).setCellValue(record.getReviewTime() != null ? record.getReviewTime().format(formatter) : "");
            // 审核意见
            row.createCell(8).setCellValue(record.getReviewComment() != null ? record.getReviewComment() : "");

            // 应用样式
            for (int i = 0; i < headers.length; i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    cell.setCellStyle(dataStyle);
                }
            }
        }

        // 写入响应
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=test_records.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // 添加辅助方法：获取状态的中文描述
    private String getStatusText(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "待审核";
            case "APPROVED": return "已通过";
            case "REJECTED": return "已驳回";
            default: return status;
        }
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
            if (record.getStudentId() == null || record.getSportsItemId() == null || 
                record.getTeacherId() == null || record.getScore() == null || 
                record.getTestTime() == null) {
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
    public List<TestRecord> getHistoryRecords(Long studentId, Long sportsItemId, Long excludeId) {
        try {
            Specification<TestRecord> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                predicates.add(cb.equal(root.get("studentId"), studentId));
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
} 