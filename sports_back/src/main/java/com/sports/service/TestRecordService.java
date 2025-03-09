package com.sports.service;

import com.sports.entity.TestRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

public interface TestRecordService {
    Page<TestRecord> getRecordList(String status, Long teacherId, Long sportsItemId, 
                                 LocalDate startDate, LocalDate endDate, Pageable pageable);
    
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
    void exportToExcel(HttpServletResponse response, String status, Long teacherId, Long sportsItemId) throws Exception;

    /**
     * 生成Excel导入模板
     */
    void generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 批量保存测试记录
     */
    List<TestRecord> saveAll(List<TestRecord> records);
} 