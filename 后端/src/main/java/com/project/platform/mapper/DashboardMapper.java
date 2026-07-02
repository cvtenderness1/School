package com.project.platform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {
    // 按日期统计订单量
    List<Map<String, Object>> selectOrderCountByDate(@Param("days") Integer days);

    // 骑手绩效统计
    List<Map<String, Object>> selectRiderPerformance();

    // 客诉/售后统计
    Map<String, Object> selectComplaintStats();

    // 资金流水/营收统计
    List<Map<String, Object>> selectFinanceFlow(@Param("days") Integer days);

    // 核心指标
    Map<String, Object> selectCoreMetrics();
}