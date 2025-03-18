package com.sports.controller;

import com.sports.common.Result;
import com.sports.dto.TeacherDashboardDTO;
import com.sports.service.TeacherDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/dashboard")
public class TeacherDashboardController {

    @Autowired
    private TeacherDashboardService teacherDashboardService;

    @GetMapping("/stats")
    public Result<TeacherDashboardDTO> getDashboardStats() {
        TeacherDashboardDTO stats = teacherDashboardService.getDashboardStats();
        return Result.success(stats);
    }
} 