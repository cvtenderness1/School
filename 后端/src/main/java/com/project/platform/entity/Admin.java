package com.project.platform.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员信息表
 */
@Data
public class Admin {
    private Integer id;
    private Integer age;
    private String username;
    private String password;
    private String nickname;
    private String avatarUrl;
    private String tel;
    private String email;
    private String roles;
    private String status;
    private String address;
    private LocalDateTime createTime;
}
