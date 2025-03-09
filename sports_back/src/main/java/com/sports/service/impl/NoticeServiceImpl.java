package com.sports.service.impl;

import com.sports.common.Result;
import com.sports.entity.Notice;
import com.sports.repository.NoticeRepository;
import com.sports.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;

    @Override
    public Page<Notice> getNoticePage(String keyword, String type, int page, int size) {
        return noticeRepository.findByTitleContainingAndTypeContainingOrderByCreateTimeDesc(
            keyword == null ? "" : keyword,
            type == null ? "" : type,
            PageRequest.of(page - 1, size)
        );
    }

    @Override
    public Page<Notice> getNotices(String keyword, Pageable pageable) {
        return noticeRepository.findByKeyword(keyword, pageable);
    }

    @Override
    @Transactional
    public Result addNotice(Notice notice) {
        try {
            notice.setStatus(1); // 设置默认状态为启用
            Notice savedNotice = noticeRepository.save(notice);
            return Result.success(savedNotice);
        } catch (Exception e) {
            log.error("添加通知失败", e);
            return Result.error("添加通知失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result updateNotice(Long id, Notice notice) {
        try {
            Notice existingNotice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));

            existingNotice.setType(notice.getType());
            existingNotice.setPriority(notice.getPriority());
            existingNotice.setTitle(notice.getTitle());
            existingNotice.setContent(notice.getContent());

            noticeRepository.save(existingNotice);
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新通知失败", e);
            return Result.error("更新通知失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result deleteNotice(Long id) {
        try {
            noticeRepository.deleteById(id);
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除通知失败", e);
            return Result.error("删除通知失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result updateStatus(Long id, Integer status) {
        try {
            Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
            notice.setStatus(status);
            noticeRepository.save(notice);
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新通知状态失败", e);
            return Result.error("更新通知状态失败：" + e.getMessage());
        }
    }

    @Override
    public List<Notice> getLatestNotices() {
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Notice> noticePage = noticeRepository.findByStatus(1, pageRequest);
        return noticePage.getContent();
    }
} 