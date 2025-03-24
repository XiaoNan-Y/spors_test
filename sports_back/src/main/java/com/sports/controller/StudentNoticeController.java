package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.Notice;
import com.sports.entity.User;
import com.sports.repository.NoticeRepository;
import com.sports.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/student/notice-details")
public class StudentNoticeController {

    @Autowired
    private NoticeRepository noticeRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{id}")
    public Result getNoticeDetail(@PathVariable Long id) {
        try {
            Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
            return Result.success(notice);
        } catch (Exception e) {
            return Result.error("获取通知详情失败：" + e.getMessage());
        }
    }
} 