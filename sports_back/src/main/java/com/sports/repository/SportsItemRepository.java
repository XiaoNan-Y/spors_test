package com.sports.repository;

import com.sports.entity.SportsItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SportsItemRepository extends JpaRepository<SportsItem, Long> {
    List<SportsItem> findByNameContaining(String keyword);
    long countByIsActiveTrue();
    List<SportsItem> findByIsActiveTrue();
}