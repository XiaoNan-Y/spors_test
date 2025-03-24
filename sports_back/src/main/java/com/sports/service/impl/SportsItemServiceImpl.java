package com.sports.service.impl;

import com.sports.dto.SportsItemStandardDTO;
import com.sports.entity.SportsItem;
import com.sports.repository.SportsItemRepository;
import com.sports.service.SportsItemService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SportsItemServiceImpl implements SportsItemService {
    
    private static final Logger log = LoggerFactory.getLogger(SportsItemServiceImpl.class);

    @Autowired
    private SportsItemRepository sportsItemRepository;

    @Override
    public List<SportsItem> findByIsActiveTrue() {
        return sportsItemRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional
    public SportsItem save(SportsItem sportsItem) {
        return sportsItemRepository.save(sportsItem);
    }

    @Override
    public boolean existsById(Long id) {
        return sportsItemRepository.existsById(id);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        try {
            sportsItemRepository.deleteById(id);
        } catch (Exception e) {
            log.error("删除体育项目失败", e);
            throw new RuntimeException("删除体育项目失败: " + e.getMessage());
        }
    }

    @Override
    public Optional<SportsItem> findById(Long id) {
        return sportsItemRepository.findById(id);
    }

    @Override
    public List<SportsItem> findActiveItems() {
        return sportsItemRepository.findByIsActiveTrue();
    }

    @Override
    public SportsItemStandardDTO getItemStandards(Long id) {
        SportsItem item = sportsItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("体育项目不存在"));
            
        SportsItemStandardDTO dto = new SportsItemStandardDTO();
        BeanUtils.copyProperties(item, dto);
        return dto;
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
    @Transactional(readOnly = true)
    public List<SportsItemStandardDTO> getAllItemsWithStandards() {
        return sportsItemRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    private SportsItemStandardDTO convertToDTO(SportsItem item) {
        SportsItemStandardDTO dto = new SportsItemStandardDTO();
        BeanUtils.copyProperties(item, dto);
        return dto;
    }

    @Override
    public SportsItem update(SportsItem sportsItem) {
        return sportsItemRepository.save(sportsItem);
    }
} 