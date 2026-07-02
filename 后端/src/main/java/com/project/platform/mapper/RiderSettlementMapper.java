package com.project.platform.mapper;

import com.project.platform.entity.RiderSettlement;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface RiderSettlementMapper {
    int insertSettlement(RiderSettlement settlement);
    List<RiderSettlement> selectList(@Param("status") Integer status);
    RiderSettlement selectByNo(@Param("settleNo") String settleNo);
    int updateStatus(@Param("settleNo") String settleNo, @Param("status") Integer status);

    int deleteByNo(@Param("settleNo") String settleNo);

    int updateToPaid(String settleNo);
}