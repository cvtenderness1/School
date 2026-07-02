package com.project.platform.service;

import com.github.pagehelper.PageInfo;
import com.project.platform.entity.good;

import java.util.List;

public interface GoodsService {
    void updateById(good goods);

    void removeById(Integer id);

    void removeByIds(List<Integer> ids);

    void add(good goods);
    boolean deductStock(Integer goodsId, Integer count);
    List<good> list(good goods);

    List<good> selectAll();

    PageInfo<good> page(Integer pageNum, Integer pageSize);
}
