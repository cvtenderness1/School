package com.project.platform.controller;

import com.project.platform.config.Result;
import com.project.platform.entity.OrderMaster;
import com.project.platform.mapper.OrderMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Resource
    private OrderMapper orderMapper;

    /**
     * 1. 模拟支付接口：直接修改数据库状态
     */
    @GetMapping("/mock")
    public Result mockPay(@RequestParam String orderId) {
        // 更新订单状态为 1 (待接单)
        updateOrderStatus(orderId, 1);
        return Result.success("支付成功");
    }

    /**
     * 2. 微信支付申请接口
     */
    @GetMapping("/wxPay/miniPay")
    public Result wxPay(@RequestParam String orderId) {
        // 在真实项目中，这里应该调用微信商户平台 API 获取真正的 prepay_id
        Map<String, String> payParams = new HashMap<>();
        payParams.put("appId", "wx123456789");
        payParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        payParams.put("nonceStr", "random_string_123");
        payParams.put("package", "prepay_id=mock_id_999");
        payParams.put("signType", "MD5");
        payParams.put("paySign", "mock_signature_abc");

        // 模拟支付成功，提前更新数据库状态
        updateOrderStatus(orderId, 1);

        return Result.success(payParams);
    }

    // 抽离公共的更新状态方法
    // 抽离公共的更新状态方法（修复版，兼容所有订单号格式）
    private void updateOrderStatus(String orderId, Integer status) {
        // 1. 先从订单号里提取主订单号（兼容带_的子订单号）
        String mainOrderNo = orderId.contains("_") ? orderId.split("_")[0] : orderId;

        // 2. 用主订单号查询所有子订单（用你自己写的 selectBymin 方法，保证能查到）
        List<OrderMaster> orders = orderMapper.selectBymin(mainOrderNo);

        if (orders != null && !orders.isEmpty()) {
            for (OrderMaster order : orders) {
                order.setOrderStatus(status);
                order.setPayTime(new Date());
                orderMapper.updateById(order);
            }
        }
    }
}