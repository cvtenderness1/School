package com.project.platform.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.project.platform.entity.Merchant;
import com.project.platform.mapper.MerchantMapper;
import com.project.platform.service.MerchantService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MerchantServiceImpl implements MerchantService {

    @Resource
    private MerchantMapper merchantMapper;

    @Override
    public PageInfo<Merchant> page(Merchant merchant, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(merchantMapper.list(merchant));
    }

    @Override
    public List<Merchant> list(Merchant merchant) {
        return merchantMapper.list(merchant);
    }

    @Override
    public void insert(Merchant merchant) {
        merchantMapper.insert(merchant);
    }

    @Override
    public void updateById(Merchant merchant) {
        merchantMapper.updateById(merchant);
    }

    @Override
    public void removeByIds(List<Integer> ids) {
        merchantMapper.removeByIds(ids);
    }

    @Override
    public List<Merchant> selectall() {
        return merchantMapper.selectAll();
    }

    @Override
    public Merchant getById(Integer id) {
        return merchantMapper.selectById(id);
    }
}