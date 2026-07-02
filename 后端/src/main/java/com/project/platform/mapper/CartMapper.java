package com.project.platform.mapper;

import com.project.platform.entity.Cart;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface CartMapper {

    @Select("SELECT * FROM cart WHERE id = #{id}")
    Cart selectById(Integer id);

    List<Cart> list(Cart cart);

    int insert(Cart entity);

    int updateById(Cart entity);

    boolean removeByIds(List<Integer> ids);

    @Select("SELECT * FROM cart WHERE user_id = #{userId}")
    List<Cart> selectByUserId(Integer userId);

    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND sku_id = #{skuId}")
    Cart selectByUserIdAndSkuId(@Param("userId") Integer userId, @Param("skuId") Integer skuId);


    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND goods_id = #{goodsId}")
    List<Cart> selectByUserIdAndgoodsId(Integer userId, Integer goodsId);

    List<Cart> selectByUserIdAndSelected(Integer userId);
}