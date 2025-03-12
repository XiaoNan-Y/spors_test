package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.SportsItem;
import com.sports.service.SportsItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sports-items")
@CrossOrigin
public class SportsItemController {

    private static final Logger log = LoggerFactory.getLogger(SportsItemController.class);

    @Autowired
    private SportsItemService sportsItemService;

    @GetMapping
    public Result list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<SportsItem> items = sportsItemService.findByKeyword(keyword, pageRequest);
            
            // 添加调试日志
            log.debug("Found {} items", items.getTotalElements());
            log.debug("Page content: {}", items.getContent());
            
            return Result.success(items);
        } catch (Exception e) {
            log.error("Failed to get sports items", e);
            return Result.error("获取体测项目列表失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result create(@RequestBody SportsItem sportsItem) {
        try {
            sportsItem.setId(null); // 确保是新建
            SportsItem saved = sportsItemService.save(sportsItem);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("创建体测项目失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody SportsItem sportsItem) {
        try {
            if (!sportsItemService.existsById(id)) {
                return Result.error("体测项目不存在");
            }
            sportsItem.setId(id);
            SportsItem updated = sportsItemService.save(sportsItem);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新体测项目失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            if (!sportsItemService.existsById(id)) {
                return Result.error("体测项目不存在");
            }
            sportsItemService.deleteById(id);
            Map<String, String> data = new HashMap<>();
            data.put("message", "删除成功");
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("删除体测项目失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestBody SportsItem sportsItem) {
        try {
            SportsItem existing = sportsItemService.findById(id)
                .orElseThrow(() -> new RuntimeException("体测项目不存在"));
            existing.setIsActive(sportsItem.getIsActive());
            SportsItem updated = sportsItemService.save(existing);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新状态失败：" + e.getMessage());
        }
    }
}