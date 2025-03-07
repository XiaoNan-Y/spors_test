package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.SportsItem;
import com.sports.service.SportsItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class SportsItemController {

    @Autowired
    private SportsItemService sportsItemService;

    @GetMapping("/sports-items")
    public Result getSportsItems(@RequestParam(required = false) String keyword) {
        List<SportsItem> items = sportsItemService.list(keyword);
        return Result.success(items);
    }

    @PostMapping("/sports-items")
    public Result add(@RequestBody SportsItem sportsItem) {
        sportsItemService.add(sportsItem);
        return Result.success(null);
    }

    @PutMapping("/sports-items/{id}")
    public Result update(@PathVariable Long id, @RequestBody SportsItem sportsItem) {
        try {
            sportsItem.setId(id);
            sportsItemService.update(sportsItem);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/sports-items/{id}")
    public Result delete(@PathVariable Long id) {
        sportsItemService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/sports-items/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestBody SportsItem sportsItem) {
        sportsItemService.updateStatus(id, sportsItem.getIsActive());
        return Result.success(null);
    }
}