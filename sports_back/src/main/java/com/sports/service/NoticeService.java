package com.sports.service;

import com.sports.common.Result;
import com.sports.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoticeService {
    
    /**
     * 获取公告列表
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 分页后的公告列表
     */
    Page<Notice> getNotices(String keyword, Pageable pageable);
    
    Result addNotice(Notice notice);
    
    Result updateNotice(Long id, Notice notice);
    
    Result deleteNotice(Long id);
    
    Result updateStatus(Long id, Integer status);
    
    List<Notice> getLatestNotices();
    
    Page<Notice> getNoticePage(String keyword, String type, int page, int size);
} 