package com.project.platform.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    List<Map<String, Object>> getOrderHeatmap(Integer days);
    List<Map<String, Object>> getRiderPerformance();
    Map<String, Object> getComplaintStats();
    List<Map<String, Object>> getFinanceFlow(Integer days);
    Map<String, Object> getCoreMetrics();
}