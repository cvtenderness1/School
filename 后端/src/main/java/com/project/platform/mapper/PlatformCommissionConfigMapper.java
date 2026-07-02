package com.project.platform.mapper;

import com.project.platform.entity.PlatformCommissionConfig;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PlatformCommissionConfigMapper {
    List<PlatformCommissionConfig> selectAll();
    PlatformCommissionConfig selectByType(@Param("orderType") Integer orderType);
    int updateConfig(PlatformCommissionConfig config);
}