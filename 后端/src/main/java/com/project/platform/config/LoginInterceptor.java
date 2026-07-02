package com.project.platform.config;

import com.project.platform.exception.CustomException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.annotation.Resource;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");

        if (token == null || token.trim().isEmpty()) {
            throw new CustomException("请先登录");
        }

        Claims claims = jwtUtil.parseToken(token);
        if (claims == null) {
            throw new CustomException("登录已过期，请重新登录");
        }

        request.setAttribute("adminId", claims.get("adminId"));
        request.setAttribute("username", claims.get("username"));
        return true;
    }
}