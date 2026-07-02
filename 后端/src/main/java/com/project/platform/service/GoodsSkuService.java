package com.project.platform.service;

import com.project.platform.entity.GoodsSpec;
import com.project.platform.entity.GoodsSku;
import java.util.List;
import java.util.Map;

public interface GoodsSkuService {
    List<GoodsSpec> getSpecListByGoodsId(Integer goodsId);
    List<GoodsSku> getSkuListByGoodsId(Integer goodsId);
    void saveAll(Integer goodsId, List<Map<String, Object>> specs, List<Map<String, Object>> skus);
}