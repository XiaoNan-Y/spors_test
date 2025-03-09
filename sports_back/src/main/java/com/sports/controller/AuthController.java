package com.sports.controller;

import com.sports.dto.ChangePasswordDTO;
import com.sports.entity.User;
import com.sports.service.UserService;
import com.sports.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class AuthController {
    
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        return userService.login(user);
    }

    @PutMapping("/change-password")
    public Result changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        return userService.changePassword(
            changePasswordDTO.getUserId(),
            changePasswordDTO.getOldPassword(),
            changePasswordDTO.getNewPassword()
        );
    }
} 