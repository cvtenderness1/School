package com.project.platform.mapper;

import com.project.platform.entity.GoodsSpec;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface GoodsSpecMapper {
    List<GoodsSpec> listByGoodsId(Integer goodsId);
    void deleteByGoodsId(Integer goodsId);
    int insert(GoodsSpec spec); // 新增
}