package com.project.platform.controller;

import com.project.platform.config.Result;
import com.project.platform.entity.AfterSale;
import com.project.platform.mapper.AfterSaleMapper;
import com.project.platform.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    @Resource
    private AfterSaleMapper afterSaleMapper;
    @Resource
    private DashboardService dashboardService;

    // 1. 订单热力图数据（按日期统计）
    @GetMapping("/order/heatmap")
    public Result getOrderHeatmap(@RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> list = dashboardService.getOrderHeatmap(days);
        return Result.success(list);
    }

    // 2. 骑手绩效排行榜
    @GetMapping("/rider/performance")
    public Result getRiderPerformance() {
        List<Map<String, Object>> list = dashboardService.getRiderPerformance();
        return Result.success(list);
    }

    // 3. 客诉率 & 售后统计
    @GetMapping("/complaint/stats")
    public Result getComplaintStats() {
        Map<String, Object> stats = dashboardService.getComplaintStats();
        return Result.success(stats);
    }

    // 4. 资金流水/营收数据
    @GetMapping("/finance/flow")
    public Result getFinanceFlow(@RequestParam(defaultValue = "30") Integer days) {
        List<Map<String, Object>> list = dashboardService.getFinanceFlow(days);
        return Result.success(list);
    }

    // 5. 核心指标卡片（订单总数、营收、客诉率、骑手在线数）
    @GetMapping("/core/metrics")
    public Result getCoreMetrics() {
        Map<String, Object> metrics = dashboardService.getCoreMetrics();
        return Result.success(metrics);
    }

    // 待处理客诉列表
    @GetMapping("/complaint/pending")
    public Result pendingComplaint() {
        List<AfterSale> list = afterSaleMapper.selectPendingList();
        return Result.success(list);
    }

    // 处理客诉（同意/驳回）
    @PostMapping("/complaint/handle")
    public Result handleComplaint(@RequestBody Map<String, Integer> params) {
        Integer id = params.get("id");
        Integer status = params.get("status"); // 2=完成 3=驳回
        afterSaleMapper.updateStatus(id, status);
        return Result.success("处理成功");
    }
}