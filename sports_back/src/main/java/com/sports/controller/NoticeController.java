package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.Notice;
import com.sports.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
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
        return noticeService.addNotice(notice);
    }

    @PutMapping("/{id}")
    public Result updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
        return noticeService.updateNotice(id, notice);
    }

    @DeleteMapping("/{id}")
    public Result deleteNotice(@PathVariable Long id) {
        return noticeService.deleteNotice(id);
    }

    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestBody Notice notice) {
        return noticeService.updateStatus(id, notice.getStatus());
    }
}