package com.project.platform.controller;

import com.project.platform.config.Result;
import com.project.platform.entity.*;
import com.project.platform.mapper.*;
import com.project.platform.service.AddressService;
import com.project.platform.service.CartService;
import com.project.platform.service.OrderService;
import com.project.platform.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/member/order")
public class MemberOrderController {

    @Resource
    private FreightTemplateMapper freightTemplateMapper;

    @Resource
    private PeakPriceMapper peakPriceMapper;

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private OrderItemMapper orderItemMapper;

    @Resource
    private BidStrategyMapper bidStrategyMapper;
    @Resource
    private OrderMapper orderMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private CartService cartService;

    @Resource
    private AddressService addressService;

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private GoodsSkuMapper goodsSkuMapper;

    @Resource
    private RiderMapper riderMapper;
    @Resource
    private AddressMapper addressMapper;
    @Resource
    private ErrandMapper errandMapper;
    @Resource
    private UserService userService;

    // 购物车结算
    @GetMapping("/pre")
    public Result pre(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("adminId");
        if (userId == null) return Result.error("请先登录");
        User user = userService.selectById(userId);
        if (user == null || "禁用".equals(user.getStatus())) {
            return Result.error("您的账号已被禁用，无法结算");
        }
        List<Cart> cartList = cartService.getByUserId(userId).stream()
                .filter(Cart::getSelected)
                .toList();

        if (cartList.isEmpty()) {
            return Result.error("请先选择购物车商品");
        }

        List<Map<String, Object>> goodsList = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        Set<Integer> merchantIdSet = new HashSet<>();

        for (Cart cart : cartList) {
            GoodsSku sku = null;
            good g = goodsMapper.selectById(cart.getGoodsId());

            if (cart.getSkuId() == -1) {
                if (g == null) continue;
            } else {
                sku = goodsSkuMapper.selectById(cart.getSkuId());
                if (sku == null || g == null) continue;
            }

            Integer merchantId = g.getMerchantId();
            merchantIdSet.add(merchantId);
            Map<String, Object> item = new HashMap<>();
            item.put("attrsText", cart.getAttrsText());
            item.put("count", cart.getCount());
            item.put("id", g.getGoodsId().toString());
            item.put("name", g.getGoodsName());

            item.put("skuId", cart.getSkuId().toString());

            BigDecimal payPrice, price;
            if (sku != null) {
                payPrice = sku.getDiscountPrice() == null ? BigDecimal.ZERO : sku.getDiscountPrice();
                price = sku.getPrice() == null ? BigDecimal.ZERO : sku.getPrice();
                String skuImage = sku.getImage();
                if (skuImage == null || skuImage.isBlank()) {
                    skuImage = g.getImg();
                }
                item.put("picture", skuImage);
            } else {
                payPrice = cart.getNowPrice() == null ? BigDecimal.ZERO : new BigDecimal(cart.getNowPrice().toString());
                price = cart.getPrice() == null ? BigDecimal.ZERO : new BigDecimal(cart.getPrice().toString());
                String cartImage = cart.getPicture();
                if (cartImage == null || cartImage.isBlank()) {
                    cartImage = g.getImg();
                }
                item.put("picture", cartImage);
            }

            item.put("payPrice", payPrice.toString());
            item.put("price", price.toString());

            BigDecimal itemTotal = payPrice.multiply(new BigDecimal(cart.getCount()));
            item.put("totalPayPrice", itemTotal.toString());
            item.put("totalPrice", itemTotal.toString());

            goodsList.add(item);
            totalPrice = totalPrice.add(itemTotal);
        }

        // 经纬度 + 距离计算
        BigDecimal postFee = BigDecimal.ZERO;
        Map<Integer, BigDecimal> peakMap = new HashMap<>();

        List<Address> userAddresses = addressService.list(userId);
        BigDecimal ulat = BigDecimal.ZERO;
        BigDecimal ulng = BigDecimal.ZERO;

        // 安全获取用户地址经纬度
        if (userAddresses != null && !userAddresses.isEmpty()) {
            Address a = userAddresses.get(0);
            if (a != null) {
                ulat = a.getLat() != null ? a.getLat() : BigDecimal.ZERO;
                ulng = a.getLng() != null ? a.getLng() : BigDecimal.ZERO;
            }
        }

        for (Integer mid : merchantIdSet) {
            Merchant m = merchantMapper.selectById(mid);
            //商家为空直接跳过
            if (m == null) continue;

            try {
                double dist = calculateDistance(
                        BigDecimal.valueOf(m.getLat() != null ? m.getLat() : 0),
                        BigDecimal.valueOf(m.getLng() != null ? m.getLng() : 0),
                        ulat,
                        ulng
                );
                BigDecimal freight = calcFreight(mid, dist);
                BigDecimal rate = getPeakRate(mid);
                peakMap.put(mid, rate);
                postFee = postFee.add(freight);
                System.out.println("商家ID=" + mid + "，运费=" + freight + "，高峰倍率=" + rate + "，累加后总运费=" + postFee);
            } catch (Exception e) {
                // 单个商家计算失败不影响整体
                postFee = postFee.add(BigDecimal.ZERO);
            }
        }
        request.setAttribute("peakMap", peakMap);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPrice", totalPrice.doubleValue());
        summary.put("postFee", postFee);
        summary.put("totalPayPrice", totalPrice.doubleValue());

        Map<String, Object> result = new HashMap<>();
        result.put("goods", goodsList);
        result.put("summary", summary);
        result.put("userAddresses", userAddresses);

        return Result.success(result);
    }
    // 立即购买
    @GetMapping("/pre/now")
    public Result preNow(
            @RequestParam(required = false) Integer skuId,
            @RequestParam Integer goodsId,
            @RequestParam Integer count,
            HttpServletRequest request) {

        Integer userId = (Integer) request.getAttribute("adminId");
        if (userId == null) return Result.error("请先登录");

        User user = userService.selectById(userId);
        if (user == null || "禁用".equals(user.getStatus())) {
            return Result.error("您的账号已被禁用，无法购买");
        }

        good goods = goodsMapper.selectById(goodsId);
        if (goods == null) return Result.error("商品不存在");

        Map<String, Object> item = new HashMap<>();
        BigDecimal itemTotal;

        if (skuId != null && skuId == -1) {
            item.put("attrsText", "默认");
            item.put("skuId", -1);
            item.put("picture", goods.getImg());

            BigDecimal price = goods.getPrice() == null ? BigDecimal.ZERO : goods.getPrice();
            BigDecimal discountPrice = goods.getDiscountPrice() == null ? BigDecimal.ZERO : goods.getDiscountPrice();

            item.put("price", price.toString());
            item.put("payPrice", discountPrice.toString());

            itemTotal = discountPrice.multiply(new BigDecimal(count));
        } else {
            GoodsSku goodssku = goodsSkuMapper.selectById(skuId);
            if (goodssku == null) {
                return Result.error("规格不存在");
            }

            BigDecimal price = goodssku.getPrice() == null ? BigDecimal.ZERO : goodssku.getPrice();
            BigDecimal discountPrice = goodssku.getDiscountPrice() == null ? BigDecimal.ZERO : goodssku.getDiscountPrice();

            item.put("attrsText", goodssku.getSkuNameArr());
            item.put("skuId", skuId);
            String skuImage = goodssku.getImage();
            if (skuImage == null || skuImage.isBlank()) {
                skuImage = goods.getImg(); // sku没图 → 取商品主图
            }
            item.put("picture", skuImage == null ? "" : skuImage);
            item.put("price", price.toString());
            item.put("payPrice", discountPrice.toString());

            itemTotal = discountPrice.multiply(new BigDecimal(count));
        }

        item.put("id", goodsId.toString());
        item.put("name", goods.getGoodsName());
        item.put("count", count);
        item.put("totalPayPrice", itemTotal.toString());
        item.put("totalPrice", itemTotal.toString());

        List<Map<String, Object>> goodsList = new ArrayList<>();
        goodsList.add(item);

        // 运费 + 高峰溢价
        List<Address> userAddresses = addressService.list(userId);
        BigDecimal ulat = BigDecimal.ZERO;
        BigDecimal ulng = BigDecimal.ZERO;

        if (userAddresses != null && !userAddresses.isEmpty()) {
            Address a = userAddresses.get(0);
            if (a != null) {
                ulat = a.getLat() != null ? a.getLat() : BigDecimal.ZERO;
                ulng = a.getLng() != null ? a.getLng() : BigDecimal.ZERO;
            }
        }

        Integer merchantId = goods.getMerchantId();
        Merchant m = merchantMapper.selectById(merchantId);

        double dist = 0.0;
        BigDecimal peakRate = new BigDecimal("1.0");
        BigDecimal postFee = BigDecimal.ZERO;

        // 商家不为空才计算距离和运费
        if (m != null) {
            try {
                BigDecimal mLat = BigDecimal.valueOf(m.getLat() != null ? m.getLat() : 0);
                BigDecimal mLng = BigDecimal.valueOf(m.getLng() != null ? m.getLng() : 0);
                dist = calculateDistance(mLat, mLng, ulat, ulng);
            } catch (Exception e) {
                dist = 0.0;
            }
            BigDecimal freight = calcFreight(merchantId, dist);
            postFee = freight;
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPrice", itemTotal.doubleValue());
        summary.put("postFee", postFee);
        summary.put("totalPayPrice", itemTotal.doubleValue());

        Map<String, Object> result = new HashMap<>();
        result.put("goods", goodsList);
        result.put("summary", summary);
        result.put("userAddresses", userAddresses);

        return Result.success(result);
    }
    @PostMapping
    @Transactional
    public Result create(@RequestBody Map<String, Object> data, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("adminId");
        if (userId == null) return Result.error("请先登录");
        User user = userService.selectById(userId);
        if (user == null || "禁用".equals(user.getStatus())) {
            return Result.error("您的账号已被禁用，无法创建订单");
        }
        Object addressIdObj = data.get("addressId");
        if (addressIdObj == null) return Result.error("请选择收货地址");
        String addressId = String.valueOf(addressIdObj);

        String note = (String) data.get("buyerMessage");
        List<Map<String, Object>> goodsList = (List<Map<String, Object>>) data.get("goods");

        if (goodsList == null || goodsList.isEmpty()) {
            return Result.error("订单无商品");
        }
        Integer deliveryTimeType = 1; // 数据库默认值
        Object dttObj = data.get("deliveryTimeType");
        System.out.println("原始deliveryTimeType参数值: " + dttObj);

        if (dttObj != null) {
            try {
                deliveryTimeType = Integer.parseInt(String.valueOf(dttObj));
                System.out.println("解析后deliveryTimeType: " + deliveryTimeType);
            } catch (NumberFormatException e) {
                System.err.println("deliveryTimeType 解析失败，使用默认值1");
                deliveryTimeType = 1;
            }
        }
        // 全局总优惠金额
        BigDecimal globalDiscountPrice = BigDecimal.ZERO;
        try {
            if (data.get("discountPrice") != null) {
                globalDiscountPrice = new BigDecimal(data.get("discountPrice").toString());
            }
        } catch (Exception e) {
            globalDiscountPrice = BigDecimal.ZERO;
        }

        Map<Integer, List<Map<String, Object>>> merchantGoodsMap = new HashMap<>();
        for (Map<String, Object> item : goodsList) {
            Object goodsIdObj = item.get("goodsId");
            if (goodsIdObj == null) continue;

            Integer goodsId = Integer.valueOf(goodsIdObj.toString());
            good goodsInfo = goodsMapper.selectById(goodsId);
            if (goodsInfo == null) continue;

            Integer merchantId = goodsInfo.getMerchantId();
            merchantGoodsMap.computeIfAbsent(merchantId, k -> new ArrayList<>()).add(item);
        }

        String mainOrderNo = "OD" + System.currentTimeMillis();

        //定义剩余可抵扣的优惠金额，用于在多个商家订单中循环分摊
        BigDecimal remainingDiscount = globalDiscountPrice;

        for (Integer merchantId : merchantGoodsMap.keySet()) {
            List<Map<String, Object>> goods = merchantGoodsMap.get(merchantId);
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant == null) continue;

            // 运费 + 高峰溢价
            Map<Integer, BigDecimal> peakMap = (Map<Integer, BigDecimal>) request.getAttribute("peakMap");
            BigDecimal peakRate = BigDecimal.ONE;
            if (peakMap != null) {
                peakRate = peakMap.getOrDefault(merchantId, BigDecimal.ONE);
            } else {
                peakRate = getPeakRate(merchantId);
            }

            Address addr = addressMapper.selectById(Integer.valueOf(addressId));
            if (addr == null) return Result.error("收货地址不存在");

            double dist = 0.0;
            try {
                BigDecimal mLat = BigDecimal.valueOf(merchant.getLat() != null ? merchant.getLat() : 0);
                BigDecimal mLng = BigDecimal.valueOf(merchant.getLng() != null ? merchant.getLng() : 0);
                BigDecimal uLat = addr.getLat() != null ? addr.getLat() : BigDecimal.ZERO;
                BigDecimal uLng = addr.getLng() != null ? addr.getLng() : BigDecimal.ZERO;
                dist = calculateDistance(mLat, mLng, uLat, uLng);
            } catch (Exception e) {
                dist = 0.0;
            }

            BigDecimal freight = calcFreight(merchantId, dist);
            BigDecimal postFee = freight;// 运费受高峰期溢价影响

            BigDecimal totalAmount = BigDecimal.ZERO;
            String orderId = mainOrderNo + "_" + merchantId;

            OrderMaster order = new OrderMaster();
            order.setOrderId(orderId);
            order.setMainOrderNo(mainOrderNo);
            order.setUserId(userId);
            order.setAddressId(addressId);
            order.setNote(note);
            order.setMerchantId(merchantId);
            Integer orderType = (Integer) data.get("orderType");
            order.setOrderType(orderType == null ? 1 : orderType);
            order.setOrderStatus(0);
            order.setPostFee(postFee);
            order.setDeliveryFee(postFee);
            order.setDeliveryTimeType(deliveryTimeType);
            // 商品计算
            for (Map<String, Object> item : goods) {
                Object countObj = item.get("count");
                if (countObj == null) continue;
                Integer count = (Integer) countObj;

                Object skuIdObj = item.get("skuId");
                Integer skuId = -1;
                if (skuIdObj != null && !"null".equals(skuIdObj.toString())) {
                    skuId = Integer.valueOf(skuIdObj.toString());
                }

                Object goodsIdObj = item.get("goodsId");
                if (goodsIdObj == null) continue;
                Integer goodsId = Integer.valueOf(goodsIdObj.toString());
                good goodsInfo = goodsMapper.selectById(goodsId);
                if (goodsInfo == null) continue;

                BigDecimal payPrice = BigDecimal.ZERO;
                String skuAttrs = "默认规格";
                String picture = goodsInfo.getImg();

                if (skuId == -1) {
                    payPrice = goodsInfo.getDiscountPrice() != null ? goodsInfo.getDiscountPrice() : BigDecimal.ZERO;
                    Integer stock = goodsInfo.getStock() == null ? 0 : goodsInfo.getStock();
                    if (stock < count) return Result.error("商品【" + goodsInfo.getGoodsName() + "】库存不足");
                    goodsInfo.setStock(stock - count);
                    goodsMapper.updateStock(goodsInfo);
                } else {
                    GoodsSku sku = goodsSkuMapper.selectById(skuId);
                    if (sku == null) return Result.error("商品规格已失效");
                    payPrice = sku.getDiscountPrice() != null ? sku.getDiscountPrice() : BigDecimal.ZERO;
                    skuAttrs = sku.getSkuNameArr();
                    String skuImage = sku.getImage();
                    if (skuImage == null || skuImage.isBlank()) skuImage = goodsInfo.getImg();
                    picture = skuImage;

                    Integer skuStock = sku.getStock() == null ? 0 : sku.getStock();
                    if (skuStock < count) return Result.error("商品规格【" + sku.getSkuNameArr() + "】库存不足");
                    sku.setStock(skuStock - count);
                    goodsSkuMapper.updateStock(sku);
                }

                BigDecimal itemTotal = payPrice.multiply(new BigDecimal(count));

                totalAmount = totalAmount.add(itemTotal);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(orderId);
                orderItem.setGoodsId(goodsId);
                orderItem.setSkuId(skuId);
                orderItem.setGoodsName(goodsInfo.getGoodsName());
                orderItem.setSkuAttrs(skuAttrs);
                orderItem.setPicture(picture);
                orderItem.setPrice(goodsInfo.getPrice() != null ? goodsInfo.getPrice() : BigDecimal.ZERO);
                orderItem.setPayPrice(payPrice);
                orderItem.setTotalPrice(itemTotal);
                orderItem.setQuantity(count);
                orderItemMapper.insert(orderItem);
            }

            order.setTotalAmount(totalAmount);
            BigDecimal originTotal = totalAmount.add(postFee); // 本商家总费用 = 商品总额 + 运费

            // 延续：动态计算当前子订单能够抵扣的优惠券金额
            BigDecimal currentOrderDiscount = BigDecimal.ZERO;
            if (remainingDiscount.compareTo(BigDecimal.ZERO) > 0) {
                if (originTotal.compareTo(remainingDiscount) >= 0) {
                    currentOrderDiscount = remainingDiscount; // 剩余优惠券够扣，全扣完
                    remainingDiscount = BigDecimal.ZERO;
                } else {
                    currentOrderDiscount = originTotal; // 当前订单金额不够扣，只扣到0
                    remainingDiscount = remainingDiscount.subtract(originTotal); // 留给下个商家的子订单继续扣
                }
            }

            BigDecimal finalPay = originTotal.subtract(currentOrderDiscount);
            if (finalPay.compareTo(BigDecimal.ZERO) < 0) finalPay = BigDecimal.ZERO;

            order.setPayMoney(finalPay);

            orderMapper.insert(order);

            // 插入 errand 表
            if (order.getOrderType() == 2) {
                Errand errand = new Errand();
                errand.setOrderId(order.getOrderId());
                errand.setUserId(Math.toIntExact(Long.valueOf(userId)));
                errand.setErrandType(4);
                errand.setRemark(note);
                errand.setStatus(0);
                String merchantAddress = merchant.getAddress() == null ? "" : merchant.getAddress();
                errand.setStartAddress(merchantAddress);
                String userAddress = addr.getFullLocation() + " " + addr.getAddress();
                errand.setEndAddress(userAddress);
                errandMapper.insert(errand);
            }
        }

        Boolean fromCart = (Boolean) data.get("fromCart");
        if (fromCart != null && fromCart) {
            cartService.clearSelectedCart(userId);
        }

        Map<String, String> result = new HashMap<>();
        result.put("id", mainOrderNo);
        return Result.success(result);
    }

    // 订单详情
    @GetMapping("/{id}")
    public Result detail(@PathVariable String id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("adminId");
        if (userId == null) return Result.error("请先登录");

        String mainOrderNo = id.contains("_") ? id.split("_")[0] : id;
        if (mainOrderNo == null) mainOrderNo = id;
        List<OrderMaster> orders = orderMapper.selectBymin(mainOrderNo);
        if (orders.isEmpty()) return Result.error("订单不存在");

        OrderMaster mainOrder = orders.get(0);

        Address addr = null;
        String addressId = mainOrder.getAddressId();
        if (addressId != null && !addressId.isBlank()) {
            try {
                addr = addressService.getById(Integer.valueOf(addressId));
            } catch (NumberFormatException e) {
                return Result.error("订单地址信息格式错误");
            }
        }
        if (addr == null) {
            return Result.error("订单收货地址不存在");
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal postFee = BigDecimal.ZERO;
        BigDecimal payMoney = BigDecimal.ZERO;
        List<String> allFinishImages = new ArrayList<>();
        for (OrderMaster order : orders) {
            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getOrderId());
            items.addAll(orderItems);
            totalAmount = totalAmount.add(order.getTotalAmount());
            postFee = postFee.add(order.getPostFee());
            payMoney = payMoney.add(order.getPayMoney());
            // 收集每个子订单的送达凭证
            if (order.getFinishImg() != null && !order.getFinishImg().isBlank()) {
                allFinishImages.add(order.getFinishImg());
            }
        }

        Merchant merchant = null;
        if (!items.isEmpty()) {
            OrderItem firstItem = items.get(0);
            good goods = goodsMapper.selectById(firstItem.getGoodsId());
            if (goods != null) {
                merchant = merchantMapper.selectById(goods.getMerchantId());
            }
        }

        Rider rider = null;
        if (mainOrder.getDeliveryId() != null) {
            rider = riderMapper.selectById(mainOrder.getDeliveryId());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", mainOrderNo);
        map.put("orderState", mainOrder.getOrderStatus());
        map.put("countdown", 1500);
        map.put("skus", items);
        map.put("receiverContact", addr.getReceiver());
        map.put("receiverMobile", addr.getContact());
        map.put("receiverAddress", addr.getFullLocation() + " " + addr.getAddress());
        map.put("receiverLat", addr.getLat());
        map.put("receiverLng", addr.getLng());
        map.put("createTime", mainOrder.getCreateTime());
        map.put("totalMoney", totalAmount);
        map.put("postFee", postFee);
        map.put("payMoney", payMoney);

        if (merchant != null) {
            map.put("merchantName", merchant.getMerchantName());
            map.put("merchantAddress", merchant.getAddress());
            map.put("merchantLat", merchant.getLat());
            map.put("merchantLng", merchant.getLng());
        }

        if (rider != null) {
            map.put("riderLat", rider.getLat());
            map.put("riderLng", rider.getLng());
        }
        map.put("finishImgList", allFinishImages);
        User user = userService.selectById(userId);
        if (user != null) {
            map.put("userAvatar", user.getAvatarUrl());
        }
        return Result.success(map);
    }

    //取消订单
    @PutMapping("/{id}/cancel")
    public Result cancel(@PathVariable String id) {
        String mainOrderNo = id.contains("_") ? id.split("_")[0] : id;

        List<OrderMaster> orders = orderMapper.selectBymin(mainOrderNo);
        if (orders == null || orders.isEmpty()) {
            return Result.error("订单不存在");
        }
        // 1. 恢复所有子订单的商品库存
        for (OrderMaster order : orders) {
            // 只对未取消、未完成的订单恢复库存
            if (order.getOrderStatus() == 0 || order.getOrderStatus() == 1 || order.getOrderStatus() == 2) {
                List<OrderItem> items = orderItemMapper.selectByOrderId(order.getOrderId());
                for (OrderItem item : items) {
                    if (item.getSkuId() == -1) {
                        // 普通商品：恢复 goods 表库存
                        good goods = goodsMapper.selectById(item.getGoodsId());
                        if (goods != null) {
                            int newStock = (goods.getStock() == null ? 0 : goods.getStock()) + item.getQuantity();
                            goods.setStock(newStock);
                            goodsMapper.updateStock(goods);
                        }
                    } else {
                        // SKU 商品：恢复 goods_sku 表库存
                        GoodsSku sku = goodsSkuMapper.selectById(item.getSkuId());
                        if (sku != null) {
                            int newStock = (sku.getStock() == null ? 0 : sku.getStock()) + item.getQuantity();
                            sku.setStock(newStock);
                            goodsSkuMapper.updateStock(sku);
                        }
                    }
                }
            }
            // 2. 更新订单状态为已取消(5)
            order.setOrderStatus(5);
            orderMapper.updateById(order);
        }

        OrderMaster order = orders.get(0);
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getOrderId());
        Address addr = addressService.getById(Integer.valueOf(order.getAddressId()));

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("orderState", 5);
        map.put("countdown", 0);
        map.put("skus", items);
        map.put("receiverContact", addr.getReceiver());
        map.put("receiverMobile", addr.getContact());
        map.put("receiverAddress", addr.getFullLocation() + " " + addr.getAddress());
        map.put("createTime", order.getCreateTime());
        map.put("totalMoney", order.getTotalAmount());
        map.put("postFee", order.getPostFee());
        map.put("payMoney", order.getPayMoney());

        return Result.success(map);
    }

    // 确认收货
    @PutMapping("/{id}/receipt")
    public Result receipt(@PathVariable String id) {
        String mainOrderNo = id.contains("_") ? id.split("_")[0] : id;

        List<OrderMaster> orders = orderMapper.selectBymin(mainOrderNo);
        if (orders == null || orders.isEmpty()) {
            return Result.error("订单不存在");
        }
        for (OrderMaster o : orders) {
            o.setOrderStatus(4);
            orderMapper.updateById(o);
        }

        OrderMaster order = orders.get(0);
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getOrderId());
        Address addr = addressService.getById(Integer.valueOf(order.getAddressId()));

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("orderState", 4);
        map.put("countdown", 0);
        map.put("skus", items);
        map.put("receiverContact", addr.getReceiver());
        map.put("receiverMobile", addr.getContact());
        map.put("receiverAddress", addr.getFullLocation() + " " + addr.getAddress());
        map.put("createTime", order.getCreateTime());
        map.put("totalMoney", order.getTotalAmount());
        map.put("postFee", order.getPostFee());
        map.put("payMoney", order.getPayMoney());

        return Result.success(map);
    }

    @DeleteMapping
    public Result delete(@RequestBody Map<String, List<String>> data) {
        for (String mainId : data.get("ids")) {
            List<OrderMaster> orders = orderMapper.selectBymin(mainId);
            for (OrderMaster o : orders) {
                orderMapper.removeByIds(List.of(o.getOrderId()));
            }
        }
        return Result.success();
    }

    // 模拟发货
    @GetMapping("/consignment/{id}")
    public Result consignment(@PathVariable String id) {
        List<OrderMaster> orders = orderMapper.selectBymin(id);
        for (OrderMaster o : orders) {
            o.setOrderStatus(3);
            orderMapper.updateById(o);
        }
        return Result.success();
    }

    private double calculateDistance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return 0;
        }
        double lat1d = lat1.doubleValue();
        double lng1d = lng1.doubleValue();
        double lat2d = lat2.doubleValue();
        double lng2d = lng2.doubleValue();

        double EARTH_RADIUS = 6371000;
        double radLat1 = Math.toRadians(lat1d);
        double radLat2 = Math.toRadians(lat2d);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1d) - Math.toRadians(lng2d);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        return Math.round(s);
    }

    private int getEstimateMinutes(double meters) {
        if (meters <= 0) return 10;
        int min = (int) Math.ceil(meters / 300.0);
        return Math.max(5, Math.min(60, min));
    }

    //获取高峰溢价倍率
    private BigDecimal getPeakRate(Integer merchantId) {
        LocalDateTime now = LocalDateTime.now();
        int week = now.getDayOfWeek().getValue();
        String hm = String.format("%02d:%02d", now.getHour(), now.getMinute());

        List<PeakPrice> list = peakPriceMapper.list(merchantId);
        BigDecimal rate = new BigDecimal("1.0");

        for (PeakPrice p : list) {
            if (p.getStatus() != 1) continue;
            if (p.getWeekDays() == null || p.getTimeRange() == null) continue;

            boolean weekMatch = Arrays.stream(p.getWeekDays().split(","))
                    .anyMatch(w -> w.trim().equals(String.valueOf(week)));
            if (!weekMatch) continue;

            String[] t = p.getTimeRange().split("-");
            if (t.length != 2) continue;
            if (hm.compareTo(t[0]) >= 0 && hm.compareTo(t[1]) <= 0) {
                rate = p.getRate();
                break;
            }
        }
        return rate;
    }

    // 计算真实运费
    private BigDecimal calcFreight(Integer merchantId, double distanceMeter) {

        // 获取竞价策略
        List<BidStrategy> strategyList = bidStrategyMapper.listByMerchantId(merchantId);
        BidStrategy useStrategy = (strategyList != null && !strategyList.isEmpty()) ? strategyList.get(0) : null;

        // 先算基础运费按距离+运费模板
        List<FreightTemplate> list = freightTemplateMapper.list(merchantId);
        BigDecimal baseFreight = BigDecimal.ZERO;

        if (list.isEmpty()) {
            if (useStrategy != null && useStrategy.getBaseBid() != null) {
                baseFreight = useStrategy.getBaseBid();
            }
        } else {
            FreightTemplate ft = list.get(0);
            BigDecimal basePrice = ft.getBasePrice() == null ? BigDecimal.ZERO : ft.getBasePrice();
            Integer baseDistance = ft.getBaseDistance() == null ? 0 : ft.getBaseDistance();
            BigDecimal extraPrice = ft.getExtraPrice() == null ? BigDecimal.ZERO : ft.getExtraPrice();

            double extraKm = Math.max(0, distanceMeter - baseDistance) / 1000.0;
            BigDecimal extraTotal = extraPrice.multiply(BigDecimal.valueOf(extraKm));
            baseFreight = basePrice.add(extraTotal);
        }

        // 乘高峰倍率
        BigDecimal peakRate = getPeakRate(merchantId);
        peakRate = peakRate == null ? BigDecimal.ONE : peakRate;
        BigDecimal dynamicFreight = baseFreight.multiply(peakRate);
        System.out.println("----------------------------");
        System.out.println("商家ID：" + merchantId);
        System.out.println("距离（米）：" + distanceMeter);
        System.out.println("基础运费：" + baseFreight);
        System.out.println("高峰倍率：" + peakRate);
        System.out.println("高峰后运费：" + dynamicFreight);
        // ----------------------------------------------------
        // 竞价策略
        if (useStrategy != null) {
            BigDecimal minBid = useStrategy.getMinBid() == null ? BigDecimal.ZERO : useStrategy.getMinBid();
            BigDecimal maxBid = useStrategy.getMaxBid() == null ? BigDecimal.ZERO : useStrategy.getMaxBid();

            // 低于最低价 → 强制用最低价
            if (minBid.compareTo(BigDecimal.ZERO) >= 0 && dynamicFreight.compareTo(minBid) < 0) {
                dynamicFreight = minBid;
            }

            // 高于最高价 → 强制用最高价
            if (maxBid.compareTo(BigDecimal.ZERO) > 0 && dynamicFreight.compareTo(maxBid) > 0) {
                dynamicFreight = maxBid;
            }
            System.out.println("竞价最低：" + minBid);
            System.out.println("竞价最高：" + maxBid);
            System.out.println("竞价后最终运费：" + dynamicFreight);
        } else {
            // 没有竞价策略，沿用原来的 50 元上限
            if (dynamicFreight.compareTo(new BigDecimal("50")) > 0) {
                dynamicFreight = new BigDecimal("50");
            }
        }

        return dynamicFreight;
    }
    //流程
    @GetMapping("/{id}/logistics")
    public Result logistics(@PathVariable String id) {
        List<OrderMaster> orders = orderMapper.selectBymin(id);

        Map<String, Object> company = new HashMap<>();
        company.put("name", "系统跑腿");
        company.put("number", "YT" + System.currentTimeMillis());
        company.put("tel", "5809");

        List<Map<String, Object>> list = new ArrayList<>();

        for (OrderMaster order : orders) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", "1");
            item.put("time", new Date().toString());

            if (order.getOrderStatus().equals(3)) {
                String text = "订单已发货，配送中";

                try {
                    Address address = addressMapper.selectById(Integer.valueOf(order.getAddressId()));
                    Rider rider = riderMapper.selectById(order.getDeliveryId());

                    if (address != null && rider != null) {
                        double distance = calculateDistance(
                                rider.getLat(),
                                rider.getLng(),
                                address.getLat(),
                                address.getLng()
                        );
                        int minutes = getEstimateMinutes(distance);
                        text = String.format("订单已发货，配送中，预计%s分钟内送达", minutes);
                    }
                } catch (Exception e) {
                    text = "订单已发货，骑手正在配送";
                }

                item.put("text", text);
            } else if (order.getOrderStatus().equals(4)) {
                item.put("text", "订单已完成");
            }

            list.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("company", company);
        result.put("count", list.size());
        result.put("list", list);
        return Result.success(result);
    }

    // 再次购买
    @GetMapping("/repurchase/{id}")
    public Result repurchase(@PathVariable String id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("adminId");
        if (userId == null) return Result.error("请先登录");

        String mainOrderNo = id.contains("_") ? id.split("_")[0] : id;
        List<OrderMaster> orders = orderMapper.selectBymin(mainOrderNo);
        if (orders == null || orders.isEmpty()) {
            return Result.error("订单不存在");
        }
        List<OrderItem> allItems = new ArrayList<>();
        for (OrderMaster order : orders) {
            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getOrderId());
            allItems.addAll(items);
        }

        for (OrderItem item : allItems) {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setGoodsId(item.getGoodsId());
            cart.setSkuId(item.getSkuId());
            cart.setCount(item.getQuantity());
            cart.setAttrsText(item.getSkuAttrs());
            cart.setPicture(item.getPicture());
            cart.setSelected(true);
            cart.setNowPrice(item.getPayPrice().doubleValue());
            cart.setPrice(item.getPrice().doubleValue());
            cart.setIsEffective(true);
            cart.setCreateTime(LocalDateTime.now());

            Cart exist = cartService.getCartByUserAndGoodsAndSku(
                    userId, item.getGoodsId(), item.getSkuId());

            if (exist != null) {
                exist.setCount(exist.getCount() + item.getQuantity());
                cartService.updateById(exist);
            } else {
                cartService.insert(cart);
            }
        }

        return pre(request);
    }

    // 订单列表
    @GetMapping
    public Result list(
            @RequestParam(required = false) Integer orderState,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Integer userId = (Integer) request.getAttribute("adminId");
        if (userId == null) return Result.error("请先登录");

        List<OrderMaster> allOrders = orderMapper.selectByUserIdAndState(userId,orderState);
        if (allOrders.isEmpty()) {
            return Result.success(Map.of("list", List.of(), "hasNextPage", false));
        }

        Map<String, OrderMaster> mainOrderMap = new HashMap<>();
        for (OrderMaster order : allOrders) {
            String mainOrderNo = order.getMainOrderNo();
            if (mainOrderNo == null) mainOrderNo = order.getOrderId();

            if (!mainOrderMap.containsKey(mainOrderNo)) {
                mainOrderMap.put(mainOrderNo, order);
            }
        }

        List<OrderMaster> mainOrders = new ArrayList<>(mainOrderMap.values());
        mainOrders.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));

        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, mainOrders.size());
        List<OrderMaster> pageList = mainOrders.subList(start, end);

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (OrderMaster order : pageList) {
            String mainOrderNo = order.getMainOrderNo() == null ? order.getOrderId() : order.getMainOrderNo();

            List<OrderMaster> childOrders = orderMapper.selectBymin(mainOrderNo);
            List<OrderItem> allItems = new ArrayList<>();

            for (OrderMaster child : childOrders) {
                List<OrderItem> items = orderItemMapper.selectByOrderId(child.getOrderId());
                allItems.addAll(items);
            }

            BigDecimal totalPayMoney = BigDecimal.ZERO;
            for (OrderMaster child : childOrders) {
                if (child.getPayMoney() != null) {
                    totalPayMoney = totalPayMoney.add(child.getPayMoney());
                }
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", mainOrderNo);
            map.put("orderState", order.getOrderStatus());
            map.put("createTime", order.getCreateTime());
            map.put("totalNum", allItems.size());
            map.put("payMoney", totalPayMoney);
            map.put("skus", allItems);

            resultList.add(map);
        }

        boolean hasNextPage = mainOrders.size() > pageNum * pageSize;
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", resultList);
        pageResult.put("hasNextPage", hasNextPage);

        return Result.success(pageResult);
    }

    // 店铺优惠券
    @GetMapping("/coupon")
    public Result coupon(@RequestParam Integer goodsId) {
        good goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            return Result.success(new ArrayList<>());
        }
        Integer merchantId = goods.getMerchantId();
        List<Coupon> list = couponMapper.list(merchantId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Coupon coupon : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", coupon.getId());
            map.put("name", coupon.getName());
            map.put("min", coupon.getMinAmount());
            map.put("value", coupon.getReducePrice());
            result.add(map);
        }
        return Result.success(result);
    }

}