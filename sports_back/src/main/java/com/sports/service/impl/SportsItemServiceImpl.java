package com.sports.service.impl;

import com.sports.entity.SportsItem;
import com.sports.repository.SportsItemRepository;
import com.sports.service.SportsItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SportsItemServiceImpl implements SportsItemService {
    
    @Autowired
    private SportsItemRepository sportsItemRepository;

    @Override
    public SportsItem save(SportsItem sportsItem) {
        return sportsItemRepository.save(sportsItem);
    }

    @Override
    public void delete(Long id) {
        sportsItemRepository.deleteById(id);
    }

    @Override
    public SportsItem update(SportsItem sportsItem) {
        return sportsItemRepository.save(sportsItem);
    }

    @Override
    public Optional<SportsItem> findById(Long id) {
        return sportsItemRepository.findById(id);
    }

    @Override
    public List<SportsItem> findAll() {
        return sportsItemRepository.findAll();
    }

    @Override
    public Page<SportsItem> findByKeyword(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return sportsItemRepository.findAll(pageable);
        }
        return sportsItemRepository.findByKeyword(keyword, pageable);
    }

    @Override
    public List<SportsItem> findActiveItems() {
        return sportsItemRepository.findByIsActiveTrue();
    }

    @Override
    public boolean updateStatus(Long id, boolean isActive) {
        Optional<SportsItem> optionalItem = sportsItemRepository.findById(id);
        if (optionalItem.isPresent()) {
            SportsItem item = optionalItem.get();
            item.setIsActive(isActive);
            sportsItemRepository.save(item);
            return true;
        }
        return false;
    }

    @Override
    public List<SportsItem> getAllActiveItems() {
        return sportsItemRepository.findByIsActiveTrue();
    }

    @Override
    public boolean existsById(Long id) {
        return sportsItemRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        sportsItemRepository.deleteById(id);
    }
} 