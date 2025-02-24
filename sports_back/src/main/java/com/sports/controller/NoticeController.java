package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.Notice;
import com.sports.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notices")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping
    public Result list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size
    ) {
        Page<Notice> noticePage = noticeService.getNoticePage(keyword, type, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("records", noticePage.getContent());
        data.put("total", noticePage.getTotalElements());
        return Result.success(data);
    }

    @GetMapping("/latest")
    public Result getLatestNotices() {
        List<Notice> latestNotices = noticeService.getLatestNotices();
        return Result.success(latestNotices);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Notice notice) {
        Notice savedNotice = noticeService.addNotice(notice);
        return Result.success(savedNotice);
    }

    @PutMapping("/update")
    public Result update(@RequestBody Notice notice) {
        Notice updatedNotice = noticeService.updateNotice(notice);
        return Result.success(updatedNotice);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        // 传入 null 或空对象作为成功的返回数据
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    public Result updateStatus(
        @PathVariable Long id,
        @RequestBody Map<String, Integer> param
    ) {
        noticeService.updateStatus(id, param.get("status"));
        // 传入 null 或空对象作为成功的返回数据
        return Result.success(null);
    }
}