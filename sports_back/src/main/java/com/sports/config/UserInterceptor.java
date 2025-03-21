package com.sports.config;

import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(UserInterceptor.class);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头中获取用户ID
        String userId = request.getHeader("X-User-ID");
        if (userId != null) {
            request.setAttribute("userId", Long.parseLong(userId));
            return true;
        }
        
        // 开发测试时，可以使用固定用户ID
        request.setAttribute("userId", 3L);
        return true;
    }
} 