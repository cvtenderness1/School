package com.project.platform.mapper;

import com.project.platform.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface MerchantMapper {
    List<Merchant> list(Merchant query);
    List<Merchant> selectAll();
    int insert(Merchant merchant);
    int updateById(Merchant merchant);
    int removeByIds(List<Integer> ids);
    @Select("SELECT postFee FROM merchant WHERE merchant_id = #{merchantId}")
    BigDecimal getPostFee(Integer merchantId);

    @Select("select * from merchant where merchant_id = #{merchantId}")
    Merchant selectById(@Param("merchantId") Integer merchantId);
}