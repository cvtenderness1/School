package com.project.platform.entity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Rider {
    private Integer riderId;
    private String account;
    private String password;
    private String name;
    private String phone;
    private String studentId;
    private Integer status;//认证状态
    private LocalDateTime createTime;
    private BigDecimal balance; // 骑手余额/工资
    private BigDecimal lat;   // 纬度
    private BigDecimal lng;   // 经度

    // 工作状态 0=休息 1=在线
    private Integer workStatus;

    // 今日已接单数
    private Integer todayOrderCount;

    // 每日最大接单量
    private Integer dailyOrderLimit;

    // 信用分 100满分
    private Integer creditScore;

    // 是否冻结 0=正常 1=冻结
    private Integer frozen;
}