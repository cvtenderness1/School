package com.project.platform.dto;

import lombok.Data;

/**
 * 微信小程序登录请求
 */
@Data
public class WxMinLoginDTO {
    private String code;
    private String encryptedData;
    private String iv;
}