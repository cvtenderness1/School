package com.project.platform.entity;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PeakPrice {
    private Integer id;
    private Integer merchantId;
    private String name;
    private String timeRange;
    private String weekDays;
    private BigDecimal rate;
    private Integer status;
}