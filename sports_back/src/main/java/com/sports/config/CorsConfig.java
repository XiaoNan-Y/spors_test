package com.sports.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的来源，包括本地开发环境和您的特定IP
        config.addAllowedOrigin("http://localhost:8081");
        config.addAllowedOrigin("http://127.0.0.1:8081");
        config.addAllowedOrigin("http://10.91.4.89:8081");
        
        // 允许凭证
        config.setAllowCredentials(true);
        
        // 允许的HTTP方法
        config.addAllowedMethod("*");
        
        // 允许的头信息
        config.addAllowedHeader("*");
        
        // 暴露的头信息
        config.addExposedHeader("Authorization");
        
        // 预检请求的有效期
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 