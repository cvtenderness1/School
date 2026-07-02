package com.project.platform.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GoodsSku {
    private Integer id;
    private Integer goodsId;
    private String skuNameArr; // 逗号分隔的规格值，如"红色,XL"
    private String image;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stock;
}