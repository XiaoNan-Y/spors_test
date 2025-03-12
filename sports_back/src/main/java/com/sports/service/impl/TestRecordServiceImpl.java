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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        // 添加验证
        if (record.getStudentNumber() == null || record.getSportsItemId() == null || 
            record.getScore() == null) {
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
    public void exportToExcel(HttpServletResponse response, String status, Long sportsItemId) throws Exception {
        // 查询数据
        Specification<TestRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
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
        sheet.setColumnWidth(6, 20 * 256);  // 审核意见

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"测试时间", "学生姓名", "学号", "测试项目", "成绩", "状态", "审核意见"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 写入数据
        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (TestRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(record.getTestTime().format(formatter));
            row.createCell(1).setCellValue(record.getStudent() != null ? record.getStudent().getRealName() : "");
            row.createCell(2).setCellValue(record.getStudentNumber());
            row.createCell(3).setCellValue(record.getSportsItem() != null ? record.getSportsItem().getName() : "");
            row.createCell(4).setCellValue(record.getScore());
            row.createCell(5).setCellValue(getStatusText(record.getStatus()));
            row.createCell(6).setCellValue(record.getReviewComment() != null ? record.getReviewComment() : "");
        }

        // 写入响应
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=test_records.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
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
                record.getScore() == null || 
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
} 