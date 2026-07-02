package com.project.platform.controller;

import com.github.pagehelper.PageInfo;
import com.project.platform.config.Result;
import com.project.platform.entity.*;
import com.project.platform.service.AdminService;

import com.project.platform.service.GoodsService;
import com.project.platform.service.GoodsSkuService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private GoodsService goodsService;

    @Resource
    private GoodsSkuService goodsSkuService;

    @PostMapping("/stock/deduct")
    public Result deductStock(@RequestBody Map<String, Object> params) {
        Integer goodsId = (Integer) params.get("goodsId");
        Integer count = (Integer) params.get("count");

        boolean success = goodsService.deductStock(goodsId, count);
        if (success) {
            return Result.success("扣减库存成功");
        } else {
            return Result.error("库存不足或扣减失败");
        }
    }
    @GetMapping("/list")
    public Result page(good goods) {
        List<good> list = goodsService.list(goods);
        return Result.success(list);
    }
    @GetMapping("/page")
    public Result page( @RequestParam(defaultValue = "1")Integer pageNum, @RequestParam(defaultValue = "10")Integer pageSize) {
        PageInfo<good> pageInfo = goodsService.page(pageNum, pageSize);
        return Result.success(pageInfo);
    }
    @GetMapping("/selectAll")
    public Result selectAll() {
        List<good> list = goodsService.selectAll();
        return Result.success(list);
    }
    @PostMapping("/add")
    public Result add(@RequestBody good goods) {
        goodsService.add(goods);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody good goods) {
        goodsService.updateById(goods);
        return Result.success();
    }

    @DeleteMapping("/del")
    public Result del(@RequestParam Integer id) {
        goodsService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/delBatch")
    public Result delBatch(@RequestBody List<Integer> ids) {
        goodsService.removeByIds(ids);
        return Result.success();
    }
    @GetMapping("/sku/{goodsId}")
    public Result getGoodsSkuData(@PathVariable Integer goodsId) {
        // 1. 获取规格列表
        List<GoodsSpec> specList = goodsSkuService.getSpecListByGoodsId(goodsId);
        // 2. 获取SKU列表
        List<GoodsSku> skuList = goodsSkuService.getSkuListByGoodsId(goodsId);
        return Result.success(Map.of(
                "specList", specList,
                "skuList", skuList
        ));
    }
    @PostMapping("/sku/saveAll")
    public Result saveSkuAll(@RequestBody Map<String, Object> params) {
        Integer goodsId = (Integer) params.get("goodsId");
        List<Map<String, Object>> specs = (List<Map<String, Object>>) params.get("specs");
        List<Map<String, Object>> skus = (List<Map<String, Object>>) params.get("skus");

        goodsSkuService.saveAll(goodsId, specs, skus);
        return Result.success();
    }
}