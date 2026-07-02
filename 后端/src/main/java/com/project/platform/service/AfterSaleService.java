package com.project.platform.service;

import com.project.platform.entity.AfterSale;
import java.util.List;

public interface AfterSaleService {
    boolean submitAfterSale(AfterSale afterSale);
    AfterSale getByOrderId(String orderId);
    List<AfterSale> listByUserId(Integer userId);
    boolean updateStatus(Integer id, Integer status);
}