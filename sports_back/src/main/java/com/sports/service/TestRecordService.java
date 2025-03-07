package com.sports.service;

import com.sports.entity.TestRecord;
import com.sports.entity.User;
import com.sports.entity.SportsItem;
import com.sports.repository.TestRecordRepository;
import com.sports.repository.UserRepository;
import com.sports.repository.SportsItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.JoinType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class TestRecordService {
    @Autowired
    private TestRecordRepository testRecordRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SportsItemRepository sportsItemRepository;
    
    public Page<TestRecord> getRecordList(String status, Long teacherId, 
            Long sportsItemId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        try {
            log.info("Fetching records with params: status={}, teacherId={}, sportsItemId={}, startDate={}, endDate={}",
                    status, teacherId, sportsItemId, startDate, endDate);
                    
            return testRecordRepository.findAll((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                // 添加日志以跟踪查询条件的构建
                log.debug("Building query predicates...");
                
                // 使用 EntityGraph 来加载关联实体
                if (query.getResultType() != Long.class) {
                    root.fetch("student", JoinType.LEFT);
                    root.fetch("teacher", JoinType.LEFT);
                    root.fetch("sportsItem", JoinType.LEFT);
                    root.fetch("reviewer", JoinType.LEFT);
                    query.distinct(true);
                }
                
                if (status != null && !status.isEmpty()) {
                    log.debug("Adding status predicate: {}", status);
                    predicates.add(cb.equal(root.get("status"), status));
                }
                
                if (teacherId != null) {
                    log.debug("Adding teacherId predicate: {}", teacherId);
                    predicates.add(cb.equal(root.get("teacher").get("id"), teacherId));
                }
                
                if (sportsItemId != null) {
                    log.debug("Adding sportsItemId predicate: {}", sportsItemId);
                    predicates.add(cb.equal(root.get("sportsItem").get("id"), sportsItemId));
                }
                
                if (startDate != null) {
                    log.debug("Adding startDate predicate: {}", startDate);
                    predicates.add(cb.greaterThanOrEqualTo(
                        root.get("testTime"), startDate.atStartOfDay()));
                }
                
                if (endDate != null) {
                    log.debug("Adding endDate predicate: {}", endDate);
                    predicates.add(cb.lessThanOrEqualTo(
                        root.get("testTime"), endDate.plusDays(1).atStartOfDay()));
                }
                
                log.debug("Final predicates count: {}", predicates.size());
                return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
            }, pageable);
            
        } catch (Exception e) {
            log.error("Error in getRecordList: ", e);
            throw new RuntimeException("获取记录列表失败: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public TestRecord reviewRecord(Long id, String status, String comment, Long reviewerId) {
        log.info("Reviewing record: id={}, status={}, comment={}, reviewerId={}", 
                id, status, comment, reviewerId);
        
        TestRecord record = testRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("测试记录不存在"));
            
        // 验证状态值
        if (!Arrays.asList(
                TestRecord.STATUS_APPROVED, 
                TestRecord.STATUS_REJECTED
            ).contains(status)) {
            throw new RuntimeException("无效的审核状态");
        }
        
        // 更新审核信息
        record.setStatus(status);
        record.setReviewComment(comment);
        record.setReviewTime(LocalDateTime.now());
        record.setReviewer(new User(reviewerId));
        record.setUpdatedAt(LocalDateTime.now());
        
        log.info("Saving reviewed record: {}", record);
        return testRecordRepository.save(record);
    }

    @Transactional
    public TestRecord updateRecord(TestRecord record) {
        log.info("Updating record: {}", record);
        
        TestRecord existing = testRecordRepository.findById(record.getId())
            .orElseThrow(() -> new RuntimeException("测试记录不存在"));
        
        // 更新基本信息
        existing.setScore(record.getScore());
        existing.setTestTime(record.getTestTime());
        existing.setRemark(record.getRemark());
        
        // 更新关联实体
        if (record.getStudent() != null && record.getStudent().getId() != null) {
            existing.setStudent(new User(record.getStudent().getId()));
        }
        if (record.getTeacher() != null && record.getTeacher().getId() != null) {
            existing.setTeacher(new User(record.getTeacher().getId()));
        }
        if (record.getSportsItem() != null && record.getSportsItem().getId() != null) {
            existing.setSportsItem(new SportsItem(record.getSportsItem().getId()));
        }
        
        // 更新时间
        existing.setUpdatedAt(LocalDateTime.now());
        
        log.info("Saving updated record: {}", existing);
        return testRecordRepository.save(existing);
    }

    public TestRecord save(TestRecord record) {
        try {
            log.info("Saving test record: {}", record);
            
            // 验证关联实体是否存在
            if (!userRepository.existsById(record.getStudent().getId())) {
                throw new RuntimeException("学生不存在");
            }
            if (!userRepository.existsById(record.getTeacher().getId())) {
                throw new RuntimeException("教师不存在");
            }
            if (!sportsItemRepository.existsById(record.getSportsItem().getId())) {
                throw new RuntimeException("测试项目不存在");
            }
            
            return testRecordRepository.save(record);
        } catch (Exception e) {
            log.error("Error saving test record:", e);
            throw new RuntimeException("保存测试记录失败: " + e.getMessage());
        }
    }
} 