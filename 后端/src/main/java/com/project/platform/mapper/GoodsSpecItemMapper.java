package com.project.platform.mapper;

import com.project.platform.entity.GoodsSpecItem;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface GoodsSpecItemMapper {
    List<GoodsSpecItem> listBySpecId(Integer specId);
    void deleteByGoodsId(Integer goodsId);
    int insert(GoodsSpecItem item); // 新增
}