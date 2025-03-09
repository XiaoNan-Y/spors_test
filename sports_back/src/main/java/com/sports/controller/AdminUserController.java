package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.User;
import com.sports.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public Result getUsers(
        @RequestParam(required = false) String userType,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        try {
            Page<User> users = userService.getUsers(userType, keyword, PageRequest.of(page, size));
            return Result.success(users);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/users")
    public Result addUser(@RequestBody User user) {
        try {
            log.info("Adding new user: {}", user);
            return userService.addUser(user);
        } catch (Exception e) {
            log.error("添加用户失败", e);
            return Result.error("添加用户失败：" + e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public Result updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            log.info("Updating user with id: {}, data: {}", id, user);
            user.setId(id);
            return userService.updateUser(user);
        } catch (Exception e) {
            log.error("Error updating user: ", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public Result deleteUser(@PathVariable Long id) {
        try {
            return userService.deleteUser(id);
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.error("删除用户失败：" + e.getMessage());
        }
    }

    @PutMapping("/users/{id}/reset-password")
    public Result resetPassword(@PathVariable Long id) {
        try {
            return userService.resetPassword(id);
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return Result.error("重置密码失败：" + e.getMessage());
        }
    }
} 