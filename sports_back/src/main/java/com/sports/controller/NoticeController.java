package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.Notice;
import com.sports.service.NoticeService;
import com.sports.repository.NoticeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/notices")
public class NoticeController {

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/latest")
    public Result getLatestNotices() {
        try {
            List<Notice> notices = noticeRepository.findLatestNotices();
            return Result.success(notices);
        } catch (Exception e) {
            return Result.error("获取公告失败：" + e.getMessage());
        }
    }

    @GetMapping
    public Result getNotices(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Notice> notices = noticeService.getNotices(keyword, PageRequest.of(page, size));
            return Result.success(notices);
        } catch (Exception e) {
            log.error("获取通知列表失败", e);
            return Result.error("获取通知列表失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result addNotice(@RequestBody Notice notice) {
        try {
            notice.setCreateTime(LocalDateTime.now());
            notice.setUpdateTime(LocalDateTime.now());
            notice.setStatus(1);  // 默认启用
            Notice saved = noticeRepository.save(notice);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("添加公告失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
        try {
            Notice existing = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
            
            existing.setTitle(notice.getTitle());
            existing.setContent(notice.getContent());
            existing.setUpdateTime(LocalDateTime.now());
            
            Notice updated = noticeRepository.save(existing);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新公告失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestBody Notice notice) {
        try {
            Notice existing = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
            
            existing.setStatus(notice.getStatus());
            existing.setUpdateTime(LocalDateTime.now());
            
            Notice updated = noticeRepository.save(existing);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新状态失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result deleteNotice(@PathVariable Long id) {
        try {
            noticeRepository.deleteById(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除公告失败：" + e.getMessage());
        }
    }
}