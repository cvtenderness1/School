package com.project.platform.controller;

import com.project.platform.config.Result;
import com.project.platform.entity.Comment;
import com.project.platform.entity.OrderMaster;
import com.project.platform.mapper.OrderMapper;
import com.project.platform.service.CommentService;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private OrderMapper orderMasterMapper;

    // 提交订单评价
    @PostMapping("/submit")
    public Result submitComment(@RequestBody Comment comment) {
        // 根据主订单号查询所有子订单
        List<OrderMaster> orderList = orderMasterMapper.selectBymin(comment.getOrderId());

        // 1. 校验订单是否存在
        if (orderList == null || orderList.isEmpty()) {
            return Result.error("订单不存在");
        }

        // 2. 校验订单是否已完成（只有已完成才能评价）
        OrderMaster order = orderList.get(0);
        if (!order.getOrderStatus().equals(4)) {
            return Result.error("订单未完成，无法评价");
        }

        // 3. 校验订单是否已评价
        if (commentService.isCommented(comment.getOrderId())) {
            return Result.error("该订单已评价");
        }

        // 4. 自动填充关联信息
        comment.setUserId(order.getUserId());
        comment.setMerchantId(order.getMerchantId());
        comment.setRiderId(order.getDeliveryId());
        comment.setCreateTime(LocalDateTime.now());

        // 5. 保存评价
        boolean save = commentService.submitComment(comment);
        if (!save) {
            return Result.error("评价失败");
        }
        for (OrderMaster om : orderList) {
            om.setOrderStatus(6);
            orderMasterMapper.updateById(om);
        }

        return Result.success("评价成功");

    }

    // 根据订单号获取评价
    @GetMapping("/getByOrderId/{orderId}")
    public Result getComment(@PathVariable String orderId) {
        Comment comment = commentService.getByOrderId(orderId);
        return Result.success(comment);
    }
    // 根据商家ID查询店铺评价
    @GetMapping("/merchant/{merchantId}")
    public Result getMerchantComments(@PathVariable Integer merchantId) {
        List<Comment> list = commentService.getByMerchantId(merchantId);
        return Result.success(list);
    }
}