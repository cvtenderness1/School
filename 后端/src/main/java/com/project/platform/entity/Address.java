package com.project.platform.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Address {
    private Integer id;
    private Integer userId;          // 用户ID
    private String receiver;         // 收货人
    private String contact;          // 手机号
    private String provinceCode;     // 省编码
    private String cityCode;         // 市编码
    private String countyCode;       // 区编码
    private String address;          // 详细地址
    private String fullLocation;     // 省市区文字
    private Integer isDefault;       // 1默认 0否
    private LocalDateTime createTime;
    private BigDecimal lat; // 收货地址 纬度
    private BigDecimal lng; // 收货地址 经度
}