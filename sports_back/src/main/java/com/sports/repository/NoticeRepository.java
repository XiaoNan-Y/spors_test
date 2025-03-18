package com.sports.repository;

import com.sports.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    
    @Query("SELECT n FROM Notice n WHERE " +
           "(:title IS NULL OR n.title LIKE %:title%) AND " +
           "(:type IS NULL OR n.type = :type) " +
           "ORDER BY n.createTime DESC")
    Page<Notice> findByTitleAndType(@Param("title") String title, 
                                   @Param("type") String type, 
                                   Pageable pageable);
    
    List<Notice> findTop5ByStatusOrderByCreateTimeDesc(Integer status);

    Page<Notice> findByStatus(Integer status, Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE n.status = 1 ORDER BY n.createTime DESC")
    List<Notice> findLatestNotices();

    @Query("SELECT n FROM Notice n WHERE " +
           "(:keyword IS NULL OR n.title LIKE %:keyword% OR n.content LIKE %:keyword%) " +
           "ORDER BY n.createTime DESC")
    Page<Notice> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notice n WHERE n.status = 1")
    Integer countActiveNotices();

    @Query("SELECT COUNT(n) FROM Notice n WHERE n.createTime >= :since")
    Integer countNoticesSince(@Param("since") LocalDateTime since);

    @Query("SELECT n FROM Notice n WHERE n.createTime >= :startTime AND n.createTime <= :endTime")
    List<Notice> findNoticesBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(n) FROM Notice n WHERE n.createTime >= :startTime AND n.createTime <= :endTime")
    Integer countNoticesBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}