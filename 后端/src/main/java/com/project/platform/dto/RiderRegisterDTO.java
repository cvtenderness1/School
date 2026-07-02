package com.project.platform.dto;

import lombok.Data;

@Data
public class RiderRegisterDTO {
    private String account;
    private String password;
    private String name;
    private String phone;
    private String studentId;
}