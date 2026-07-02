package com.project.platform.service;

import com.project.platform.entity.OrderMaster;
import java.util.List;

public interface OrderService {
    List<OrderMaster> list(OrderMaster query);
    void updateById(OrderMaster order);
    void removeByIds(List<String> orderIds);
    void insert(OrderMaster order);

    OrderMaster getById(String orderId);
}