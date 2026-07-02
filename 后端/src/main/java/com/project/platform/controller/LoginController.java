package com.project.platform.controller;

import cn.hutool.core.util.StrUtil;
import com.project.platform.config.JwtUtil;
import com.project.platform.config.Result;
import com.project.platform.dto.LoginDTO;
import com.project.platform.dto.WxMinLoginDTO;
import com.project.platform.dto.WxMinSimpleLoginDTO;
import com.project.platform.entity.User;
import com.project.platform.service.UserService;
import com.project.platform.vo.LoginVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wx")
public class LoginController {

    @Resource
    private UserService userService;

    @Resource
    private JwtUtil jwtUtil;

    // 账号密码登录
    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO dto) {
        if (StrUtil.isBlank(dto.getAccount()) || StrUtil.isBlank(dto.getPassword())) {
            return Result.error("账号或密码不能为空");
        }

        User user = userService.getByUsername(dto.getAccount());
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!dto.getPassword().equals(user.getPassword())) {
            return Result.error("密码错误");
        }

        LoginVO vo = buildLoginVO(user);
        return Result.success(vo);
    }

    // 小程序模拟快捷登录
    @PostMapping("/login/wxMin/simple")
    public Result wxMinSimple(@RequestBody WxMinSimpleLoginDTO dto) {
        String phone = dto.getPhoneNumber();
        if (StrUtil.isBlank(phone)) {
            return Result.error("手机号不能为空");
        }

        User dbUser = userService.getByTel(phone);
        if (dbUser == null) {
            dbUser = new User();
            dbUser.setUsername(phone);
            dbUser.setTel(phone);
            dbUser.setNickname("微信用户");
            userService.insert(dbUser);
        }

        LoginVO vo = buildLoginVO(dbUser);
        return Result.success(vo);
    }

    // 小程序正式登录
    @PostMapping("/login/wxMin")
    public Result wxMin(@RequestBody WxMinLoginDTO dto) {
        User user = new User();
        user.setTel("13800138000");
        User dbUser = userService.getByTel(user.getTel());

        if (dbUser == null) {
            dbUser = new User();
            dbUser.setUsername("wx_user");
            dbUser.setTel("13800138000");
            dbUser.setNickname("微信用户");
            userService.insert(dbUser);
        }

        LoginVO vo = buildLoginVO(dbUser);
        return Result.success(vo);
    }
    //用户注册（账号+密码）
    @PostMapping("/register")
    public Result register(@RequestBody LoginDTO dto) {
        // 1. 校验非空
        if (StrUtil.isBlank(dto.getAccount()) || StrUtil.isBlank(dto.getPassword())) {
            return Result.error("账号和密码不能为空");
        }

        // 2. 检查用户名是否已存在
        User existUser = userService.getByUsername(dto.getAccount());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }

        // 3. 创建用户
        User user = new User();
        user.setUsername(dto.getAccount());  // 用户名
        user.setPassword(dto.getPassword());// 密码
        user.setTel(dto.getAccount());       // 手机号默认账号
        userService.insert(user);

        // 4. 注册成功，直接返回登录信息（自动登录）
        LoginVO vo = buildLoginVO(user);
        return Result.success(vo);
    }
    //生成JWT Token
    private LoginVO buildLoginVO(User user) {
        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setMobile(user.getTel());
        vo.setToken(jwtUtil.generateToken(user.getId(), user.getUsername()));

        return vo;
    }
}