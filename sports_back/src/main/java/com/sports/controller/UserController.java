package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.User;
import com.sports.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/students")
    public Result getStudents(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            log.info("Fetching students list with pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);
            Page<User> students = userService.getUsers("STUDENT", keyword, PageRequest.of(pageNum - 1, pageSize));
            log.info("Found {} students", students.getTotalElements());
            return Result.success(students);
        } catch (Exception e) {
            log.error("Failed to get students list", e);
            return Result.error("获取学生列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/teachers")
    public Result getTeachers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            log.info("Fetching teachers list with pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);
            Page<User> teachers = userService.getUsers("TEACHER", keyword, PageRequest.of(pageNum - 1, pageSize));
            log.info("Found {} teachers", teachers.getTotalElements());
            return Result.success(teachers);
        } catch (Exception e) {
            log.error("Failed to get teachers list", e);
            return Result.error("获取教师列表失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result createUser(@RequestBody User user) {
        try {
            log.info("Creating new user: {}", user.getUsername());
            return userService.addUser(user);
        } catch (Exception e) {
            log.error("Failed to create user", e);
            return Result.error("创建用户失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Long id) {
        try {
            log.info("Deleting user with id: {}", id);
            return userService.deleteUser(id);
        } catch (Exception e) {
            log.error("Failed to delete user", e);
            return Result.error("删除用户失败：" + e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        try {
            log.info("User trying to login: {}", user.getUsername());
            return userService.login(user);
        } catch (Exception e) {
            log.error("Login failed", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            log.info("Updating user with id: {}, data: {}", id, user);
            user.setId(id);
            return userService.updateUser(user);
        } catch (Exception e) {
            log.error("Failed to update user", e);
            return Result.error("更新用户失败：" + e.getMessage());
        }
    }

    // 获取个人信息
    @GetMapping("/profile")
    public Result getProfile() {
        try {
            // 从当前登录用户中获取用户ID
            // 这里暂时从localStorage中获取，之后可以改用Spring Security
            Long userId = 1L; // 测试用，实际应该从认证信息中获取
            User user = userService.getUserById(userId);
            if (user != null) {
                // 清除敏感信息
                user.setPassword(null);
                return Result.success(user);
            }
            return Result.error("用户不存在");
        } catch (Exception e) {
            log.error("获取个人信息失败", e);
            return Result.error("获取个人信息失败：" + e.getMessage());
        }
    }

    // 更新个人信息
    @PutMapping("/profile")
    public Result updateProfile(@RequestBody User user) {
        try {
            // 从当前登录用户中获取用户ID
            Long userId = 1L; // 测试用，实际应该从认证信息中获取
            user.setId(userId);
            return userService.updateProfile(user);
        } catch (Exception e) {
            log.error("更新个人信息失败", e);
            return Result.error("更新个人信息失败：" + e.getMessage());
        }
    }

    @PutMapping("/change-password")
    public Result changePassword(@RequestBody Map<String, String> params) {
        try {
            // 从当前登录用户中获取用户ID
            Long userId = 1L; // 测试用，实际应该从认证信息中获取
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return Result.error("密码不能为空");
            }
            
            return userService.changePassword(userId, oldPassword, newPassword);
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error("修改密码失败：" + e.getMessage());
        }
    }
} 