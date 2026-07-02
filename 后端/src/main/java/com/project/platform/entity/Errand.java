package com.project.platform.entity;

import lombok.Data;

@Data
public class Errand {
    private Integer errandId;
    private String orderId;
    private Integer userId;
    private Integer deliveryId;
    private Integer errandType; // 1代取 2代买 3代送 4其他
    private String startAddress;
    private String endAddress;
    private String remark;
    private Integer status;

}