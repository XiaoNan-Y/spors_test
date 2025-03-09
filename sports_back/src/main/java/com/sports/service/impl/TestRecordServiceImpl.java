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

@Slf4j
@Service
public class TestRecordServiceImpl implements TestRecordService {

    @Autowired
    private TestRecordRepository testRecordRepository;

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
        record.setStatus("PENDING");
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
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