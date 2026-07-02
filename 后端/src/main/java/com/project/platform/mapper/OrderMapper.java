package com.project.platform.mapper;

import com.project.platform.entity.OrderMaster;
import com.project.platform.entity.Rider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    // 分页/列表查询
    List<OrderMaster> list(OrderMaster query);
    // 单个/批量更新
    int updateById(OrderMaster order);
    int insert(OrderMaster order);
    // 批量删除（和骑手/商家模块统一风格）
    int removeByIds(List<String> orderIds);
    OrderMaster getById(String orderId);

    OrderMaster selectById(String id);

    List<OrderMaster> selectBymin(String mainOrderNo);

    List<OrderMaster> selectByUserIdAndState(
            @Param("userId") Integer userId,
            @Param("orderState") Integer orderState
    );

    List<OrderMaster> findDeliveringOrdersByRider(Integer riderId);

    List<OrderMaster> selectGrabNearby();

    List<Map<String,Object>> selectRiderFinishOrder(@Param("startDate") String startDate,
                                                    @Param("endDate") String endDate);

    // 🔥 原子抢单：只有 order_status=1 才能抢
    int grabOrder(@Param("orderId") String orderId, @Param("riderId") Integer riderId);
}