package com.sports.service.impl;

import com.sports.common.Result;
import com.sports.repository.UserRepository;
import com.sports.repository.SportsItemRepository;
import com.sports.repository.TestRecordRepository;
import com.sports.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SportsItemRepository sportsItemRepository;
    
    @Autowired
    private TestRecordRepository testRecordRepository;

    @Override
    public Result getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取用户统计
        long studentCount = userRepository.countByUserType("STUDENT");
        long teacherCount = userRepository.countByUserType("TEACHER");
        stats.put("studentCount", studentCount);
        stats.put("teacherCount", teacherCount);
        
        // 获取体育项目数量
        long sportsItemCount = sportsItemRepository.countByIsActiveTrue();
        stats.put("sportsItemCount", sportsItemCount);
        
        // 获取测试记录数量
        long testRecordCount = testRecordRepository.getTestCount();
        stats.put("testRecordCount", testRecordCount);
        
        // 获取各项目测试分布
        List<Object[]> itemStats = testRecordRepository.getTestCountByItem();
        Map<String, Long> itemDistribution = itemStats.stream()
            .collect(Collectors.toMap(
                arr -> (String) arr[0],
                arr -> (Long) arr[1]
            ));
        stats.put("itemDistribution", itemDistribution);
        
        // 获取成绩分布
        List<Object[]> scoreStats = testRecordRepository.getAverageScoreByStudent();
        Map<String, Object> scoreDistribution = new HashMap<>();
        scoreStats.forEach(arr -> {
            Long studentId = (Long) arr[0];
            Double avgScore = (Double) arr[1];
            scoreDistribution.put(studentId.toString(), avgScore);
        });
        stats.put("scoreDistribution", scoreDistribution);
        
        return Result.success(stats);
    }
} 