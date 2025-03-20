package com.sports.service;

import com.sports.entity.SportsItem;
import com.sports.dto.SportsItemStandardDTO;
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
    
    boolean existsById(Long id);
    
    void deleteById(Long id);
    
    List<SportsItemStandardDTO> getAllItemsWithStandards();
    
    SportsItemStandardDTO getItemStandards(Long id);
}