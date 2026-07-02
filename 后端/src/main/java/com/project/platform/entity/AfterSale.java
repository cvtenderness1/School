package com.project.platform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AfterSale {
    private Integer id;
    private String orderId;
    private Integer userId;
    private Integer merchantId;
    private Integer riderId;
    private Integer type; // 1取消/改单 2退款 3投诉差评 4发票申请
    private String reason;
    private String imgUrls;
    private Integer status; // 0待处理 1处理中 2已完成 3已驳回
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}