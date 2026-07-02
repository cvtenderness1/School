package com.project.platform.mapper;

import com.project.platform.entity.good;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface GoodsMapper {

    void updateById(good goods);
    List<good> selectAll();
    void removeById(Integer id);

    void removeByIds(List<Integer> ids);

    void add(good goods);
    List<good> list(good goods);

    // 扣减库存
    @Update("update goods set stock = stock - #{count} where goods_id = #{goodsId} and stock >= #{count}")
    int deductStock(@Param("goodsId") Integer goodsId, @Param("count") Integer count);
    @Select("SELECT * FROM goods WHERE goods_id = #{goodsId}")
    good selectById(Integer goodsId);

    @Select("SELECT * FROM goods WHERE merchant_id = #{merchantId}")
    List<good> selectBymerchantId(Integer merchantId);

    void updateStock(good goodsInfo);
}
