package com.sports.service;

import com.sports.common.Result;
import com.sports.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Result login(User user);
    Result getUserList(String userType, String keyword);
    Result addUser(User user);
    Result updateUser(User user);
    Result deleteUser(Long id);
    Result resetPassword(Long id);
    Result changePassword(Long userId, String oldPassword, String newPassword);
} 