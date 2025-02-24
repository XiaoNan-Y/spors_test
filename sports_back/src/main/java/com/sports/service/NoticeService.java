package com.sports.service;

import com.sports.entity.Notice;
import com.sports.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;

    public Page<Notice> getNoticePage(String keyword, String type, int page, int size) {
        return noticeRepository.findByTitleContainingAndTypeContainingOrderByCreateTimeDesc(
            keyword == null ? "" : keyword,
            type == null ? "" : type,
            PageRequest.of(page - 1, size)
        );
    }

    public List<Notice> getLatestNotices() {
        return noticeRepository.findTop5ByStatusOrderByCreateTimeDesc(1);
    }

    public Notice addNotice(Notice notice) {
        notice.setCreateTime(LocalDateTime.now());
        notice.setStatus(1);
        return noticeRepository.save(notice);
    }

    public Notice updateNotice(Notice notice) {
        notice.setUpdateTime(LocalDateTime.now());
        return noticeRepository.save(notice);
    }

    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    public void updateStatus(Long id, Integer status) {
        Notice notice = noticeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("通知不存在"));
        notice.setStatus(status);
        notice.setUpdateTime(LocalDateTime.now());
        noticeRepository.save(notice);
    }
}