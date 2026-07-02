package com.project.platform.mapper;

import com.project.platform.entity.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderItemMapper {

    @Insert("INSERT INTO order_item(order_id, goods_id, sku_id, goods_name, picture, sku_attrs, price, pay_price, quantity, total_price) " +
            "VALUES(#{orderId}, #{goodsId}, #{skuId}, #{goodsName}, #{picture}, #{skuAttrs}, #{price}, #{payPrice}, #{quantity},#{totalPrice})")
    int insert(OrderItem item);
    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> selectByOrderId(String orderId);
}