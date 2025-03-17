package com.sports.repository;

import com.sports.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
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

    @Query("SELECT u FROM User u WHERE u.userType = :userType " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "u.realName LIKE %:keyword% OR " +
           "u.username LIKE %:keyword% OR " +
           "(u.studentNumber LIKE %:keyword% AND u.userType = 'STUDENT'))")
    Page<User> findByUserTypeAndKeyword(
        @Param("userType") String userType,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    Page<User> findByUserType(String userType, Pageable pageable);

    // 根据学号查找用户
    User findByStudentNumber(String studentNumber);
    
    // 检查学号是否已存在
    boolean existsByStudentNumber(String studentNumber);
    
    // 获取所有不重复的班级名称
    @Query("SELECT DISTINCT u.className FROM User u WHERE u.className IS NOT NULL ORDER BY u.className")
    List<String> findDistinctClassName();
}