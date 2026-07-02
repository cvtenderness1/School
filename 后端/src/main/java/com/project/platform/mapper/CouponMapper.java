package com.project.platform.mapper;
import com.project.platform.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CouponMapper {
    List<Coupon> list(Integer merchantId);
    void insert(Coupon c);
    void update(Coupon c);
    void delete(Integer id);
}