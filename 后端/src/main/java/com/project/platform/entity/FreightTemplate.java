package com.project.platform.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FreightTemplate {
    private Integer id;
    private Integer merchantId;
    private String name;
    private BigDecimal basePrice;
    private Integer baseDistance;
    private BigDecimal extraPrice;
    private BigDecimal maxPrice;
    private LocalDateTime createTime;
}