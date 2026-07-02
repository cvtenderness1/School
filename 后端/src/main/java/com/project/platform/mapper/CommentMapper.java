package com.project.platform.mapper;

import com.project.platform.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 评价
    int insertComment(Comment comment);

    // 根据订单号查询评价
    Comment selectByOrderId(@Param("orderId") String orderId);

    // 检查订单是否已评价
    int countByOrderId(@Param("orderId") String orderId);

    List<Comment> selectByMerchantId(Integer merchantId);
}