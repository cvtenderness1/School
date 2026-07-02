package com.project.platform.mapper;

import com.project.platform.entity.AfterSale;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AfterSaleMapper {

    // 售后申请
    int insertAfterSale(AfterSale afterSale);

    // 根据订单号查询售后
    AfterSale selectByOrderId(@Param("orderId") String orderId);

    // 查询用户所有售后列表
    List<AfterSale> selectByUserId(@Param("userId") Integer userId);

    // 更新售后状态
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    // 统计总售后单
    int countTotal();

    // 统计待处理
    int countPending();

    // 统计差评/投诉
    int countBadComment();

    // 查询待处理客诉列表
    List<AfterSale> selectPendingList();
}