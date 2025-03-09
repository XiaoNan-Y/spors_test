package com.sports.service;

import com.sports.entity.SportsItem;
import java.util.List;

public interface SportsItemService {
    
    List<SportsItem> list(String keyword);
    
    void add(SportsItem sportsItem);
    
    void update(SportsItem sportsItem);
    
    void delete(Long id);
    
    void updateStatus(Long id, Boolean isActive);
    
    List<SportsItem> findAll();
    
    List<SportsItem> findByIsActiveTrue();
}