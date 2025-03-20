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
import com.sports.repository.ScoreAppealRepository;
import com.sports.entity.ScoreAppeal;
import com.sports.dto.ScoreAppealDTO;
import com.sports.service.StudentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private ScoreAppealRepository scoreAppealRepository;

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

    @Override
    public List<TestRecordDTO> getAppealableRecords(Long userId) {
        try {
            log.info("开始获取可申诉记录 - userId: {}", userId);
            
            // 获取学生信息
            User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            log.info("找到学生信息 - studentNumber: {}, realName: {}", 
                student.getStudentNumber(), student.getRealName());
            
            // 使用学号获取已审核通过的记录
            List<TestRecord> records = testRecordRepository.findByStudentNumberAndStatusWithSportsItem(
                student.getStudentNumber(), "APPROVED");
            log.info("找到已审核通过的记录数量: {}", records.size());
            
            // 打印每条记录的详细信息
            records.forEach(record -> {
                log.info("记录详情 - ID: {}, 项目: {}, 成绩: {}, 状态: {}, 学号: {}", 
                    record.getId(),
                    record.getSportsItem() != null ? record.getSportsItem().getName() : "未知项目",
                    record.getScore(),
                    record.getStatus(),
                    record.getStudentNumber()
                );
            });
            
            // 转换为DTO
            List<TestRecordDTO> appealableRecords = records.stream()
                .map(record -> {
                    TestRecordDTO dto = new TestRecordDTO();
                    BeanUtils.copyProperties(record, dto);
                    if (record.getSportsItem() != null) {
                        dto.setSportsItemName(record.getSportsItem().getName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

            log.info("最终可申诉记录数量: {}", appealableRecords.size());
            return appealableRecords;
            
        } catch (Exception e) {
            log.error("获取可申诉记录失败 - userId: {}", userId, e);
            throw new RuntimeException("获取可申诉记录失败", e);
        }
    }

    @Override
    @Transactional
    public ScoreAppealDTO submitAppeal(ScoreAppealDTO appealDTO, Long userId) {
        // 检查是否已经存在未完成的申诉
        if (scoreAppealRepository.existsByTestRecordIdAndStatusNot(
                appealDTO.getTestRecordId(), "REJECTED")) {
            throw new RuntimeException("该成绩已有申诉在处理中");
        }

        // 获取测试记录
        TestRecord testRecord = testRecordRepository.findById(appealDTO.getTestRecordId())
            .orElseThrow(() -> new RuntimeException("测试记录不存在"));

        // 创建申诉记录
        ScoreAppeal appeal = new ScoreAppeal();
        appeal.setTestRecord(testRecord);
        appeal.setStudent(new User(userId));
        appeal.setOriginalScore(testRecord.getScore());
        appeal.setExpectedScore(appealDTO.getExpectedScore());
        appeal.setReason(appealDTO.getReason());
        appeal.setStatus("PENDING");
        appeal.setCreateTime(LocalDateTime.now());

        // 保存申诉记录
        ScoreAppeal saved = scoreAppealRepository.save(appeal);
        
        // 转换为DTO返回
        ScoreAppealDTO resultDTO = new ScoreAppealDTO();
        BeanUtils.copyProperties(saved, resultDTO);
        resultDTO.setTestRecordId(saved.getTestRecord().getId());
        resultDTO.setSportsItemName(saved.getTestRecord().getSportsItem().getName());
        return resultDTO;
    }

    @Override
    public Page<ScoreAppealDTO> getStudentAppeals(Long userId, String status, Pageable pageable) {
        try {
            List<ScoreAppeal> appeals = scoreAppealRepository.findByStudentIdAndStatusWithDetails(userId, status);
            
            List<ScoreAppealDTO> dtoList = appeals.stream()
                .map(appeal -> {
                    ScoreAppealDTO dto = new ScoreAppealDTO();
                    
                    // 设置基本信息
                    dto.setId(appeal.getId());
                    dto.setCreateTime(appeal.getCreateTime());
                    dto.setExpectedScore(appeal.getExpectedScore());
                    dto.setOriginalScore(appeal.getOriginalScore());
                    dto.setReason(appeal.getReason());
                    dto.setStatus(appeal.getStatus());
                    dto.setReviewComment(appeal.getReviewComment());
                    dto.setReviewTime(appeal.getReviewTime());
                    
                    // 设置测试记录相关信息
                    if (appeal.getTestRecord() != null) {
                        dto.setTestRecordId(appeal.getTestRecord().getId());
                        if (appeal.getTestRecord().getSportsItem() != null) {
                            dto.setSportsItemName(appeal.getTestRecord().getSportsItem().getName());
                        }
                    }
                    
                    // 设置学生信息
                    if (appeal.getStudent() != null) {
                        dto.setStudentName(appeal.getStudent().getRealName());
                        dto.setStudentNumber(appeal.getStudent().getStudentNumber());
                    }
                    
                    // 设置审核人信息
                    if (appeal.getReviewer() != null) {
                        dto.setReviewerName(appeal.getReviewer().getRealName());
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
            
            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), dtoList.size());
            
            return new PageImpl<>(
                dtoList.subList(start, end),
                pageable,
                dtoList.size()
            );
        } catch (Exception e) {
            log.error("获取学生申诉列表失败", e);
            throw new RuntimeException("获取申诉列表失败", e);
        }
    }
} 