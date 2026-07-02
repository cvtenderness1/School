package com.project.platform.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Coupon {
    private Integer id;
    private Integer merchantId;
    private String name;
    private Integer type; //1满减 2折扣
    private BigDecimal minAmount;
    private BigDecimal discount;
    private BigDecimal reducePrice;
    private Integer total;
    private Integer used;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}