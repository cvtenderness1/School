package com.project.platform.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class RiderBalanceFlow {
    private Long id;
    private Integer riderId;
    private String settleNo;
    private Integer flowType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String remark;
    private Date createTime;
}