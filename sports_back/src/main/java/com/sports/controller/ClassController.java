package com.sports.controller;

import com.sports.common.Result;
import com.sports.repository.TestRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class ClassController {

    @Autowired
    private TestRecordRepository testRecordRepository;

    @GetMapping("/classes")
    public Result getClassList() {
        try {
            List<String> classNames = testRecordRepository.findDistinctClassNames();
            return Result.success(classNames);
        } catch (Exception e) {
            return Result.error("获取班级列表失败：" + e.getMessage());
        }
    }
} 