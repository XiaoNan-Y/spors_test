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
        String userIdStr = request.getHeader("userId");
        log.info("Received userId header: {}", userIdStr);
        
        // 检查 userIdStr 是否为 null 或 "null" 字符串
        if (userIdStr != null && !userIdStr.equals("null") && !userIdStr.trim().isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdStr);
                request.setAttribute("userId", userId);
                log.info("Set userId attribute: {}", userId);
            } catch (NumberFormatException e) {
                log.error("Invalid userId format: {}", userIdStr, e);
                // 不设置 userId 属性，让控制器处理
            }
        } else {
            log.warn("No valid userId provided in request header");
            // 可以选择设置一个默认值或者不设置
            // request.setAttribute("userId", null);
        }
        
        return true;
    }
} 