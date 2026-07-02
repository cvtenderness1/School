package com.project.platform.controller;

import com.project.platform.config.Result;
import com.project.platform.entity.InvoiceApply;
import com.project.platform.service.FinanceService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/finance")
public class FinanceController {

    @Resource
    private FinanceService financeService;

    // 1.生成结算单
    @PostMapping("/settlement/generate")
    public Result generateSettlement(@RequestParam String startDate,
                                     @RequestParam String endDate){
        financeService.generateSettlement(startDate,endDate);
        return Result.success("结算单生成成功");
    }

    // 2.结算单列表
    @GetMapping("/settlement/list")
    public Result settlementList(@RequestParam(defaultValue = "-1") Integer status){
        return Result.success(financeService.settlementList(status));
    }

    // 3.审核/修改状态
    @PutMapping("/settlement/audit")
    public Result audit(@RequestParam String settleNo,
                        @RequestParam Integer status){
        financeService.auditSettlement(settleNo,status);
        return Result.success("操作成功");
    }

    // 4.批量打款
    @PostMapping("/pay/batch")
    public Result batchPay(@RequestBody List<String> settleNoList){
        financeService.batchPay(settleNoList);
        return Result.success("批量打款完成");
    }

    // 5.开票申请
    @PostMapping("/invoice/apply")
    public Result invoiceApply(@RequestBody InvoiceApply apply){
        financeService.applyInvoice(apply);
        return Result.success("开票申请提交成功");
    }
    @DeleteMapping("/invoice/delete")
    public Result deleteInvoice(@RequestParam Integer id) {
        financeService.deleteInvoice(id);
        return Result.success();
    }
    // 6.开票列表
    @GetMapping("/invoice/list")
    public Result invoiceList(@RequestParam(defaultValue = "-1") Integer status){
        return Result.success(financeService.invoiceList(status));
    }

    // 7.标记已开票
    @PutMapping("/invoice/done")
    public Result invoiceDone(@RequestParam Long id){
        financeService.markInvoiceDone(id);
        return Result.success("已标记为已开票");
    }

    @DeleteMapping("/settlement/delete")
    public Result deleteSettlement(@RequestParam String settleNo) {
        financeService.deleteSettlement(settleNo);
        return Result.success("删除成功");
    }
}