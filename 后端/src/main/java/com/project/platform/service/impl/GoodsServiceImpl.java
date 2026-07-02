package com.project.platform.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.project.platform.entity.Admin;
import com.project.platform.entity.good;
import com.project.platform.mapper.GoodsMapper;
import com.project.platform.service.GoodsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


import java.util.List;
@Service
public class GoodsServiceImpl implements GoodsService {
    @Resource
    private GoodsMapper goodsMapper;
    @Override
    public void updateById(good goods) {
        goodsMapper.updateById(goods);
    }

    @Override
    public void removeById(Integer id) {
        goodsMapper.removeById(id);
    }

    @Override
    public void removeByIds(List<Integer> ids) {
        goodsMapper.removeByIds(ids);
    }

    @Override
    public void add(good goods) {
        goodsMapper.add(goods);
    }

    @Override
    public boolean deductStock(Integer goodsId, Integer count) {
        if (goodsId == null || count == null || count <= 0) return false;
        return goodsMapper.deductStock(goodsId, count) > 0;
    }

    @Override
    public List<good> list(good goods) {
        return  goodsMapper.list(goods);
    }

    @Override
    public List<good> selectAll() {
        return goodsMapper.selectAll();
    }
    @Override
    public PageInfo page( Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<good> page = goodsMapper.selectAll();
        return PageInfo.of(page);
    }
}
