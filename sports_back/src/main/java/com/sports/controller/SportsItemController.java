package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.SportsItem;
import com.sports.service.SportsItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sports-items")
public class SportsItemController {

    @Autowired
    private SportsItemService sportsItemService;

    @GetMapping
    public Result getSportsItems(@RequestParam(required = false) String keyword) {
        try {
            List<SportsItem> items;
            if (keyword != null && !keyword.isEmpty()) {
                // 搜索时获取所有匹配的项目
                items = sportsItemService.list(keyword);
            } else {
                // 默认只获取激活状态的项目
                items = sportsItemService.findByIsActiveTrue();
            }
            return Result.success(items);
        } catch (Exception e) {
            return Result.error("获取体测项目列表失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result add(@RequestBody SportsItem sportsItem) {
        sportsItemService.add(sportsItem);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody SportsItem sportsItem) {
        try {
            sportsItem.setId(id);
            sportsItemService.update(sportsItem);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        sportsItemService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestBody SportsItem sportsItem) {
        sportsItemService.updateStatus(id, sportsItem.getIsActive());
        return Result.success(null);
    }
}