package com.project.platform.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 购物车表
 */
@Data
public class Cart {
    private Integer id;
    private Integer userId;          // 用户ID
    private Integer skuId;           // SKU ID
    private Integer goodsId;         // 商品ID
    private String name;             // 商品名称
    private String picture;          // 商品图片
    private Integer count;            // 数量
    private Double price;             // 加入时价格
    private Double nowPrice;         // 当前价格
    private Integer stock;           // 库存
    private Boolean selected;        // 是否选中
    private String attrsText;        // 属性文字
    private Boolean isEffective;     // 是否有效商品
    private LocalDateTime createTime;// 创建时间
}