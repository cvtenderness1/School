package com.project.platform.service;

import com.github.pagehelper.PageInfo;
import com.project.platform.entity.Cart;
import java.util.List;

public interface CartService {
    PageInfo<Cart> page(Cart cart, Integer pageNum, Integer pageSize);
    Cart selectById(Integer id);
    List<Cart> list(Cart cart);
    void insert(Cart entity);
    void updateById(Cart entity);
    void removeByIds(List<Integer> ids);
    List<Cart> getByUserId(Integer userId);
    Cart getByUserIdAndSkuId(Integer userId, Integer skuId);
    List<Cart> getByUserIdAndgoodsId(Integer userId, Integer goodsId);

    Cart getById(Integer id);

    void clearSelectedCart(Integer userId);

    Cart getCartByUserAndGoodsAndSku(Integer userId, Integer goodsId, Integer skuId);
}