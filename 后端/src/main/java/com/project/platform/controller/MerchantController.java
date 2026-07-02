package com.project.platform.controller;

import com.github.pagehelper.PageInfo;
import com.project.platform.config.Result;
import com.project.platform.entity.Category;
import com.project.platform.entity.Merchant;
import com.project.platform.entity.Rider;
import com.project.platform.entity.good;
import com.project.platform.mapper.CategoryMapper;
import com.project.platform.mapper.GoodsMapper;
import com.project.platform.service.GoodsService;
import com.project.platform.service.MerchantService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/merchant")
public class MerchantController {
    @Resource
    private GoodsService goodsService;
    @Resource
    private MerchantService merchantService;
    @Resource
    private CategoryMapper categoryMapper;
    @GetMapping("/page")
    public Result page(Merchant merchant,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Merchant> pageInfo = merchantService.page(merchant, pageNum, pageSize);
        return Result.success(pageInfo);
    }
    @GetMapping("/selectall")
    public Result selectall() {
        List<Merchant> list = merchantService.selectall();
        return Result.success(list);
    }
    @PostMapping("/add")
    public Result add(@RequestBody Merchant merchant, @RequestParam String categoryName ) {
        if (merchant.getScore() == null) {
            merchant.setScore(new BigDecimal("5.0"));
        }

        if (merchant.getUserId() == null) {
            merchant.setUserId(1);
        }
        merchantService.insert(merchant);
        // 4. 保存分类
        Category category = new Category();
        category.setMerchantId(merchant.getMerchantId());
        category.setCategoryName(categoryName);
        category.setSort(0);
        categoryMapper.insert(category);

        return Result.success();
    }
    @GetMapping("/findid")
    public Result findid(@RequestParam Integer merchantId) {
        Category category = categoryMapper.findById(merchantId);
        return Result.success(category);
    }
    @PutMapping("/update")
    public Result update(@RequestBody Merchant merchant,@RequestParam String categoryName) {
        merchantService.updateById(merchant);
        Category category = new Category();
        category.setMerchantId(merchant.getMerchantId());
        category.setCategoryName(categoryName);
        category.setSort(0);
        categoryMapper.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/delBatch")
    public Result delBatch(@RequestBody List<Integer> ids) {
        merchantService.removeByIds(ids);
        return Result.success();
    }
    @GetMapping("/detail")
    public Result detail(@RequestParam Integer id) {
        Merchant merchant = merchantService.getById(id);
        return Result.success(merchant);
    }

    @GetMapping("/goods")
    public Result goods(@RequestParam Integer merchantId) {
        good query = new good();
        query.setMerchantId(merchantId);
        List<good> list = goodsService.list(query);
        return Result.success(list);
    }
}