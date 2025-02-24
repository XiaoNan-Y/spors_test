package com.sports.repository;

import com.sports.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findByTitleContainingAndTypeContainingOrderByCreateTimeDesc(
        String title, String type, Pageable pageable);
    
    List<Notice> findTop5ByStatusOrderByCreateTimeDesc(Integer status);
}