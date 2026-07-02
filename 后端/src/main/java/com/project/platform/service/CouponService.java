package com.project.platform.service;
import com.project.platform.entity.*;
import com.project.platform.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CouponService{
    @Resource CouponMapper mapper;
    public List<Coupon> list(Integer merchantId){return mapper.list(merchantId);}
    public void add(Coupon c){mapper.insert(c);}
    public void update(Coupon c){mapper.update(c);}
    public void del(Integer id){mapper.delete(id);}
}