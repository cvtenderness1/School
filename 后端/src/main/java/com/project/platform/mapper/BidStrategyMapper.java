package com.project.platform.mapper;
import com.project.platform.entity.BidStrategy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BidStrategyMapper {
    List<BidStrategy> list(Integer merchantId);
    void insert(BidStrategy b);
    void update(BidStrategy b);
    void delete(Integer id);

    List<BidStrategy> listByMerchantId(@Param("merchantId") Integer merchantId);
}