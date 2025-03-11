package com.sports.service.impl;

import com.sports.entity.TestRecord;
import com.sports.entity.SportsItem;
import com.sports.repository.TestRecordRepository;
import com.sports.repository.SportsItemRepository;
import com.sports.service.TestRecordService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
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
    @Transactional
    public TestRecord save(TestRecord record) {
        // 数据完整性检查
        if (record.getStudentId() == null || record.getSportsItemId() == null || 
            record.getTeacherId() == null || record.getScore() == null || 
            record.getTestTime() == null) {
            throw new RuntimeException("记录数据不完整");
        }

        // 设置初始状态
        record.setStatus("PENDING");
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        // 检查成绩是否异常
        boolean isAbnormal = checkAbnormalScore(record);
        if (isAbnormal) {
            String reason = getAbnormalReason(record);
            // 记录异常原因，但仍然保存
            record.setReviewComment("系统自动标记：" + reason);
        }

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
        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=test_records.xlsx");

        // TODO: 实现数据导出到Excel
        // 1. 查询符合条件的数据
        // 2. 创建Excel工作簿
        // 3. 写入数据
        // 4. 输出到响应流
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