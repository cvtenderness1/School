package com.project.platform.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class good {
    private Integer goodsId;
    private Integer merchantId;
    private Integer categoryId;
    private String goodsName;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String img;
    private Integer stock;
    private Integer status;
    private String desc; //商品描述
}