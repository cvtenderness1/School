package com.project.platform.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.project.platform.entity.Rider;
import com.project.platform.exception.CustomException;
import com.project.platform.mapper.RiderMapper;
import com.project.platform.service.RiderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RiderServiceImpl implements RiderService {

    @Resource
    private RiderMapper riderMapper;

    @Override
    public PageInfo<Rider> page(Rider rider, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(riderMapper.list(rider));
    }

    @Override
    public List<Rider> list(Rider rider) {
        return riderMapper.list(rider);
    }

    @Override
    public List<Rider> selectAll() {
        return riderMapper.selectAll();
    }

    @Override
    public void insert(Rider rider) {
        Rider exist = riderMapper.selectByAccount(rider.getAccount());
        if (exist != null) throw new CustomException("账号已存在");
        riderMapper.insert(rider);
    }

    @Override
    public void updateById(Rider rider) {
        riderMapper.updateById(rider);
    }

    @Override
    public void removeByIds(List<Integer> ids) {
        riderMapper.removeByIds(ids);
    }

    /**
     * 根据账号查询骑手（用于账号密码登录）
     */
    @Override
    public Rider getByAccount(String account) {
        return riderMapper.selectByAccount(account);
    }

    /**
     * 根据手机号查询骑手（用于模拟快捷登录）
     */
    @Override
    public Rider getByPhone(String phone) {
        return riderMapper.selectByPhone(phone);
    }
    @Override
    public List<Rider> getBalanceRank() {
        return riderMapper.selectBalanceRank();
    }

    @Override
    @Transactional
    public void batchUpdateStatus(List<Integer> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 调用 Mapper 批量更新
        riderMapper.batchUpdateStatus(ids, status);
    }

    @Override
    public Rider selectById(Integer riderId) {
        return riderMapper.selectById(riderId);
    }

    @Override
    public void addBalance(Integer riderId, BigDecimal money) {
        riderMapper.addBalance(riderId, money);
    }
    @Override
    public void updateLocation(Integer riderId, BigDecimal lat, BigDecimal lng) {
        riderMapper.updateLocation(riderId, lat, lng);
    }


}