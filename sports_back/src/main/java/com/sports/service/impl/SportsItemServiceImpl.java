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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public boolean existsById(Long id) {
        return sportsItemRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        sportsItemRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SportsItemStandardDTO> getAllItemsWithStandards() {
        return sportsItemRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SportsItemStandardDTO getItemStandards(Long id) {
        SportsItem item = sportsItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("体育项目不存在"));
        
        if (!item.getIsActive()) {
            throw new RuntimeException("该体育项目当前不可用");
        }
        
        return convertToDTO(item);
    }

    private SportsItemStandardDTO convertToDTO(SportsItem item) {
        SportsItemStandardDTO dto = new SportsItemStandardDTO();
        BeanUtils.copyProperties(item, dto);
        
        // 设置评分标准列表
        List<SportsItemStandardDTO.StandardDetail> standards = new ArrayList<>();
        
        // 这里需要根据实际的数据结构来设置标准
        // 示例：从item的某些字段解析或者从关联表获取
        standards.add(createStandardDetail("A", "90-100", "优秀"));
        standards.add(createStandardDetail("B", "80-89", "良好"));
        standards.add(createStandardDetail("C", "70-79", "及格"));
        standards.add(createStandardDetail("D", "0-69", "不及格"));
        
        dto.setStandards(standards);
        return dto;
    }

    private SportsItemStandardDTO.StandardDetail createStandardDetail(
        String grade, String scoreRange, String requirement) {
        SportsItemStandardDTO.StandardDetail detail = new SportsItemStandardDTO.StandardDetail();
        detail.setGrade(grade);
        detail.setScoreRange(scoreRange);
        detail.setRequirement(requirement);
        return detail;
    }
} 