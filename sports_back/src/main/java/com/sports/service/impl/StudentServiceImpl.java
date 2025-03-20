package com.sports.service.impl;

import com.sports.dto.TestRecordDTO;
import com.sports.entity.TestRecord;
import com.sports.entity.ExemptionApplication;
import com.sports.entity.Notice;
import com.sports.entity.User;
import com.sports.repository.TestRecordRepository;
import com.sports.repository.ExemptionApplicationRepository;
import com.sports.repository.NoticeRepository;
import com.sports.repository.SportsItemRepository;
import com.sports.repository.UserRepository;
import com.sports.service.StudentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);

    @Autowired
    private TestRecordRepository testRecordRepository;

    @Autowired
    private ExemptionApplicationRepository exemptionApplicationRepository;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private SportsItemRepository sportsItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Map<String, Object> getDashboardStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取测试记录统计
        long testedCount = testRecordRepository.countByStudentIdAndStatusNot(userId, "PENDING");
        long pendingCount = testRecordRepository.countByStudentIdAndStatus(userId, "PENDING");
        long passedCount = testRecordRepository.countByStudentIdAndStatus(userId, "APPROVED");
        
        double passRate = testedCount > 0 ? (passedCount * 100.0 / testedCount) : 0;
        
        // 获取免测申请统计
        long pendingApplications = exemptionApplicationRepository.countByStudentIdAndStatus(userId, "PENDING");
        long approvedApplications = exemptionApplicationRepository.countByStudentIdAndStatus(userId, "APPROVED");
        long rejectedApplications = exemptionApplicationRepository.countByStudentIdAndStatus(userId, "REJECTED");
        
        // 获取最新通知
        List<Notice> latestNotices = noticeRepository.findTop5ByStatusOrderByCreateTimeDesc(1);
        
        // 获取最近成绩
        List<TestRecord> recentRecords = testRecordRepository.findTop5ByStudentIdOrderByCreatedAtDesc(userId);
        
        // 组装数据
        stats.put("testedCount", testedCount);
        stats.put("pendingCount", pendingCount);
        stats.put("passRate", Math.round(passRate * 10) / 10.0);
        stats.put("pendingApplications", pendingApplications);
        stats.put("approvedApplications", approvedApplications);
        stats.put("rejectedApplications", rejectedApplications);
        stats.put("latestNotices", latestNotices);
        stats.put("recentRecords", recentRecords);
        
        return stats;
    }

    @Override
    public Page<TestRecord> getTestRecords(Long userId, Long sportsItemId, Pageable pageable) {
        if (sportsItemId != null) {
            return testRecordRepository.findByStudentIdAndSportsItemId(userId, sportsItemId, pageable);
        }
        return testRecordRepository.findByStudentId(userId, pageable);
    }

    @Override
    @Transactional
    public ExemptionApplication submitExemption(ExemptionApplication application) {
        application.setStatus("PENDING");
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        return exemptionApplicationRepository.save(application);
    }

    @Override
    public Page<ExemptionApplication> getExemptions(Long userId, Pageable pageable) {
        return exemptionApplicationRepository.findByStudentId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TestRecordDTO> getStudentTestRecords(Long userId, String status, Pageable pageable) {
        try {
            log.info("Getting student test records - userId: {}, status: {}, page: {}", 
                    userId, status, pageable);
            
            // 先获取学生信息
            User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            String studentNumber = student.getStudentNumber();
            if (studentNumber == null) {
                throw new RuntimeException("学生学号不存在");
            }
            
            // 根据学号和状态查询测试记录
            Page<TestRecord> records = testRecordRepository.findByStudentNumberAndStatus(
                studentNumber, 
                status, 
                pageable
            );
            
            log.info("Found {} records for student number {}", records.getTotalElements(), studentNumber);
            
            // 将 TestRecord 转换为 TestRecordDTO
            return records.map(record -> {
                TestRecordDTO dto = new TestRecordDTO();
                BeanUtils.copyProperties(record, dto);
                
                // 确保设置创建时间
                if (record.getCreatedAt() != null) {
                    dto.setCreatedAt(record.getCreatedAt());
                }
                
                // 设置体育项目名称
                if (record.getSportsItem() != null) {
                    dto.setSportsItemName(record.getSportsItem().getName());
                }
                
                // 计算等级
                dto.setGrade(calculateGrade(record.getScore()));
                
                return dto;
            });
        } catch (Exception e) {
            log.error("Error getting student test records", e);
            throw e;
        }
    }
    
    private String calculateGrade(Double score) {
        if (score == null) return "N/A";
        
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        return "D";
    }
} 