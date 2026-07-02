package com.project.platform.service.impl;

import com.project.platform.entity.AfterSale;
import com.project.platform.mapper.AfterSaleMapper;
import com.project.platform.service.AfterSaleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AfterSaleServiceImpl implements AfterSaleService {

    @Resource
    private AfterSaleMapper afterSaleMapper;

    @Override
    public boolean submitAfterSale(AfterSale afterSale) {
        afterSale.setStatus(0); // 默认待处理
        return afterSaleMapper.insertAfterSale(afterSale) > 0;
    }

    @Override
    public AfterSale getByOrderId(String orderId) {
        return afterSaleMapper.selectByOrderId(orderId);
    }

    @Override
    public List<AfterSale> listByUserId(Integer userId) {
        return afterSaleMapper.selectByUserId(userId);
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        return afterSaleMapper.updateStatus(id, status) > 0;
    }
}