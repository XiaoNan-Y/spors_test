package com.sports.repository;

import com.sports.entity.SportsItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SportsItemRepository extends JpaRepository<SportsItem, Long> {
    List<SportsItem> findByNameContaining(String keyword);
    long countByIsActiveTrue();
}