package com.sports.service.impl;

import com.sports.common.Result;
import com.sports.entity.User;
import com.sports.repository.UserRepository;
import com.sports.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public Page<User> getUsers(String userType, String keyword, Pageable pageable) {
        try {
            log.info("Fetching users with type: {}, keyword: {}", userType, keyword);
            Page<User> users;
            if (keyword != null && !keyword.trim().isEmpty()) {
                users = userRepository.findByUserTypeAndKeyword(userType, keyword.trim(), pageable);
            } else {
                users = userRepository.findByUserType(userType, pageable);
            }
            log.info("Found {} users", users.getTotalElements());
            return users;
        } catch (Exception e) {
            log.error("Error fetching users", e);
            throw new RuntimeException("获取用户列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result login(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser == null) {
            return Result.error("用户不存在");
        }
        
        // TODO: 这里应该使用加密后的密码比较，暂时使用明文比较
        if (!existingUser.getPassword().equals(user.getPassword())) {
            return Result.error("密码错误");
        }
        
        // 登录成功，清除密码后返回用户信息
        existingUser.setPassword(null);
        return Result.success(existingUser);
    }
    
    @Override
    public Result changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 验证旧密码
            if (!user.getPassword().equals(oldPassword)) {
                return Result.error("原密码错误");
            }
            
            // 更新密码
            user.setPassword(newPassword);
            userRepository.save(user);
            
            return Result.success(null);
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error("修改密码失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result addUser(User user) {
        try {
            // 检查用户名是否已存在
            User existingUser = userRepository.findByUsername(user.getUsername());
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }
            
            // 设置默认密码
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                user.setPassword("123456"); // 默认密码
            }
            
            // 验证学生用户必须填写学号
            if ("STUDENT".equals(user.getUserType()) && 
                (user.getStudentNumber() == null || user.getStudentNumber().trim().isEmpty())) {
                return Result.error("学生用户必须填写学号");
            }
            
            // 保存用户
            User savedUser = userRepository.save(user);
            log.info("Successfully added user: {}", savedUser.getUsername());
            return Result.success(savedUser);
            
        } catch (Exception e) {
            log.error("Error adding user: ", e);
            throw new RuntimeException("添加用户失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result updateUser(User user) {
        try {
            User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
                
            existingUser.setRealName(user.getRealName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            
            // 更新学号（仅学生用户）
            if ("STUDENT".equals(existingUser.getUserType())) {
                existingUser.setStudentNumber(user.getStudentNumber());
            }
            
            userRepository.save(existingUser);
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.error("更新用户失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result deleteUser(Long id) {
        try {
            userRepository.deleteById(id);
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.error("删除用户失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result resetPassword(Long id) {
        try {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            // TODO: 实现密码重置
            user.setPassword("123456"); // 设置默认密码
            userRepository.save(user);
            return Result.success(null);
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return Result.error("重置密码失败：" + e.getMessage());
        }
    }

    @Override
    public Page<User> getUsersByType(String userType, Pageable pageable) {
        try {
            return userRepository.findByUserType(userType, pageable);
        } catch (Exception e) {
            throw new RuntimeException("获取用户列表失败：" + e.getMessage());
        }
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    @Override
    @Transactional
    public Result updateProfile(User user) {
        try {
            User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 只更新允许修改的字段
            existingUser.setRealName(user.getRealName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            
            userRepository.save(existingUser);
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新个人信息失败", e);
            return Result.error("更新个人信息失败：" + e.getMessage());
        }
    }
} 