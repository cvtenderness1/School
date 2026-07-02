package com.project.platform.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class InvoiceApply {
    private Long id;
    private String invoiceNo;
    private Integer applyType;
    private Integer targetId;
    private String settleNo;
    private BigDecimal invoiceAmount;
    private String invoiceTitle;
    private String taxNo;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private String riderName;
    private String orderId;
}