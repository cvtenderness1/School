package com.project.platform.service;
import com.github.pagehelper.PageInfo;
import com.project.platform.entity.Rider;

import java.math.BigDecimal;
import java.util.List;

public interface RiderService {
    PageInfo<Rider> page(Rider rider, Integer pageNum, Integer pageSize);
    List<Rider> list(Rider rider);
    void insert(Rider rider);
    void updateById(Rider rider);
    void removeByIds(List<Integer> ids);
    List<Rider> selectAll();

    Rider getByAccount(String account);
    Rider getByPhone(String phone);

    Rider selectById(Integer riderId);

    void addBalance(Integer riderId, BigDecimal money);
    void updateLocation(Integer riderId, BigDecimal lat, BigDecimal lng);

    List<Rider> getBalanceRank();

    void batchUpdateStatus(List<Integer> ids, Integer status);
}