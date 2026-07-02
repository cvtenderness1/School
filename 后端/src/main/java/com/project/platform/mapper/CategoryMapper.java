package com.project.platform.mapper;

import com.project.platform.entity.Category;
import com.project.platform.entity.Merchant;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CategoryMapper {
    int insert(Category category);
    Category findById(int id);
    int updateById(Category category);
    @MapKey("category_id")
    List<Map<String, Object>> findAllDistinctCategoryName();
    List<Category> findByMerchantId(Integer merchantId);
}