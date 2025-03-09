package com.sports.repository;

import com.sports.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findByTitleContainingAndTypeContainingOrderByCreateTimeDesc(
        String title, String type, Pageable pageable);
    
    List<Notice> findTop5ByStatusOrderByCreateTimeDesc(Integer status);

    Page<Notice> findByStatus(Integer status, Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE " +
           "(:keyword IS NULL OR n.title LIKE %:keyword% OR n.content LIKE %:keyword%) " +
           "ORDER BY n.createTime DESC")
    Page<Notice> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}