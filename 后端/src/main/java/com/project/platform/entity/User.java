    package com.project.platform.entity;

    import lombok.Data;

    import java.time.LocalDateTime;
    /**
     * 用户信息表
     */
    @Data
    public class User {
        private Integer id;
        private String username;
        private String password;
        private String nickname;
        private String avatarUrl;
        private String tel;
        private String email;
        private String status;
        private LocalDateTime createTime;
        private String address;

        private String gender;        // 性别：男/女
        private String birthday;      // 生日：yyyy-MM-dd
        private String provinceCode; // 省份编码
        private String cityCode;     // 城市编码
        private String countyCode;   // 区县编码
    }
