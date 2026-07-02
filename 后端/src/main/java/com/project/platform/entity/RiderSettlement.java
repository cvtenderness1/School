package com.project.platform.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class RiderSettlement {
    private Long id;
    private String settleNo;
    private Integer riderId;
    private Date settleStart;
    private Date settleEnd;
    private Integer totalOrderNum;
    private BigDecimal totalOrderAmount;
    private BigDecimal platformCommission;
    private BigDecimal riderCommission;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private String riderName;
}