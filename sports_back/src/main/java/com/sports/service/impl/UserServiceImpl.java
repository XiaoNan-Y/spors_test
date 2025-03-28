package com.sports.service.impl;

import com.sports.common.Result;
import com.sports.entity.User;
import com.sports.repository.UserRepository;
import com.sports.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
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
    public Result login(User loginUser) {
        log.info("用户尝试登录 - username: {}", loginUser.getUsername());
        
        User user = userRepository.findByUsername(loginUser.getUsername());
        if (user == null) {
            log.warn("登录失败 - 用户不存在: {}", loginUser.getUsername());
            return Result.error("用户不存在");
        }
        
        log.info("找到用户 - id: {}, username: {}, userType: {}, className: {}", 
            user.getId(), user.getUsername(), user.getUserType(), user.getClassName());
        
        if (!loginUser.getPassword().equals(user.getPassword())) {
            log.warn("登录失败 - 密码错误: username={}", loginUser.getUsername());
            return Result.error("用户名或密码错误");
        }
        
        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("userType", user.getUserType());
        data.put("realName", user.getRealName());
        data.put("className", user.getClassName());
        data.put("token", generateToken(user));  // 生成新的token
        
        log.info("用户登录成功 - id: {}, username: {}, userType: {}", 
            user.getId(), user.getUsername(), user.getUserType());
        
        return Result.success(data);
    }
    
    private String generateToken(User user) {
        // 这里应该实现真正的token生成逻辑
        return "token_" + user.getId() + "_" + System.currentTimeMillis();
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
            log.info("开始添加用户，用户信息：username={}, userType={}, realName={}, studentNumber={}", 
                user.getUsername(), user.getUserType(), user.getRealName(), user.getStudentNumber());

            // 基本字段验证
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }

            // 检查用户名是否已存在
            User existingUser = userRepository.findByUsername(user.getUsername());
            if (existingUser != null) {
                log.warn("用户名已存在：{}", user.getUsername());
                return Result.error("用户名已存在");
            }
            
            // 设置默认密码
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                user.setPassword("123456"); // 默认密码
                log.info("用户未设置密码，使用默认密码");
            }
            
            // 验证用户类型
            if (user.getUserType() == null || user.getUserType().trim().isEmpty()) {
                log.warn("用户类型为空");
                return Result.error("用户类型不能为空");
            }

            // 验证学生用户必须填写学号
            if (User.TYPE_STUDENT.equals(user.getUserType())) {
                if (user.getStudentNumber() == null || user.getStudentNumber().trim().isEmpty()) {
                    log.warn("学生用户未填写学号");
                    return Result.error("学生用户必须填写学号");
                }
            }
            
            // 保存用户
            try {
                User savedUser = userRepository.save(user);
                log.info("用户添加成功：id={}, username={}", savedUser.getId(), savedUser.getUsername());
                return Result.success(savedUser);
            } catch (Exception e) {
                log.error("保存用户到数据库失败：{}", e.getMessage(), e);
                throw e;
            }
            
        } catch (Exception e) {
            log.error("添加用户过程中发生异常：{}", e.getMessage(), e);
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