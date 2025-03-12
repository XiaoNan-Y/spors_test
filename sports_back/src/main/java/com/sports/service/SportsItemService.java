package com.sports.service;

import com.sports.entity.SportsItem;
import java.util.List;

public interface SportsItemService {
    List<SportsItem> getAllActiveItems();
    SportsItem getById(Long id);
    SportsItem save(SportsItem item);
    void delete(Long id);
    void updateStatus(Long id, boolean isActive);
    List<SportsItem> list(String keyword);
    void update(SportsItem sportsItem);
    List<SportsItem> findAll();
}