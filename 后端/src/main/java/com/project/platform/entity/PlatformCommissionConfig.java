package com.project.platform.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PlatformCommissionConfig {
    private Integer id;
    private Integer orderType;
    private BigDecimal commissionRate;
    private Date createTime;
    private Date updateTime;
}