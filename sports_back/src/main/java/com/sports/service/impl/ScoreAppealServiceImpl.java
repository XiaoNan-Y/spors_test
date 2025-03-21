package com.sports.service.impl;

import com.sports.dto.ScoreAppealDTO;
import com.sports.entity.ScoreAppeal;
import com.sports.entity.TestRecord;
import com.sports.entity.User;
import com.sports.repository.ScoreAppealRepository;
import com.sports.repository.TestRecordRepository;
import com.sports.repository.UserRepository;
import com.sports.service.ScoreAppealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScoreAppealServiceImpl implements ScoreAppealService {

    private static final Logger log = LoggerFactory.getLogger(ScoreAppealServiceImpl.class);

    @Autowired
    private ScoreAppealRepository appealRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRecordRepository testRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ScoreAppealDTO> getTeacherAppeals(Long teacherId, String status, Pageable pageable) {
        try {
            log.info("Getting teacher appeals - status: {}", status);
            
            // 获取分页数据
            Page<ScoreAppeal> appealPage = appealRepository.findAllByStatus(status, pageable);
            
            if (appealPage.isEmpty()) {
                log.info("No appeals found");
                return Page.empty(pageable);
            }
            
            // 获取详细数据
            List<ScoreAppeal> appealsWithDetails = appealRepository.findByAppealsWithDetails(appealPage.getContent());
            
            log.info("Found {} appeals with details", appealsWithDetails.size());
            
            // 转换为DTO
            List<ScoreAppealDTO> dtoList = appealsWithDetails.stream()
                .map(appeal -> {
                    ScoreAppealDTO dto = new ScoreAppealDTO();
                    
                    // 设置基本信息
                    dto.setId(appeal.getId());
                    dto.setStatus(appeal.getStatus());
                    dto.setCreateTime(appeal.getCreateTime());
                    dto.setReviewTime(appeal.getReviewTime());
                    dto.setOriginalScore(appeal.getOriginalScore());
                    dto.setExpectedScore(appeal.getExpectedScore());
                    dto.setReason(appeal.getReason());
                    dto.setReviewComment(appeal.getReviewComment());
                    
                    // 设置学生信息
                    if (appeal.getStudent() != null) {
                        dto.setStudentId(appeal.getStudent().getId());
                        dto.setStudentName(appeal.getStudent().getRealName());
                        dto.setStudentNumber(appeal.getStudent().getStudentNumber());
                        dto.setClassName(appeal.getStudent().getClassName());
                        log.info("Setting student info - name: {}, number: {}, class: {}", 
                            appeal.getStudent().getRealName(),
                            appeal.getStudent().getStudentNumber(),
                            appeal.getStudent().getClassName());
                    }
                    
                    // 设置测试记录信息
                    if (appeal.getTestRecord() != null && appeal.getTestRecord().getSportsItem() != null) {
                        dto.setTestRecordId(appeal.getTestRecord().getId());
                        dto.setTestItem(appeal.getTestRecord().getSportsItem().getName());
                        dto.setSportsItemName(appeal.getTestRecord().getSportsItem().getName());
                        log.info("Setting test record info - id: {}, item: {}", 
                            appeal.getTestRecord().getId(),
                            appeal.getTestRecord().getSportsItem().getName());
                    }
                    
                    // 设置审核人信息
                    if (appeal.getReviewer() != null) {
                        dto.setReviewByName(appeal.getReviewer().getRealName());
                        dto.setReviewerName(appeal.getReviewer().getRealName());
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
            
            log.info("Converted to DTOs, size: {}", dtoList.size());
            
            // 打印第一条数据的详细信息，用于调试
            if (!dtoList.isEmpty()) {
                ScoreAppealDTO firstDto = dtoList.get(0);
                log.info("First appeal details: studentName={}, studentNumber={}, className={}, testItem={}, originalScore={}, expectedScore={}, status={}",
                    firstDto.getStudentName(),
                    firstDto.getStudentNumber(),
                    firstDto.getClassName(),
                    firstDto.getTestItem(),
                    firstDto.getOriginalScore(),
                    firstDto.getExpectedScore(),
                    firstDto.getStatus());
            }
            
            return new PageImpl<>(dtoList, pageable, appealPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("获取教师申诉列表失败", e);
            throw new RuntimeException("获取申诉列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ScoreAppealDTO reviewAppeal(Long id, ScoreAppealDTO appealDTO, Long teacherId) {
        ScoreAppeal appeal = appealRepository.findByIdWithDetails(id);
        if (appeal == null) {
            throw new RuntimeException("申诉不存在");
        }
        
        User teacher = userRepository.findById(teacherId)
            .orElseThrow(() -> new RuntimeException("教师不存在"));

        appeal.setStatus(appealDTO.getStatus());
        appeal.setReviewComment(appealDTO.getReviewComment());
        appeal.setReviewer(teacher);
        appeal.setReviewTime(LocalDateTime.now());

        if ("APPROVED".equals(appealDTO.getStatus())) {
            // 更新测试记录的成绩
            TestRecord testRecord = appeal.getTestRecord();
            testRecord.setScore(appealDTO.getExpectedScore());
            testRecordRepository.save(testRecord);
        }

        ScoreAppeal updated = appealRepository.save(appeal);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public ScoreAppealDTO submitAppeal(ScoreAppealDTO appealDTO, Long studentId) {
        // 实现学生提交申诉的逻辑
        return null; // 待实现
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScoreAppealDTO> getStudentAppeals(Long studentId, String status, Pageable pageable) {
        // 实现获取学生申诉列表的逻辑
        return null; // 待实现
    }

    private ScoreAppealDTO convertToDTO(ScoreAppeal appeal) {
        ScoreAppealDTO dto = new ScoreAppealDTO();
        
        dto.setId(appeal.getId());
        dto.setStatus(appeal.getStatus());
        dto.setCreateTime(appeal.getCreateTime());
        dto.setReviewTime(appeal.getReviewTime());
        dto.setOriginalScore(appeal.getOriginalScore());
        dto.setExpectedScore(appeal.getExpectedScore());
        dto.setReason(appeal.getReason());
        dto.setReviewComment(appeal.getReviewComment());
        
        if (appeal.getStudent() != null) {
            dto.setStudentId(appeal.getStudent().getId());
            dto.setStudentName(appeal.getStudent().getRealName());
            dto.setStudentNumber(appeal.getStudent().getStudentNumber());
            dto.setClassName(appeal.getStudent().getClassName());
        }
        
        if (appeal.getTestRecord() != null && appeal.getTestRecord().getSportsItem() != null) {
            dto.setTestRecordId(appeal.getTestRecord().getId());
            dto.setTestItem(appeal.getTestRecord().getSportsItem().getName());
        }
        
        if (appeal.getReviewer() != null) {
            dto.setReviewByName(appeal.getReviewer().getRealName());
        }
        
        return dto;
    }
} 