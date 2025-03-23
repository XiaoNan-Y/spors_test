package com.sports.service.impl;

import com.sports.entity.User;
import com.sports.exception.ImportException;
import com.sports.repository.UserRepository;
import com.sports.service.ExcelService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DateUtil;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelServiceImpl implements ExcelService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${file.template.path:/templates/}")
    private String templatePath;

    // 添加获取模板文件的方法
    public Resource getTemplate(String type) {
        String templateName = "STUDENT".equals(type) ? "student_import_template.xlsx" : "teacher_import_template.xlsx";
        return new ClassPathResource(templatePath + templateName);
    }

    @Override
    @Transactional
    public List<User> importStudents(MultipartFile file) throws Exception {
        List<User> users = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 验证表头
            Row headerRow = sheet.getRow(0);
            if (!validateStudentHeader(headerRow)) {
                throw new IllegalArgumentException("Excel文件格式不正确，请使用正确的模板");
            }
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                try {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    // 验证必填字段
                    String username = getCellValue(row.getCell(0));
                    String realName = getCellValue(row.getCell(1));
                    String studentNumber = getCellValue(row.getCell(2));
                    String className = getCellValue(row.getCell(3));
                    
                    if (StringUtils.isBlank(username) || StringUtils.isBlank(realName) 
                            || StringUtils.isBlank(studentNumber) || StringUtils.isBlank(className)) {
                        errors.add("第" + (i + 1) + "行：必填字段不能为空");
                        continue;
                    }
                    
                    // 检查用户名是否已存在
                    if (userRepository.existsByUsername(username)) {
                        errors.add("第" + (i + 1) + "行：用户名'" + username + "'已存在");
                        continue;
                    }
                    
                    // 检查学号是否已存在
                    if (userRepository.existsByStudentNumber(studentNumber)) {
                        errors.add("第" + (i + 1) + "行：学号'" + studentNumber + "'已存在");
                        continue;
                    }
                    
                    User user = new User();
                    user.setUsername(username);
                    user.setRealName(realName);
                    user.setStudentNumber(studentNumber);
                    user.setClassName(className);
                    user.setEmail(getCellValue(row.getCell(4)));
                    user.setPhone(getCellValue(row.getCell(5)));
                    user.setUserType(User.TYPE_STUDENT);
                    user.setPassword(passwordEncoder.encode("123456")); // 默认密码
                    user.setIsActive(true);
                    
                    users.add(userRepository.save(user));
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行：处理失败 - " + e.getMessage());
                }
            }
            
            if (!errors.isEmpty()) {
                throw new ImportException("导入过程中存在错误", errors, users);
            }
            
            return users;
        }
    }

    @Override
    @Transactional
    public List<User> importTeachers(MultipartFile file) throws Exception {
        List<User> users = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 验证表头
            Row headerRow = sheet.getRow(0);
            if (!validateTeacherHeader(headerRow)) {
                throw new IllegalArgumentException("Excel文件格式不正确，请使用正确的模板");
            }
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                try {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    // 验证必填字段
                    String username = getCellValue(row.getCell(0));
                    String realName = getCellValue(row.getCell(1));
                    
                    if (StringUtils.isBlank(username) || StringUtils.isBlank(realName)) {
                        errors.add("第" + (i + 1) + "行：必填字段不能为空");
                        continue;
                    }
                    
                    // 检查用户名是否已存在
                    if (userRepository.existsByUsername(username)) {
                        errors.add("第" + (i + 1) + "行：用户名'" + username + "'已存在");
                        continue;
                    }
                    
                    User user = new User();
                    user.setUsername(username);
                    user.setRealName(realName);
                    user.setEmail(getCellValue(row.getCell(2)));
                    user.setPhone(getCellValue(row.getCell(3)));
                    user.setUserType(User.TYPE_TEACHER);
                    user.setPassword(passwordEncoder.encode("123456")); // 默认密码
                    user.setIsActive(true);
                    
                    users.add(userRepository.save(user));
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行：处理失败 - " + e.getMessage());
                }
            }
            
            if (!errors.isEmpty()) {
                throw new ImportException("导入过程中存在错误", errors, users);
            }
            
            return users;
        }
    }

    private boolean validateStudentHeader(Row headerRow) {
        String[] expectedHeaders = {"用户名", "姓名", "学号", "班级", "邮箱", "电话"};
        return validateHeader(headerRow, expectedHeaders);
    }

    private boolean validateTeacherHeader(Row headerRow) {
        String[] expectedHeaders = {"用户名", "姓名", "邮箱", "电话"};
        return validateHeader(headerRow, expectedHeaders);
    }

    private boolean validateHeader(Row headerRow, String[] expectedHeaders) {
        if (headerRow == null) return false;
        
        for (int i = 0; i < expectedHeaders.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null || !expectedHeaders[i].equals(getCellValue(cell))) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf((long)cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
} 