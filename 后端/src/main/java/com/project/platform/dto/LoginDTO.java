package com.project.platform.dto;

import lombok.Data;

/**
 * 账号密码登录请求
 */
@Data
public class LoginDTO {
    private String account;
    private String password;
}