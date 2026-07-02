package com.project.platform.controller;

import com.github.pagehelper.PageInfo;
import com.project.platform.config.Result;
import com.project.platform.entity.Cart;
import com.project.platform.service.CartService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 购物车接口
 */
@RestController
@RequestMapping("/user/cart")
public class CartController {
    @Resource
    private CartService cartService;

    /**
     * 分页查询
     */
    @GetMapping("page")
    public Result page(Cart cart,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Cart> pageInfo = cartService.page(cart, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 根据id查询
     */
    @GetMapping("selectById/{id}")
    public Result selectById(@PathVariable("id") Integer id) {
        Cart entity = cartService.selectById(id);
        return Result.success(entity);
    }

    /**
     * 列表
     */
    @GetMapping("list")
    public Result list(Cart cart) {
        return Result.success(cartService.list(cart));
    }

    /**
     * 新增（加入购物车）
     */
    @PostMapping
    public Result add(@RequestBody Cart cart, HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("adminId");
            cart.setUserId(userId);

            // 1. 根据商品类型判断是否重复：规格商品用skuId，默认商品用spuId
            Cart existCart=null;
            if (cart.getSkuId() != null && cart.getSkuId() == -1) {
                // 默认商品（skuId=-1）
                List<Cart> carts = cartService.getByUserIdAndgoodsId(userId,cart.getGoodsId());
                for (Cart cart1 : carts) {
                    if(cart.getGoodsId().equals(cart1.getGoodsId())){
                        existCart = cart1;
                    }
                }

            } else {
                // 规格商品：根据 用户ID + SKU ID 判断
                existCart = cartService.getByUserIdAndSkuId(userId, cart.getSkuId());
            }

            // 存在则数量累加
            if (existCart != null) {
                existCart.setCount(existCart.getCount() + cart.getCount());
                cartService.updateById(existCart);
                return Result.success(existCart);
            }

            // 2. 新增购物车
            cart.setSelected(true);
            cart.setIsEffective(true);
            cart.setCreateTime(LocalDateTime.now());
            cartService.insert(cart);
            return Result.success(cart);
        } catch (Exception e) {
            return Result.error("加入购物车失败：" + e.getMessage());
        }
    }
    /**
     * 更新
     */
    @PutMapping("/{skuId}")
    public Result update(@PathVariable Integer skuId,
                         @RequestBody Cart cart,
                         HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("adminId");

        List<Cart> cartList;

        // 只有当skuId=-1且同时传了goodsId时，才走批量更新
        if (skuId == -1 && cart.getGoodsId() != null) {
            System.out.println("批量更新购物车商品，goodsId=" + cart.getGoodsId());
            cartList = cartService.getByUserIdAndgoodsId(userId, cart.getGoodsId());
        } else {
            // 其他情况（包括skuId!=-1，或skuId=-1但没传goodsId），都按单个skuId更新
            System.out.println("单个更新购物车商品，skuId=" + skuId);
            Cart dbCart = cartService.getByUserIdAndSkuId(userId, skuId);
            cartList = new ArrayList<>();
            if (dbCart != null) {
                cartList.add(dbCart);
            }
        }

        if (cartList.isEmpty()) {
            return Result.error("购物车商品不存在");
        }

        for (Cart dbCart : cartList) {
            if (cart.getSelected() != null) {
                dbCart.setSelected(cart.getSelected());
            }
            if (cart.getCount() != null) {
                dbCart.setCount(cart.getCount());
            }
            cartService.updateById(dbCart);
        }

        return Result.success(cartList);
    }
    /**
     * 批量删除
     */
    @DeleteMapping
    public Result delBatch(@RequestBody Map<String, List<Integer>> map, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("adminId");
        if (userId == null) {
            return Result.error("用户未登录");
        }

        List<Integer> ids = map.get("ids"); // 前端传购物车主键 id，不是 skuId！
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的商品");
        }

        // 直接按购物车主键删除，绝对不会删错！
        cartService.removeByIds(ids);
        return Result.success();
    }
    /**
     * 全选/取消全选
     */
    @PutMapping("/selected")
    public Result selectAll(@RequestBody Map<String, Boolean> map, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("adminId");
        boolean selected = map.get("selected");
        List<Cart> list = cartService.getByUserId(userId);
        for (Cart cart : list) {
            cart.setSelected(selected);
            cartService.updateById(cart);
        }
        return Result.success();
    }

    /**
     * 获取当前用户购物车列表
     */
    @GetMapping
    public Result getCart(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("adminId");
        if (userId == null) {
            // 未登录时返回空列表，不报错
            return Result.success(List.of());
        }
        List<Cart> list = cartService.getByUserId(userId);
        return Result.success(list);
    }
}