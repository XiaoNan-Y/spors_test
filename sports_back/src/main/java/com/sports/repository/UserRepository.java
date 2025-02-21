package com.sports.repository;

import com.sports.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    // 根据用户名查找用户
    User findByUsername(String username);
    
    // 根据用户类型查找用户
    List<User> findByUserType(String userType);
    
    // 根据用户类型统计用户数量
    long countByUserType(String userType);
    
    // 根据用户类型和关键字搜索用户
    @Query("SELECT u FROM User u WHERE u.userType = :userType " +
           "AND (u.username LIKE %:keyword% OR u.realName LIKE %:keyword%)")
    List<User> findByUserTypeAndUsernameContainingOrRealNameContaining(
        @Param("userType") String userType,
        @Param("keyword") String usernameKeyword,
        @Param("keyword") String realNameKeyword
    );
} 