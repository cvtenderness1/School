package com.project.platform.mapper;

import com.project.platform.entity.RiderBalanceFlow;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface RiderBalanceFlowMapper {
    int insertFlow(RiderBalanceFlow flow);
    List<RiderBalanceFlow> selectByRider(@Param("riderId") Integer riderId);
}