package com.project.platform.service;

import com.project.platform.entity.InvoiceApply;
import com.project.platform.entity.RiderSettlement;
import java.util.List;

public interface FinanceService {
    // 生成骑手结算单
    void generateSettlement(String startDate, String endDate);
    // 结算单列表
    List<RiderSettlement> settlementList(Integer status);
    // 审核结算单
    void auditSettlement(String settleNo, Integer status);
    // 批量打款
    void batchPay(List<String> settleNoList);
    // 开票申请
    void applyInvoice(InvoiceApply apply);
    // 开票列表
    List<InvoiceApply> invoiceList(Integer status);
    // 标记已开票
    void markInvoiceDone(Long id);

    void deleteSettlement(String settleNo);

    void deleteInvoice(Integer id);
}