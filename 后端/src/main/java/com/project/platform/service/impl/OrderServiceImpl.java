package com.project.platform.service.impl;

import com.project.platform.entity.OrderMaster;
import com.project.platform.entity.Rider;
import com.project.platform.mapper.OrderMapper;
import com.project.platform.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Override
    public List<OrderMaster> list(OrderMaster query) {
        return orderMapper.list(query);
    }

    @Override
    public void updateById(OrderMaster order) {
        orderMapper.updateById(order);
    }
    @Override
    public void insert(OrderMaster order) {
        orderMapper.insert(order);
    }

    @Override
    public OrderMaster getById(String orderId) {
      return orderMapper.getById(orderId);

    }

    @Override
    public void removeByIds(List<String> orderIds) {
        orderMapper.removeByIds(orderIds);
    }
}