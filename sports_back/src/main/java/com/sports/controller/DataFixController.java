package com.sports.controller;

import com.sports.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;

@RestController
@RequestMapping("/api/data-fix")
public class DataFixController {
    
    @Autowired
    private EntityManager entityManager;
    
    @PostMapping("/fix-student-names")
    @Transactional
    public Result fixStudentNames() {
        try {
            String sql = "UPDATE test_record tr " +
                        "INNER JOIN user u ON tr.student_id = u.id " +
                        "SET tr.student_name = u.real_name, " +
                        "    tr.student_number = COALESCE(tr.student_number, u.student_number), " +
                        "    tr.class_name = COALESCE(tr.class_name, u.class_name) " +
                        "WHERE u.real_name IS NOT NULL";
            
            int updatedCount = entityManager.createNativeQuery(sql).executeUpdate();
            
            String sql2 = "UPDATE test_record tr " +
                         "INNER JOIN user u ON tr.student_number = u.student_number " +
                         "SET tr.student_name = u.real_name, " +
                         "    tr.class_name = COALESCE(tr.class_name, u.class_name) " +
                         "WHERE tr.student_name IS NULL " +
                         "AND u.real_name IS NOT NULL";
            
            int updatedCount2 = entityManager.createNativeQuery(sql2).executeUpdate();
            
            return Result.success("已更新 " + (updatedCount + updatedCount2) + " 条记录");
        } catch (Exception e) {
            return Result.error("修复数据失败：" + e.getMessage());
        }
    }

    @PostMapping("/fix-all-student-info")
    @Transactional
    public Result fixAllStudentInfo() {
        try {
            String sql1 = "UPDATE test_record tr " +
                         "INNER JOIN user u ON tr.student_id = u.id " +
                         "SET tr.student_name = u.real_name, " +
                         "    tr.student_number = u.student_number, " +
                         "    tr.class_name = u.class_name " +
                         "WHERE u.user_type = 'STUDENT'";
            
            int count1 = entityManager.createNativeQuery(sql1).executeUpdate();

            String sql2 = "UPDATE test_record tr " +
                         "INNER JOIN user u ON tr.student_number = u.student_number " +
                         "SET tr.student_name = u.real_name, " +
                         "    tr.class_name = u.class_name " +
                         "WHERE tr.student_id IS NULL " +
                         "AND u.user_type = 'STUDENT'";
            
            int count2 = entityManager.createNativeQuery(sql2).executeUpdate();

            return Result.success(String.format("已更新 %d 条记录 (通过ID: %d, 通过学号: %d)", 
                                             count1 + count2, count1, count2));
        } catch (Exception e) {
            return Result.error("修复数据失败：" + e.getMessage());
        }
    }

    @PostMapping("/fix-exemption-names")
    @Transactional
    public Result fixExemptionNames() {
        try {
            // 通过 student_id 更新
            String sql1 = "UPDATE test_exemption te " +
                         "INNER JOIN user u ON te.student_id = u.id " +
                         "SET te.student_name = u.real_name, " +
                         "    te.student_number = COALESCE(te.student_number, u.student_number), " +
                         "    te.class_name = COALESCE(te.class_name, u.class_name) " +
                         "WHERE u.real_name IS NOT NULL";
            
            int count1 = entityManager.createNativeQuery(sql1).executeUpdate();

            // 通过 student_number 更新没有 student_id 的记录
            String sql2 = "UPDATE test_exemption te " +
                         "INNER JOIN user u ON te.student_number = u.student_number " +
                         "SET te.student_name = u.real_name, " +
                         "    te.class_name = COALESCE(te.class_name, u.class_name) " +
                         "WHERE te.student_name IS NULL " +
                         "AND u.real_name IS NOT NULL";
            
            int count2 = entityManager.createNativeQuery(sql2).executeUpdate();

            return Result.success(String.format("已更新 %d 条记录 (通过ID: %d, 通过学号: %d)", 
                                             count1 + count2, count1, count2));
        } catch (Exception e) {
            return Result.error("修复数据失败：" + e.getMessage());
        }
    }
} 