package com.project.platform.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.project.platform.config.Result;
import com.project.platform.entity.*;
import com.project.platform.mapper.ErrandMapper;
import com.project.platform.mapper.GoodsMapper;
import com.project.platform.mapper.OrderItemMapper;
import com.project.platform.service.OrderService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Resource
    private OrderService orderService;

    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private ErrandMapper errandMapper;
    @Resource
    private GoodsMapper goodsMapper;
    // 订单列表查询
    @GetMapping("/list")
    public Result list(OrderMaster query) {
        List<OrderMaster> list = orderService.list(query);
        return Result.success(list);
    }
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable String id) {
        List<OrderItem> entity = orderItemMapper.selectByOrderId(id);
        return Result.success(entity);
    }
    //骑手接单管理专用：分页查询自己的订单
    @GetMapping("/page")
    public Result page(OrderMaster query,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<OrderMaster> list = orderService.list(query);
        PageInfo<OrderMaster> pageInfo = PageInfo.of(list);
        return Result.success(pageInfo);
    }

    //骑手：更新配送状态（开始/完成/取消）
    @PutMapping("/updateStatus")
    public Result updateStatus(@RequestBody OrderMaster order) {
        OrderMaster exist = orderService.getById(order.getOrderId());
        if (exist == null) {
            return Result.error("订单不存在");
        }

        // 状态规则
        if (order.getOrderStatus() == 3) {
            if (exist.getOrderStatus() != 2) {
                return Result.error("只能对待配送订单进行配送");
            }
        } else if (order.getOrderStatus() == 4) {
            if (exist.getOrderStatus() != 3) {
                return Result.error("只能对配送中订单完成配送");
            }
            order.setFinishTime(new Date());
        } else if (order.getOrderStatus() == 5) {
            if (exist.getOrderStatus() != 3) {
                return Result.error("只能对配送中订单取消配送");
            }
        }

        // 更新订单
        orderService.updateById(order);
        // 跑腿同步 errand 状态
        if (exist.getOrderType() == 2) {
            Errand errand = errandMapper.selectByOrderId(order.getOrderId());
            if (errand != null) {
                // 订单状态 → 同步到 errand
                if (order.getOrderStatus() == 2) {
                    errand.setStatus(1); // 配送中
                } else if (order.getOrderStatus() == 4) {
                    errand.setStatus(2); // 已完成
                } else if (order.getOrderStatus() == 5) {
                    errand.setStatus(3); // 已取消
                }
                errandMapper.update(errand);
            }
        }

        return Result.success("操作成功");
    }

    // 骑手：编辑订单信息
    @PutMapping("/update")
    public Result update(@RequestBody OrderMaster order) {
        orderService.updateById(order);
        return Result.success("修改成功");
    }

    //骑手：删除单个订单
    @DeleteMapping("/del")
    public Result del(@RequestParam String orderId) {
        orderService.removeByIds(List.of(orderId));
        return Result.success("删除成功");
    }

    // 指派骑手
    @PutMapping("/assignRider")
    public Result assignRider(@RequestBody OrderMaster order) {
        // 1. 更新订单
        order.setOrderStatus(2);
        orderService.updateById(order);

        //更新 errand
        OrderMaster om = orderService.getById(order.getOrderId());
        if (om.getOrderType() == 2) {
            Errand errand = errandMapper.selectByOrderId(order.getOrderId());
            if (errand != null) {
                errand.setDeliveryId(order.getDeliveryId());
                errand.setStatus(1); // 配送中
                errandMapper.update(errand);
            }
        }
        return Result.success("指派成功，订单等待骑手取餐");
    }

    // 取消订单
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrderMaster order) {
        OrderMaster existOrder = orderService.getById(order.getOrderId());
        if (existOrder == null) {
            return Result.error("订单不存在");
        }
        if (existOrder.getOrderStatus() >= 3) {
            return Result.error("订单已在配送或已完成，无法取消");
        }

        order.setOrderStatus(5);
        orderService.updateById(order);

        // 跑腿单同步取消
        if (existOrder.getOrderType() == 2) {
            Errand errand = errandMapper.selectByOrderId(order.getOrderId());
            if (errand != null) {
                errand.setStatus(3);
                errandMapper.update(errand);
            }
        }

        return Result.success("订单已取消");
    }

    @GetMapping("/errand/{orderId}")
    public Result getErrand(@PathVariable String orderId) {
        Errand errand = errandMapper.selectByOrderId(orderId);
        return Result.success(errand);
    }
    @PostMapping("/add")
    public Result add(@RequestBody OrderMaster order) {
        // 1. 生成订单号
        String orderId = "OD" + System.currentTimeMillis() + (int)(Math.random()*1000);
        order.setOrderId(orderId);
        order.setOrderStatus(1);
        order.setMainOrderNo(orderId);
        orderService.insert(order);
        Integer goodsId = order.getGoodsId();
        Integer buyCount = order.getBuyCount();

        if (goodsId != null && buyCount != null) {
            // 获取商品信息（用于快照）
            good goods = goodsMapper.selectById(goodsId);

            OrderItem item = new OrderItem();
            item.setOrderId(orderId);           // 订单号
            item.setGoodsId(goodsId);           // 商品ID
            item.setGoodsName(goods.getGoodsName()); // 商品名
            BigDecimal price = Objects.nonNull(goods.getDiscountPrice())
                    ? goods.getDiscountPrice()
                    : goods.getPrice();

            BigDecimal total = price.multiply(new BigDecimal(buyCount));
            BigDecimal deliveryFee = order.getDeliveryFee() == null ? new BigDecimal(0) : order.getDeliveryFee();
            BigDecimal finalTotal = total.add(deliveryFee); // 商品费 + 配送费

            item.setPrice(finalTotal);
            item.setQuantity(buyCount);// 数量
            orderItemMapper.insert(item); // 插入明细表
        }

        // 插入跑腿任务
        if (order.getOrderType() == 2) {
            Errand errand = new Errand();
            errand.setOrderId(orderId);
            errand.setUserId(order.getUserId());
            errand.setErrandType(4); // 1代取 2代买 3代送 4其他
            errand.setErrandType(order.getErrandType());

            errand.setStartAddress(order.getStartAddress());
            errand.setEndAddress(order.getEndAddress());

            errand.setRemark(order.getNote());
            errand.setStatus(0);
            errandMapper.insert(errand);
        }

        return Result.success("创建成功");
    }
    // 批量删除订单
    @DeleteMapping("/delBatch")
    public Result delBatch(@RequestBody List<String> orderIds) {
        orderService.removeByIds(orderIds);
        return Result.success("删除成功");
    }


}