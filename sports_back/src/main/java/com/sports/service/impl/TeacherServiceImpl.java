package com.sports.service.impl;

import com.sports.entity.TestRecord;
import com.sports.repository.TestRecordRepository;
import com.sports.repository.UserRepository;
import com.sports.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TestRecordRepository testRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<String> getTeacherClasses(Long teacherId) {
        // TODO: 根据教师ID获取其负责的班级列表
        List<String> classes = new ArrayList<>();
        classes.add("计科1班");
        classes.add("计科2班");
        classes.add("软工1班");
        return classes;
    }

    @Override
    public Page<TestRecord> getTestRecords(Long teacherId, Long sportsItemId, 
                                         String className, String studentNumber, 
                                         Pageable pageable) {
        Specification<TestRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (sportsItemId != null) {
                predicates.add(cb.equal(root.get("sportsItemId"), sportsItemId));
            }
            
            if (className != null && !className.isEmpty()) {
                predicates.add(cb.equal(root.get("student").get("className"), className));
            }
            
            if (studentNumber != null && !studentNumber.isEmpty()) {
                predicates.add(cb.equal(root.get("studentNumber"), studentNumber));
            }
            
            // TODO: 根据教师ID筛选其负责的班级的记录
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return testRecordRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public TestRecord addTestRecord(Long teacherId, TestRecord record) {
        // TODO: 验证学生是否属于该教师负责的班级
        
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        return testRecordRepository.save(record);
    }

    @Override
    @Transactional
    public TestRecord updateTestRecord(Long teacherId, TestRecord record) {
        TestRecord existing = testRecordRepository.findById(record.getId())
            .orElseThrow(() -> new RuntimeException("记录不存在"));
            
        // TODO: 验证记录是否属于该教师负责的班级
        
        existing.setScore(record.getScore());
        existing.setClassName(record.getClassName());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return testRecordRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteTestRecord(Long teacherId, Long recordId) {
        TestRecord record = testRecordRepository.findById(recordId)
            .orElseThrow(() -> new RuntimeException("记录不存在"));
            
        // TODO: 验证记录是否属于该教师负责的班级
        
        testRecordRepository.delete(record);
    }
} 