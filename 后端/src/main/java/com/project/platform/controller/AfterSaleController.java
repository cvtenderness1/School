package com.project.platform.controller;

import com.project.platform.config.Result;
import com.project.platform.entity.AfterSale;
import com.project.platform.entity.OrderMaster;
import com.project.platform.mapper.OrderMapper;
import com.project.platform.service.AfterSaleService;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/afterSale")
public class AfterSaleController {

    @Resource
    private AfterSaleService afterSaleService;

    @Resource
    private OrderMapper orderMasterMapper;

    // 用户提交售后申请
    @PostMapping("/submit")
    public Result submitAfterSale(@RequestBody AfterSale afterSale) {
        // 根据主订单号查询所有子订单
        List<OrderMaster> orderList = orderMasterMapper.selectBymin(afterSale.getOrderId());

        // 1. 校验订单是否存在
        if (orderList == null || orderList.isEmpty()) {
            return Result.error("订单不存在");
        }

        // 2. 取第一个子订单来填充用户、商家、骑手信息（主/子订单的这些信息是一致的）
        OrderMaster order = orderList.get(0);
        afterSale.setUserId(order.getUserId());
        afterSale.setMerchantId(order.getMerchantId());
        afterSale.setRiderId(order.getDeliveryId());

        // 3. 保存售后申请
        boolean success = afterSaleService.submitAfterSale(afterSale);
        return success ? Result.success("售后申请提交成功") : Result.error("提交失败");
    }

    // 根据订单号查询售后
    @GetMapping("/getByOrderId/{orderId}")
    public Result getByOrderId(@PathVariable String orderId) {
        AfterSale afterSale = afterSaleService.getByOrderId(orderId);
        return Result.success(afterSale);
    }

    // 查询用户所有售后列表
    @GetMapping("/list/{userId}")
    public Result listByUserId(@PathVariable Integer userId) {
        List<AfterSale> list = afterSaleService.listByUserId(userId);
        return Result.success(list);
    }
}