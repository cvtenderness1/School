package com.project.platform.entity;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BidStrategy {
    private Integer id;
    private Integer merchantId;
    private String name;
    private BigDecimal baseBid;
    private BigDecimal minBid;
    private BigDecimal maxBid;
    private Integer status;
    private Integer priority;
}