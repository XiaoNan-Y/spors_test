package com.sports.service.impl;

import com.sports.entity.User;
import com.sports.repository.UserRepository;
import com.sports.service.UserService;
import com.sports.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Transactional(readOnly = true)  // 默认只读事务
@Slf4j  // 添加 Lombok 日志注解
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Result login(User user) {
        log.debug("Attempting login for username: {}", user.getUsername());
        User dbUser = userRepository.findByUsername(user.getUsername());
        if (dbUser == null) {
            log.debug("User not found: {}", user.getUsername());
            return Result.error("用户不存在");
        }
        log.debug("Found user: {}", dbUser);
        if (!dbUser.getPassword().equals(user.getPassword())) {
            return Result.error("密码错误");
        }
        return Result.success(dbUser);
    }

    @Override
    public Result getUserList(String userType, String keyword) {
        List<User> users;
        if (StringUtils.hasText(keyword)) {
            users = userRepository.findByUserTypeAndUsernameContainingOrRealNameContaining(
                userType, keyword, keyword);
        } else {
            users = userRepository.findByUserType(userType);
        }
        return Result.success(users);
    }

    @Override
    @Transactional  // 添加事务注解
    public Result addUser(User user) {
        log.debug("Adding new user: {}", user);  // 添加日志
        try {
            // 检查用户名是否已存在
            if (userRepository.findByUsername(user.getUsername()) != null) {
                return Result.error("用户名已存在");
            }
            
            // 设置默认密码
            user.setPassword("123456");
            
            // 保存用户
            User savedUser = userRepository.save(user);
            log.debug("User saved successfully: {}", savedUser);
            
            return Result.success("添加成功");
        } catch (Exception e) {
            log.error("Error adding user: ", e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateUser(User user) {
        User existingUser = userRepository.findById(user.getId()).orElse(null);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }
        // 如果修改了用户名，需要检查新用户名是否已存在
        if (!existingUser.getUsername().equals(user.getUsername()) &&
            userRepository.findByUsername(user.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        // 保持原密码不变
        user.setPassword(existingUser.getPassword());
        userRepository.save(user);
        return Result.success("更新成功");
    }

    @Override
    public Result deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return Result.error("用户不存在");
        }
        userRepository.deleteById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result resetPassword(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword("123456");
        userRepository.save(user);
        return Result.success("密码重置成功");
    }

    @Override
    public Result changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 验证旧密码
        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("原密码错误");
        }
        
        // 更新密码
        user.setPassword(newPassword);
        userRepository.save(user);
        return Result.success("密码修改成功");
    }
} 