package com.project.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/register",
                        "/404",
                        "/**/avatar/**",
                        "/files/**",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.jpeg",
                        "/**/*.gif",
                        "/**/*.js",
                        "/**/*.css",
                        "/wx/register",
                        "/wx/login",
                        "/wx/login/wxMin",
                        "/wx/login/wxMin/simple",
                        "/rider/login",
                        "/rider/register",
                        "/rider/login/simple",
                        "/ocr/**",
                        "/error"
                );
    }
}