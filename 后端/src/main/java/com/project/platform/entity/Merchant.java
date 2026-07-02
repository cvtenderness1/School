package com.project.platform.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Merchant {
    private Integer merchantId;
    private Integer userId;
    private String merchantName;
    private String coverImg;
    private String address;
    private Double lat;  // 纬度
    private Double lng;  // 经度
    private BigDecimal score;
    private Integer status; // 1营业 0停业
    private Date createTime;
    private String alt; // 商家描述
    private String categoryName;
    private BigDecimal postFee;//运费
}