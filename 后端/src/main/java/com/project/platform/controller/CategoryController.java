package com.project.platform.controller;

import com.project.platform.config.Result;
import com.project.platform.mapper.CategoryMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryMapper categoryMapper;

    @GetMapping("/listAll")
    public Result listAll() {
        // 直接返回分类名称数组
        List<Map<String, Object>>  list = categoryMapper.findAllDistinctCategoryName();
        return Result.success(list);
    }
    @GetMapping("/byMerchant")
    public Result byMerchant(@RequestParam Integer merchantId) {
        return Result.success(categoryMapper.findByMerchantId(merchantId));
    }
}