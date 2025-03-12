package com.sports.service;

import com.sports.entity.SportsItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SportsItemService {
    SportsItem save(SportsItem sportsItem);
    
    void delete(Long id);
    
    SportsItem update(SportsItem sportsItem);
    
    Optional<SportsItem> findById(Long id);
    
    List<SportsItem> findAll();
    
    Page<SportsItem> findByKeyword(String keyword, Pageable pageable);
    
    List<SportsItem> findActiveItems();
    
    boolean updateStatus(Long id, boolean isActive);
    
    List<SportsItem> getAllActiveItems();
    
    boolean existsById(Long id);
    
    void deleteById(Long id);
}