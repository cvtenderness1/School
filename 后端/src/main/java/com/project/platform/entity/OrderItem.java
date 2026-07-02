package com.project.platform.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItem {
    private Integer itemId;
    private String orderId;
    private Integer goodsId;      // 商品ID
    private Integer skuId;       // + 规格ID（必须加）
    private String goodsName;
    private String skuAttrs;     // + 属性文字
    private String picture;      // + 图片
    private BigDecimal price;    // 原价
    private BigDecimal payPrice; // + 实付价
    private BigDecimal totalPrice; // + 小计
    private Integer quantity;    // 数量
}