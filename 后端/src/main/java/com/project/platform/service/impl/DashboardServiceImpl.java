package com.project.platform.service.impl;

import com.project.platform.mapper.DashboardMapper;
import com.project.platform.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private DashboardMapper dashboardMapper;

    @Override
    public List<Map<String, Object>> getOrderHeatmap(Integer days) {
        return dashboardMapper.selectOrderCountByDate(days);
    }

    @Override
    public List<Map<String, Object>> getRiderPerformance() {
        return dashboardMapper.selectRiderPerformance();
    }

    @Override
    public Map<String, Object> getComplaintStats() {
        return dashboardMapper.selectComplaintStats();
    }

    @Override
    public List<Map<String, Object>> getFinanceFlow(Integer days) {
        return dashboardMapper.selectFinanceFlow(days);
    }

    @Override
    public Map<String, Object> getCoreMetrics() {
        return dashboardMapper.selectCoreMetrics();
    }
}