package com.project.platform.service.impl;

import com.project.platform.entity.*;
import com.project.platform.mapper.*;
import com.project.platform.service.FinanceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Resource
    private RiderSettlementMapper settlementMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private PlatformCommissionConfigMapper commissionConfigMapper;
    @Resource
    private RiderBalanceFlowMapper flowMapper;
    @Resource
    private RiderMapper riderMapper;
    @Resource
    private InvoiceApplyMapper invoiceApplyMapper;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    @Transactional
    public void generateSettlement(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 1.获取抽成比例
        PlatformCommissionConfig config = commissionConfigMapper.selectByType(2);
        BigDecimal rate = config.getCommissionRate();

        // 2.查询该时间段 已完成订单 按骑手分组统计
        List<Map<String,Object>> riderOrderData = orderMapper.selectRiderFinishOrder(startDate,endDate);

        for(Map<String,Object> map : riderOrderData){
            Integer riderId = Integer.parseInt(map.get("riderId").toString());
            Integer orderNum = Integer.parseInt(map.get("orderNum").toString());
            BigDecimal totalAmount = new BigDecimal(map.get("totalAmount").toString());

            // 计算抽成、骑手佣金
            BigDecimal commission = totalAmount.multiply(rate).divide(new BigDecimal(100),2);
            BigDecimal riderMoney = totalAmount.subtract(commission);

            // 生成结算单号
            String settleNo = "SET" + System.currentTimeMillis() + new Random().nextInt(1000);

            RiderSettlement settle = new RiderSettlement();
            settle.setSettleNo(settleNo);
            settle.setRiderId(riderId);
            try {
                // 捕获 parse 异常
                settle.setSettleStart(sdf.parse(startDate));
                settle.setSettleEnd(sdf.parse(endDate));
            } catch (ParseException e) {
                throw new RuntimeException("日期格式解析错误，请检查格式是否为 yyyy-MM-dd", e);
            }
            settle.setTotalOrderNum(orderNum);
            settle.setTotalOrderAmount(totalAmount);
            settle.setPlatformCommission(commission);
            settle.setRiderCommission(riderMoney);
            settle.setStatus(0);
            settle.setCreateTime(new Date());

            settlementMapper.insertSettlement(settle);
        }
    }

    @Override
    public List<RiderSettlement> settlementList(Integer status) {
        return settlementMapper.selectList(status);
    }

    @Override
    public void auditSettlement(String settleNo, Integer status) {
        settlementMapper.updateStatus(settleNo,status);
    }

    @Override
    @Transactional
    public void batchPay(List<String> settleNoList) {
        for (String no : settleNoList) {
            //只有 status=1 才能改成 2，返回影响行数
            int rows = settlementMapper.updateToPaid(no);

            // 没更新到 → 已支付 / 状态不对 → 跳过
            if (rows <= 0) {
                continue;
            }

            // 只有真正更新成功，才执行打款
            RiderSettlement settle = settlementMapper.selectByNo(no);
            if (settle == null) continue;

            Integer riderId = settle.getRiderId();
            BigDecimal money = settle.getRiderCommission();

            // 1. 增加骑手余额
            riderMapper.addBalance(riderId, money);

            // 2. 记录余额流水
            BigDecimal newBalance = riderMapper.selectBalance(riderId);
            RiderBalanceFlow flow = new RiderBalanceFlow();
            flow.setRiderId(riderId);
            flow.setSettleNo(no);
            flow.setFlowType(1);
            flow.setAmount(money);
            flow.setBalanceAfter(newBalance);
            flow.setRemark("结算单打款：" + no);
            flow.setCreateTime(new Date());
            flowMapper.insertFlow(flow);
        }
    }

    @Override
    public void applyInvoice(InvoiceApply apply) {
        String invoiceNo = "INV" + System.currentTimeMillis();
        apply.setInvoiceNo(invoiceNo);
        apply.setStatus(0);
        apply.setCreateTime(new Date());
        apply.setUpdateTime(new Date());
        invoiceApplyMapper.insertInvoice(apply);
    }

    @Override
    public List<InvoiceApply> invoiceList(Integer status) {
        return invoiceApplyMapper.selectList(status);
    }

    @Override
    public void markInvoiceDone(Long id) {
        invoiceApplyMapper.updateInvoiceStatus(id,1);
    }

    @Override
    public void deleteSettlement(String settleNo) {
        settlementMapper.deleteByNo(settleNo);
    }

    @Override
    public void deleteInvoice(Integer id) {
        invoiceApplyMapper.deleteById(id);
    }
}