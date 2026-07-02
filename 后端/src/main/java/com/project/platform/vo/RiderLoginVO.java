package com.project.platform.vo;
import lombok.Data;

@Data
public class RiderLoginVO {
    private Integer riderId;
    private String account;
    private String name;
    private String phone;
    private String studentId;
    private Integer status;
    private String token;
}