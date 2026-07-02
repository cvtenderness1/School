package com.project.platform.service;

import com.github.pagehelper.PageInfo;
import com.project.platform.entity.Merchant;
import java.util.List;

public interface MerchantService {
    PageInfo<Merchant> page(Merchant merchant, Integer pageNum, Integer pageSize);
    List<Merchant> list(Merchant merchant);
    void insert(Merchant merchant);
    void updateById(Merchant merchant);
    void removeByIds(List<Integer> ids);
    List<Merchant> selectall();

    Merchant getById(Integer id);
}