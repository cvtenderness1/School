package com.project.platform.service.impl;

import com.project.platform.entity.GoodsSpec;
import com.project.platform.entity.GoodsSpecItem;
import com.project.platform.entity.GoodsSku;
import com.project.platform.mapper.GoodsSpecItemMapper;
import com.project.platform.mapper.GoodsSpecMapper;
import com.project.platform.mapper.GoodsSkuMapper;
import com.project.platform.service.GoodsSkuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class GoodsSkuServiceImpl implements GoodsSkuService {

    @Resource
    private GoodsSpecMapper goodsSpecMapper;
    @Resource
    private GoodsSpecItemMapper goodsSpecItemMapper;
    @Resource
    private GoodsSkuMapper goodsSkuMapper;

    @Override
    public List<GoodsSpec> getSpecListByGoodsId(Integer goodsId) {
        List<GoodsSpec> specList = goodsSpecMapper.listByGoodsId(goodsId);
        // 给每个规格加载对应的规格值列表
        for (GoodsSpec spec : specList) {
            List<GoodsSpecItem> itemList = goodsSpecItemMapper.listBySpecId(spec.getId());
            spec.setList(itemList);
        }
        return specList;
    }

    @Override
    public List<GoodsSku> getSkuListByGoodsId(Integer goodsId) {
        return goodsSkuMapper.listByGoodsId(goodsId);
    }
    @Override
    @Transactional
    public void saveAll(Integer goodsId, List<Map<String, Object>> specs, List<Map<String, Object>> skus) {
        // 先删除旧数据
        goodsSkuMapper.deleteByGoodsId(goodsId);
        goodsSpecItemMapper.deleteByGoodsId(goodsId);
        goodsSpecMapper.deleteByGoodsId(goodsId);

        // 保存规格
        for (Map<String, Object> specMap : specs) {
            GoodsSpec spec = new GoodsSpec();
            spec.setGoodsId(goodsId);
            spec.setName((String) specMap.get("name"));
            goodsSpecMapper.insert(spec);

            Integer specId = spec.getId();
            String itemsStr = (String) specMap.get("itemsStr");
            if (itemsStr == null) continue;
            String[] items = itemsStr.split(",");
            for (String itemName : items) {
                GoodsSpecItem item = new GoodsSpecItem();
                item.setSpecId(specId);
                item.setName(itemName.trim());
                goodsSpecItemMapper.insert(item);
            }
        }

        // 保存 SKU
        for (Map<String, Object> skuMap : skus) {
            GoodsSku sku = new GoodsSku();
            sku.setGoodsId(goodsId);
            sku.setSkuNameArr((String) skuMap.get("skuNameArr"));
            sku.setImage((String) skuMap.get("image"));
            sku.setPrice(new BigDecimal(skuMap.get("price").toString()));
            sku.setDiscountPrice(new BigDecimal(skuMap.get("discountPrice").toString()));
            sku.setStock((Integer) skuMap.get("stock"));
            goodsSkuMapper.insert(sku);
        }
    }
}