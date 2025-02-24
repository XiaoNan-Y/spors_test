package com.sports.controller;

import com.sports.common.Result;
import com.sports.repository.UserRepository;
import com.sports.repository.SportsItemRepository;
import com.sports.repository.TestRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportsItemRepository sportsItemRepository;

    @Autowired
    private TestRecordRepository testRecordRepository;

    @GetMapping("/stats")
    public Result getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 获取用户统计
            long studentCount = userRepository.countByUserType("STUDENT");
            long teacherCount = userRepository.countByUserType("TEACHER");
            
            // 获取体育项目数量
            long sportsItemCount = sportsItemRepository.countByIsActiveTrue();
            
            // 获取测试记录数量
            long testRecordCount = testRecordRepository.count();
            
            stats.put("studentCount", studentCount);
            stats.put("teacherCount", teacherCount);
            stats.put("sportsItemCount", sportsItemCount);
            stats.put("testRecordCount", testRecordCount);
            
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }
}