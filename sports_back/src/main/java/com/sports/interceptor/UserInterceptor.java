package com.sports.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Enumeration;
import com.sports.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.sports.entity.User;

public class UserInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(UserInterceptor.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 打印所有请求头，帮助调试
        log.info("UserInterceptor - 请求URI: {}", request.getRequestURI());
        log.info("UserInterceptor - 请求方法: {}", request.getMethod());
        
        Enumeration<String> headerNames = request.getHeaderNames();
        log.info("UserInterceptor - 所有请求头:");
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.info("  {} = {}", headerName, request.getHeader(headerName));
        }
        
        // 获取请求头中的用户ID
        String userIdStr = request.getHeader("userId");
        log.info("UserInterceptor - 接收到请求，userId from header: {}", userIdStr);
        
        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                
                // 验证用户是否存在
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
                
                log.info("UserInterceptor - 找到用户: id={}, username={}, realName={}", 
                    user.getId(), user.getUsername(), user.getRealName());
                
                // 将用户ID设置为请求属性
                request.setAttribute("userId", userId);
                log.info("UserInterceptor - 设置请求属性 userId: {}", userId);
                return true;
            } catch (NumberFormatException e) {
                log.error("UserInterceptor - userId格式错误: {}", userIdStr);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            } catch (Exception e) {
                log.error("UserInterceptor - 验证用户失败: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        }
        
        log.warn("UserInterceptor - 请求头中未找到userId");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
} 