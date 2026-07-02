package com.project.platform.controller;

import com.project.platform.config.JwtUtil;
import com.project.platform.config.Result;

import com.project.platform.entity.Admin;
import com.project.platform.service.AdminService;



import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CommonController {
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private AdminService adminService;

    /**
     * 登录
     */
    @PostMapping("login")
    public Result login(@RequestBody Admin admin) {
        Admin dbadmin = adminService.login(admin);
        // 1. 生成token
        String token = jwtUtil.generateToken(dbadmin.getId(), dbadmin.getUsername());

        // 2. 把用户信息和token一起返回给前端
        Map<String, Object> result = new HashMap<>();
        result.put("admin", dbadmin);
        result.put("token", token);
        return Result.success(result);
    }

    /**
     * 注册
     *
     */
    @PostMapping("register")
    public Result register(@RequestBody Admin admin) {
        adminService.register(admin);
        return Result.success();
    }

}