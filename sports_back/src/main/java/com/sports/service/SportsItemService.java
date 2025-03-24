package com.sports.service;

import com.sports.entity.SportsItem;
import com.sports.dto.SportsItemStandardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SportsItemService {
    List<SportsItem> findByIsActiveTrue();
    SportsItem save(SportsItem sportsItem);
    boolean existsById(Long id);
    void deleteById(Long id);
    Optional<SportsItem> findById(Long id);
    List<SportsItem> findActiveItems();
    SportsItemStandardDTO getItemStandards(Long id);
    
    List<SportsItem> findAll();
    
    Page<SportsItem> findByKeyword(String keyword, Pageable pageable);
    
    boolean updateStatus(Long id, boolean isActive);
    
    List<SportsItemStandardDTO> getAllItemsWithStandards();
    
    SportsItem update(SportsItem sportsItem);
}