package com.sports.service.impl;

import com.sports.entity.TestRecord;
import com.sports.entity.User;
import com.sports.repository.TestRecordRepository;
import com.sports.service.TestRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TestRecordServiceImpl implements TestRecordService {

    @Autowired
    private TestRecordRepository testRecordRepository;

    // 定义各项目的正常成绩范围
    private static final Map<String, double[]> SCORE_RANGES = new HashMap<>();
    static {
        SCORE_RANGES.put("100米跑", new double[]{9.0, 20.0});
        SCORE_RANGES.put("1000米跑", new double[]{180.0, 600.0});
        SCORE_RANGES.put("立定跳远", new double[]{1.5, 3.5});
        SCORE_RANGES.put("引体向上", new double[]{0.0, 30.0});
        SCORE_RANGES.put("仰卧起坐", new double[]{10.0, 80.0});
    }

    @Override
    public boolean checkAbnormalScore(TestRecord record) {
        String itemName = record.getSportsItem().getName();
        double[] range = SCORE_RANGES.get(itemName);
        if (range == null) return false;
        
        double score = record.getScore();
        return score < range[0] || score > range[1];
    }

    @Override
    public String getAbnormalReason(TestRecord record) {
        String itemName = record.getSportsItem().getName();
        double[] range = SCORE_RANGES.get(itemName);
        if (range == null) return null;
        
        double score = record.getScore();
        if (score < range[0]) {
            return String.format("成绩%.2f低于正常范围%.2f-%2f", score, range[0], range[1]);
        } else if (score > range[1]) {
            return String.format("成绩%.2f高于正常范围%.2f-%2f", score, range[0], range[1]);
        }
        return null;
    }

    @Override
    public Page<TestRecord> getRecordList(String status, Long teacherId, Long sportsItemId,
                                        LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return testRecordRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (teacherId != null) {
                predicates.add(cb.equal(root.get("teacher").get("id"), teacherId));
            }

            if (sportsItemId != null) {
                predicates.add(cb.equal(root.get("sportsItem").get("id"), sportsItemId));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("testTime"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("testTime"), endDate.plusDays(1).atStartOfDay()));
            }

            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    @Override
    @Transactional
    public TestRecord save(TestRecord record) {
        // 检查是否存在待审核的记录
        if (testRecordRepository.existsPendingRecord(
                record.getStudent().getId(), 
                record.getSportsItem().getId())) {
            throw new RuntimeException("该学生在此项目上已有待审核的记录");
        }

        // 设置初始状态
        record.setStatus(TestRecord.STATUS_PENDING);
        
        // 检查异常分数
        boolean isAbnormal = checkAbnormalScore(record);
        record.setIsAbnormal(isAbnormal);
        if (isAbnormal) {
            record.setAbnormalReason(getAbnormalReason(record));
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
        existing.setStatus(record.getStatus());
        existing.setReviewComment(record.getReviewComment());
        existing.setUpdatedAt(LocalDateTime.now());

        return testRecordRepository.save(existing);
    }

    @Override
    @Transactional
    public TestRecord reviewRecord(Long id, String status, String reviewComment, Long reviewerId) {
        TestRecord record = testRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("记录不存在"));

        record.setStatus(status);
        record.setReviewComment(reviewComment);
        record.setReviewer(new User(reviewerId));
        record.setReviewTime(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        return testRecordRepository.save(record);
    }
} 