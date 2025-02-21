package com.sports.repository;

import com.sports.entity.SportsItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SportsItemRepository extends JpaRepository<SportsItem, Long> {
    long countByIsActiveTrue();
} 