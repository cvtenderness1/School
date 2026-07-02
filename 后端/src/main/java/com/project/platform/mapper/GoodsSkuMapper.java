package com.project.platform.mapper;

import com.project.platform.entity.GoodsSku;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface GoodsSkuMapper {
    List<GoodsSku> listByGoodsId(Integer goodsId);
    void deleteByGoodsId(Integer goodsId);
    int insert(GoodsSku sku); // 新增

    GoodsSku selectById(Integer id);

    void updateStock(GoodsSku sku);
}