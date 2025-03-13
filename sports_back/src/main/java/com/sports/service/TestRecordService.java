package com.sports.service;

import com.sports.entity.TestRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface TestRecordService {
    /**
     * 获取学生在某个项目的历史测试记录
     * @param studentNumber 学生学号
     * @param sportsItemId 体育项目ID
     * @param excludeId 需要排除的记录ID（可选）
     * @return 历史测试记录列表
     */
    List<TestRecord> getHistoryRecords(String studentNumber, Long sportsItemId, Long excludeId);

    Page<TestRecord> getRecordList(String status, Long sportsItemId, Pageable pageable);
    
    TestRecord save(TestRecord record);
    
    TestRecord updateRecord(TestRecord record);
    
    TestRecord reviewRecord(Long id, String status, String reviewComment, Long reviewerId);
    
    boolean checkAbnormalScore(TestRecord record);
    
    String getAbnormalReason(TestRecord record);

    /**
     * 从Excel文件导入数据
     */
    List<TestRecord> importFromExcel(MultipartFile file) throws Exception;

    /**
     * 导出数据到Excel
     */
    void exportToExcel(HttpServletResponse response, String status, Long sportsItemId) throws Exception;

    /**
     * 生成Excel导入模板
     */
    void generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 批量保存测试记录
     */
    List<TestRecord> saveAll(List<TestRecord> records);

    TestRecord modifyReview(Long id, String status, String comment, Long reviewerId);

    Page<TestRecord> getTestRecords(String className, Long sportsItemId, 
                                   String status, String studentNumber, 
                                   Pageable pageable);
} 