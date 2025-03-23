package com.sports.service.impl;

import com.sports.dto.ExemptionApplicationDTO;
import com.sports.entity.ExemptionApplication;
import com.sports.entity.User;
import com.sports.entity.RetestApplication;
import com.sports.repository.ExemptionApplicationRepository;
import com.sports.repository.UserRepository;
import com.sports.repository.RetestApplicationRepository;
import com.sports.service.ExemptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Slf4j
@Service
public class ExemptionServiceImpl implements ExemptionService {

    @Autowired
    private ExemptionApplicationRepository exemptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RetestApplicationRepository retestApplicationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ExemptionApplication> getExemptionApplications(String keyword, Pageable pageable) {
        try {
            Page<ExemptionApplication> applications = exemptionRepository.findByStudentNameContainingOrStudentNumberContaining(
                keyword != null ? keyword.trim() : "", 
                keyword != null ? keyword.trim() : "", 
                pageable
            );
            
            // 手动初始化懒加载的关联对象
            applications.getContent().forEach(app -> {
                if (app.getSportsItem() != null) {
                    app.getSportsItem().getId();  // 触发懒加载
                    app.getSportsItem().getName();
                }
                if (app.getStudent() != null) {
                    app.getStudent().getId();  // 触发懒加载
                    app.getStudent().getRealName();
                }
            });
            
            return applications;
        } catch (Exception e) {
            log.error("获取免测申请列表失败", e);
            throw new RuntimeException("获取免测申请列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ExemptionApplication submitApplication(ExemptionApplication application) {
        try {
            // 获取学生信息
            User student = userRepository.findById(application.getStudentId())
                .orElseThrow(() -> new RuntimeException("学生信息不存在"));
            
            // 设置学生相关信息，注意字段对应关系
            application.setStudentNumber(student.getStudentNumber());  // 学号
            application.setStudentName(student.getRealName());        // User表的real_name对应到ExemptionApplication表的student_name
            application.setClassName(student.getClassName());         // 班级
            
            // 设置申请状态和时间
            application.setStatus("PENDING");
            application.setType("EXEMPTION");
            application.setApplyTime(LocalDateTime.now());
            application.setUpdateTime(LocalDateTime.now());
            application.setCreatedAt(LocalDateTime.now());
            application.setUpdatedAt(LocalDateTime.now());
            
            // 保存申请前添加调试日志
            log.debug("准备保存申请 - studentId: {}, realName: {}, studentNumber: {}, className: {}", 
                student.getId(), student.getRealName(), student.getStudentNumber(), student.getClassName());
            
            // 保存申请
            ExemptionApplication saved = exemptionRepository.save(application);
            
            // 保存后添加调试日志
            log.debug("申请保存成功 - id: {}, studentName: {}, studentNumber: {}, className: {}", 
                saved.getId(), saved.getStudentName(), saved.getStudentNumber(), saved.getClassName());
            
            return saved;
        } catch (Exception e) {
            log.error("提交申请失败", e);
            throw new RuntimeException("提交申请失败: " + e.getMessage());
        }
    }

    @Override
    public Page<ExemptionApplication> getStudentApplications(Long studentId, Pageable pageable) {
        log.debug("Fetching applications for student: {}", studentId);
        try {
            // 使用 studentId 查询
            Page<ExemptionApplication> applications = exemptionRepository.findByStudentIdWithPage(studentId, pageable);
            log.debug("Found {} applications", applications.getTotalElements());
            return applications;
        } catch (Exception e) {
            log.error("Error fetching applications for student {}", studentId, e);
            throw new RuntimeException("获取申请记录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExemptionApplicationDTO> getAllApplications(
            String className, String type, String status, String keyword, Pageable pageable) {
        List<ExemptionApplication> applications = exemptionRepository.findAllWithFilters(type, keyword, pageable);
        long total = exemptionRepository.countWithFilters(type, keyword);
        
        List<ExemptionApplicationDTO> dtos = applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        return new PageImpl<>(dtos, pageable, total);
    }

    @Override
    @Transactional
    public ExemptionApplication adminReview(Long id, String status, String comment, Long reviewerId) {
        ExemptionApplication application = exemptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("申请不存在"));

        if (!"EXEMPTION".equals(application.getType())) {
            throw new RuntimeException("只能审核免测申请");
        }

        application.setStatus(status);
        application.setAdminReviewComment(comment);
        application.setAdminReviewTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        
        return exemptionRepository.save(application);
    }

    @Override
    @Transactional
    public ExemptionApplication teacherReview(Long id, String status, String comment, Long reviewerId) {
        try {
            RetestApplication application = retestApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
                
            // 验证状态是否为待审核
            if (!"PENDING".equals(application.getStatus())) {
                throw new RuntimeException("只能审核待审核状态的申请");
            }
            
            // 更新申请状态
            application.setStatus(status);
            application.setTeacherReviewComment(comment);
            application.setTeacherReviewTime(LocalDateTime.now());
            application.setReviewerId(reviewerId);
            
            // 获取审核教师信息
            User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("审核教师不存在"));
            application.setReviewerName(reviewer.getRealName());
            
            RetestApplication saved = retestApplicationRepository.save(application);
            
            // 转换为 ExemptionApplication 对象返回
            ExemptionApplication result = new ExemptionApplication();
            BeanUtils.copyProperties(saved, result);
            return result;
        } catch (Exception e) {
            log.error("审核重测申请失败", e);
            throw new RuntimeException("审核失败: " + e.getMessage());
        }
    }

    @Override
    public ExemptionApplication getApplicationById(Long id) {
        return exemptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("申请不存在"));
    }

    @Override
    @Transactional
    public void deleteApplication(Long id, Long studentId) {
        ExemptionApplication application = exemptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        if (!application.getStudentId().equals(studentId)) {
            throw new RuntimeException("无权删除此申请");
        }
        
        if (!application.getStatus().startsWith("PENDING")) {
            throw new RuntimeException("只能删除待审核的申请");
        }
        
        exemptionRepository.delete(application);
    }

    @Override
    @Transactional
    public ExemptionApplicationDTO updateApplication(ExemptionApplication application) {
        // 更新时间
        application.setUpdateTime(LocalDateTime.now());
        
        ExemptionApplication updated = exemptionRepository.save(application);
        return convertToDTO(updated);
    }

    @Override
    public Page<ExemptionApplicationDTO> getTeacherPendingApplications(
            String className, String type, String keyword, Pageable pageable) {
        try {
            // 构建查询条件
            List<ExemptionApplication> applications;
            if (className != null && !className.isEmpty()) {
                // 如果指定了班级，则按班级筛选
                applications = exemptionRepository.findAllWithFiltersNoPage(
                    className, type, "PENDING_TEACHER", keyword);
            } else {
                // 否则查询所有待教师审核的申请
                applications = exemptionRepository.findAllWithFiltersNoPage(
                    null, type, "PENDING_TEACHER", keyword);
            }
            
            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), applications.size());
            
            List<ExemptionApplication> pageContent = start < end ? 
                applications.subList(start, end) : 
                new ArrayList<>();
                
            // 转换为DTO
            List<ExemptionApplicationDTO> dtos = pageContent.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
            return new PageImpl<>(dtos, pageable, applications.size());
        } catch (Exception e) {
            log.error("获取教师待审核申请失败", e);
            throw new RuntimeException("获取教师待审核申请失败: " + e.getMessage());
        }
    }

    @Override
    public Page<ExemptionApplicationDTO> getAdminPendingApplications(
            String className, String type, String keyword, Pageable pageable) {
        try {
            // 构建查询条件
            List<ExemptionApplication> applications;
            if (className != null && !className.isEmpty()) {
                // 如果指定了班级，则按班级筛选
                applications = exemptionRepository.findAllWithFiltersNoPage(
                    className, type, "PENDING_ADMIN", keyword);
            } else {
                // 否则查询所有待管理员审核的申请
                applications = exemptionRepository.findAllWithFiltersNoPage(
                    null, type, "PENDING_ADMIN", keyword);
            }
            
            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), applications.size());
            
            List<ExemptionApplication> pageContent = start < end ? 
                applications.subList(start, end) : 
                new ArrayList<>();
                
            // 转换为DTO
            List<ExemptionApplicationDTO> dtos = pageContent.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
            return new PageImpl<>(dtos, pageable, applications.size());
        } catch (Exception e) {
            log.error("获取管理员待审核申请失败", e);
            throw new RuntimeException("获取管理员待审核申请失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getDistinctClassNames() {
        try {
            return userRepository.findDistinctClassName();
        } catch (Exception e) {
            log.error("获取班级列表失败", e);
            throw new RuntimeException("获取班级列表失败: " + e.getMessage());
        }
    }

    @Override
    public Page<ExemptionApplication> getAdminExemptionApplications(
            String keyword, String status, Pageable pageable) {
        try {
            // 添加调试日志
            log.debug("Fetching admin exemption applications - keyword: {}, status: {}", keyword, status);
            
            // 直接使用 findByTypeAndStatusAndKeyword 方法
            Page<ExemptionApplication> applications = exemptionRepository.findByTypeAndStatusAndKeyword(
                status, keyword, pageable);
            
            // 添加调试日志
            log.debug("Found {} applications", applications.getTotalElements());
            applications.getContent().forEach(app -> {
                log.debug("Application: id={}, studentName={}, status={}, applyTime={}", 
                    app.getId(), app.getStudentName(), app.getStatus(), 
                    app.getApplyTime().toLocalTime());
            });
            
            return applications;
        } catch (Exception e) {
            log.error("Error fetching admin exemption applications", e);
            throw new RuntimeException("获取管理员免测申请列表失败: " + e.getMessage());
        }
    }

    @Override
    public Page<ExemptionApplication> getTeacherRetestApplications(String keyword, Pageable pageable) {
        try {
            // 使用 RetestApplicationRepository 获取数据
            Page<RetestApplication> retestApplications = retestApplicationRepository.findPendingApplications(
                keyword, 
                pageable
            );
            
            // 转换为 ExemptionApplication 对象（为了保持接口兼容）
            return retestApplications.map(retest -> {
                ExemptionApplication exemption = new ExemptionApplication();
                BeanUtils.copyProperties(retest, exemption);
                return exemption;
            });
        } catch (Exception e) {
            log.error("获取重测申请列表失败", e);
            throw new RuntimeException("获取重测申请列表失败: " + e.getMessage());
        }
    }

    @Override
    public Page<ExemptionApplication> getTeacherExemptionApplications(String keyword, Pageable pageable) {
        try {
            return exemptionRepository.findPendingExemptionApplications(keyword, pageable);
        } catch (Exception e) {
            log.error("获取教师免测申请列表失败", e);
            throw new RuntimeException("获取教师免测申请列表失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportApprovedExemptions() throws Exception {
        try {
            // 查询所有已通过的申请
            List<ExemptionApplication> approvedApplications = exemptionRepository
                .findByTypeAndStatus("EXEMPTION", "APPROVED");

            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("免测申请数据");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"序号", "学号", "姓名", "班级", "申请原因", "申请时间", "审核意见", "审核时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            int rowNum = 1;
            for (ExemptionApplication app : approvedApplications) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(app.getStudentNumber());
                row.createCell(2).setCellValue(app.getStudentName());
                row.createCell(3).setCellValue(app.getClassName());
                row.createCell(4).setCellValue(app.getReason());
                row.createCell(5).setCellValue(
                    app.getApplyTime() != null ? 
                    app.getApplyTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : ""
                );
                row.createCell(6).setCellValue(app.getAdminReviewComment());
                row.createCell(7).setCellValue(
                    app.getAdminReviewTime() != null ? 
                    app.getAdminReviewTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : ""
                );
            }

            // 调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 将工作簿写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("导出免测申请数据失败", e);
            throw new Exception("导出失败：" + e.getMessage());
        }
    }

    private ExemptionApplicationDTO convertToDTO(ExemptionApplication application) {
        if (application == null) return null;
        
        ExemptionApplicationDTO dto = new ExemptionApplicationDTO();
        BeanUtils.copyProperties(application, dto, "sportsItem");
        
        if (application.getSportsItem() != null) {
            dto.setSportsItemId(application.getSportsItem().getId());
            dto.setSportsItemName(application.getSportsItem().getName());
        }
        
        return dto;
    }
} 