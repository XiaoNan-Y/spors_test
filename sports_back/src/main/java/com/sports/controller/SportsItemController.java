package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.SportsItem;
import com.sports.service.SportsItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sports-items")
public class SportsItemController {

    @Autowired
    private SportsItemService sportsItemService;

    @GetMapping
    public Result list(@RequestParam(required = false) String keyword) {
        try {
            List<SportsItem> items = sportsItemService.list(keyword);
            return Result.success(items);
        } catch (Exception e) {
            return Result.error("获取体育项目列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/active")
    public Result getActiveItems() {
        try {
            List<SportsItem> items = sportsItemService.getAllActiveItems();
            return Result.success(items);
        } catch (Exception e) {
            return Result.error("获取活动项目列表失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result add(@RequestBody SportsItem sportsItem) {
        try {
            SportsItem saved = sportsItemService.save(sportsItem);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("添加体育项目失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody SportsItem sportsItem) {
        try {
            sportsItem.setId(id);
            sportsItemService.update(sportsItem);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("更新体育项目失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            sportsItemService.delete(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除体育项目失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        try {
            sportsItemService.updateStatus(id, isActive);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("更新状态失败：" + e.getMessage());
        }
    }
}