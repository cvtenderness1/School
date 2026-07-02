    package com.project.platform.entity;

    import lombok.Data;
    import java.math.BigDecimal;
    import java.util.Date;

    @Data
    public class OrderMaster {
        private String orderId;//子订单id
        private Integer userId;
        private Integer merchantId;
        private String addressId;
        private Integer orderType;        // 1-外卖 2-跑腿
        private BigDecimal totalAmount;
        private Integer orderStatus;      // 0待付款 1待接单 2待配送 3配送中 4已完成 5已取消
        private Integer deliveryId;//骑手id
        private BigDecimal deliveryFee;
        private String note;
        private Date createTime;
        private Date payTime;
        private Date finishTime;

        private Integer goodsId;          // 商品ID
        private Integer buyCount;         // 购买数量

        private String startAddress;  // 取件地址
        private String endAddress;    // 送达地址
        private String mainOrderNo;//主订单
        private Integer errandType;

        private BigDecimal postFee;      // 运费
        private BigDecimal payMoney;     // 实付金额 = 总价+运费

        private Integer payType;     // 1在线 2货到付款
        private Integer payChannel;  // 1支付宝 2微信
        private Integer deliveryTimeType; // 配送时间

        private String finishImg; // 送达凭证照片
    }