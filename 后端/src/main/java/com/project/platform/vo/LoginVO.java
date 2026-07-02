package com.project.platform.vo;

import lombok.Data;

/**
 * 登录返回结果
 */
@Data
public class LoginVO {
    private Integer id;
    private String avatarUrl;
    private String username;
    private String nickname;
    private String mobile; // 手机号
    private String token;  // 登录凭证
}