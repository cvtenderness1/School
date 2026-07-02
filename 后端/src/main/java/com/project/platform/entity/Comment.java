package com.project.platform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Integer commentId;
    private String orderId;
    private Integer userId;
    private Integer merchantId;
    private Integer riderId;
    private Integer score;
    private String content;
    private LocalDateTime createTime;
}