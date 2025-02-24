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
    public Result list(@RequestParam(required = false) String keyword) {
        List<SportsItem> items = sportsItemService.list(keyword);
        return Result.success(items);
    }

    @PostMapping("/add")
    public Result add(@RequestBody SportsItem sportsItem) {
        sportsItemService.add(sportsItem);
        return Result.success(null);
    }

    @PutMapping("/update")
    public Result update(@RequestBody SportsItem sportsItem) {
        sportsItemService.update(sportsItem);
        return Result.success(null);
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