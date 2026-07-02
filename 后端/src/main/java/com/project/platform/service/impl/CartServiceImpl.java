package com.project.platform.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.project.platform.entity.Cart;
import com.project.platform.mapper.CartMapper;
import com.project.platform.service.CartService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Resource
    private CartMapper cartMapper;

    @Override
    public Cart getCartByUserAndGoodsAndSku(Integer userId, Integer goodsId, Integer skuId) {
        if (skuId == -1) {
            List<Cart> carts = getByUserIdAndgoodsId(userId, goodsId);
            return carts.isEmpty() ? null : carts.get(0);
        }
        return cartMapper.selectByUserIdAndSkuId(userId, skuId);
    }
    @Override
    public PageInfo<Cart> page(Cart cart, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Cart> page = cartMapper.list(cart);
        return PageInfo.of(page);
    }

    @Override
    public Cart selectById(Integer id) {
        return cartMapper.selectById(id);
    }

    @Override
    public List<Cart> list(Cart cart) {
        return cartMapper.list(cart);
    }

    @Override
    public void insert(Cart entity) {
        if (entity.getSelected() == null) {
            entity.setSelected(true);
        }
        if (entity.getIsEffective() == null) {
            entity.setIsEffective(true);
        }
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        cartMapper.insert(entity);
    }

    @Override
    public void updateById(Cart entity) {
        cartMapper.updateById(entity);
    }

    @Override
    public void removeByIds(List<Integer> ids) {
        cartMapper.removeByIds(ids);
    }

    @Override
    public List<Cart> getByUserId(Integer userId) {
        return cartMapper.selectByUserId(userId);
    }

    @Override
    public Cart getByUserIdAndSkuId(Integer userId, Integer skuId) {
        return cartMapper.selectByUserIdAndSkuId(userId, skuId);
    }
    @Override

    public List<Cart> getByUserIdAndgoodsId(Integer userId, Integer goodsId){
        return cartMapper.selectByUserIdAndgoodsId(userId, goodsId);
    }
    @Override
    public Cart getById(Integer id) {
        return cartMapper.selectById(id);
    }

    public void clearSelectedCart(Integer userId) {
        // 查出所有已选中的购物车
        List<Cart> selectedCarts = cartMapper.selectByUserIdAndSelected(userId);

        if (selectedCarts.isEmpty()) return;

        //提取 ID 批量删除
        List<Integer> ids = selectedCarts.stream()
                .map(Cart::getId)
                .toList();

        cartMapper.removeByIds(ids);
    }
}