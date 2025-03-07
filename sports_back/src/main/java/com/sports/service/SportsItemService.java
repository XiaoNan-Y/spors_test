package com.sports.service;

import com.sports.entity.SportsItem;
import com.sports.repository.SportsItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SportsItemService {

    @Autowired
    private SportsItemRepository sportsItemRepository;

    public List<SportsItem> list(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return sportsItemRepository.findByNameContaining(keyword);
        }
        return sportsItemRepository.findAll();
    }

    public void add(SportsItem sportsItem) {
        sportsItemRepository.save(sportsItem);
    }

    @Transactional
    public void update(SportsItem sportsItem) {
        SportsItem existingItem = sportsItemRepository.findById(sportsItem.getId())
            .orElseThrow(() -> new RuntimeException("项目不存在"));
        
        // 更新字段
        existingItem.setName(sportsItem.getName());
        existingItem.setDescription(sportsItem.getDescription());
        existingItem.setUnit(sportsItem.getUnit());
        existingItem.setType(sportsItem.getType());
        existingItem.setIsActive(sportsItem.getIsActive());
        
        sportsItemRepository.save(existingItem);
    }

    public void delete(Long id) {
        sportsItemRepository.deleteById(id);
    }

    public void updateStatus(Long id, Boolean isActive) {
        SportsItem item = sportsItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("项目不存在"));
        item.setIsActive(isActive);
        sportsItemRepository.save(item);
    }
}