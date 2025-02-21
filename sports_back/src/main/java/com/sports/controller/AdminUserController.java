package com.sports.controller;

import com.sports.common.Result;
import com.sports.entity.User;
import com.sports.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/users")
@Slf4j
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result getUserList(
        @RequestParam String userType,
        @RequestParam(required = false) String keyword
    ) {
        log.debug("Getting user list for type: {}, keyword: {}", userType, keyword);
        return userService.getUserList(userType, keyword);
    }

    @PostMapping("/add")
    public Result addUser(@RequestBody User user) {
        log.debug("Adding new user: {}", user);
        return userService.addUser(user);
    }

    @PostMapping("/update")
    public Result updateUser(@RequestBody User user) {
        log.debug("Updating user: {}", user);
        return userService.updateUser(user);
    }

    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Long id) {
        log.debug("Deleting user with id: {}", id);
        return userService.deleteUser(id);
    }

    @PostMapping("/{id}/reset-password")
    public Result resetPassword(@PathVariable Long id) {
        log.debug("Resetting password for user id: {}", id);
        return userService.resetPassword(id);
    }
} 