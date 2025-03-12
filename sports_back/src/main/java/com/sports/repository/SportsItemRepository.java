package com.sports.repository;

import com.sports.entity.SportsItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SportsItemRepository extends JpaRepository<SportsItem, Long> {
    
    @Query("SELECT s FROM SportsItem s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.type) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SportsItem> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    long countByIsActiveTrue();
    
    List<SportsItem> findByIsActiveTrue();
    
    List<SportsItem> findByType(String type);
    
    List<SportsItem> findByTypeAndIsActiveTrue(String type);
    
    // 添加按名称模糊查询的方法
    Page<SportsItem> findByNameContainingIgnoreCase(String name, Pageable pageable);
}