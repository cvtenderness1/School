package com.project.platform.controller;
import com.github.pagehelper.PageInfo;
import com.project.platform.config.Result;
import com.project.platform.entity.*;
import com.project.platform.mapper.*;
import com.project.platform.service.AddressService;
import com.project.platform.service.OrderService;
import com.project.platform.service.RiderService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/rider")
public class RiderController {
    @Resource
    private RiderService riderService;
    @Resource
    private RiderMapper riderMapper;
    @Resource
    private OrderService orderService;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private ErrandMapper errandMapper;
    @Resource
    private MerchantMapper merchantMapper;
    @Resource
    private AddressService addressService;
    @Resource
    private AddressMapper addressMapper;
    @Autowired
    private UserMapper userMapper;

    @GetMapping("page")
    public Result page(Rider rider,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Rider> page = riderService.page(rider, pageNum, pageSize);
        return Result.success(page);
    }
    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Rider> list = riderService.selectAll();
        return Result.success(list);
    }
    @PostMapping("add")
    public Result add(@RequestBody Rider rider) {
        riderService.insert(rider);
        return Result.success();
    }

    @PutMapping("update")
    public Result update(@RequestBody Rider rider) {
        riderService.updateById(rider);
        return Result.success();
    }

    @DeleteMapping("delBatch")
    public Result delBatch(@RequestBody List<Integer> ids) {
        riderService.removeByIds(ids);
        return Result.success();
    }

    // 骑手首页：查询所有可抢订单（未被抢）
    @GetMapping("/grabList")
    public Result riderGrabList() {
        OrderMaster query = new OrderMaster();
        // 只查：待接单（1） + 还没有骑手（deliveryId=null）
        query.setOrderStatus(1);
        query.setDeliveryId(null);
        List<OrderMaster> list = orderService.list(query);

        // 给每个订单补全地址信息
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (OrderMaster order : list) {
            Map<String, Object> map = new HashMap<>();
            // 复制订单基本信息
            map.putAll(convertOrderToMap(order));
            map.put("note", order.getNote());
            // 处理外卖订单：取件地址 = 商家地址，送达地址 = 用户收货地址
            if (order.getOrderType() == 1) {
                // 1. 取件地址：商家门店地址
                Merchant merchant = merchantMapper.selectById(order.getMerchantId());
                if (merchant != null) {
                    map.put("startAddress", merchant.getAddress());
                } else {
                    map.put("startAddress", "商家门店");
                }

                // 2. 送达地址：用户收货地址
                 Address address = addressService.getById(Integer.valueOf(order.getAddressId()));
                 map.put("endAddress", address.getFullLocation() + " " + address.getAddress());

            }
            // 处理跑腿订单：从errand表取地址
            else if (order.getOrderType() == 2) {
                Errand errand = errandMapper.selectByOrderId(order.getOrderId());
                if (errand != null) {
                    map.put("startAddress", errand.getStartAddress());
                    map.put("endAddress", errand.getEndAddress());
                    map.put("errandType", errand.getErrandType());

                }
            }

            // 处理期望送达时间文本
            String expectText = switch (order.getDeliveryTimeType() != null ? order.getDeliveryTimeType() : 1) {
                case 0 -> "尽快送达";
                case 1 -> "1小时内送达";
                case 2 -> "2小时内送达";
                case 3 -> "3小时内送达";
                default -> "尽快送达";
            };
            map.put("expectTime", expectText);

            resultList.add(map);
        }

        return Result.success(resultList);
    }

    private Map<String, Object> convertOrderToMap(OrderMaster order) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", order.getOrderId());
        map.put("mainOrderNo", order.getMainOrderNo());
        map.put("userId", order.getUserId());
        map.put("merchantId", order.getMerchantId());
        map.put("addressId", order.getAddressId());
        map.put("orderType", order.getOrderType());
        map.put("orderStatus", order.getOrderStatus());
        map.put("deliveryId", order.getDeliveryId());

        map.put("deliveryFee", order.getDeliveryFee());
        map.put("note", order.getNote());
        map.put("createTime", order.getCreateTime());
        map.put("payTime", order.getPayTime());
        map.put("finishTime", order.getFinishTime());
        map.put("payType", order.getPayType());
        map.put("payChannel", order.getPayChannel());
        map.put("payMoney", order.getPayMoney());
        map.put("postFee", order.getPostFee());
        map.put("totalAmount", order.getTotalAmount());
        map.put("deliveryTimeType", order.getDeliveryTimeType());
        return map;
    }

    // 骑手抢单
    @PostMapping("/grab")
    @Transactional
    public Result riderGrab(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String orderId = params.get("orderId");
        Integer riderId = (Integer) request.getAttribute("adminId");

        if (orderId == null || riderId == null) {
            return Result.error("参数错误");
        }

        // 1. 查询骑手信息
        Rider rider = riderService.selectById(riderId);
        if (rider == null) return Result.error("骑手不存在");

        if (rider.getFrozen() != null && rider.getFrozen() == 1) {
            return Result.error("账号已冻结，无法接单");
        }
        if (rider.getStatus() == null || rider.getStatus() == 0) {
            return Result.error("账号未认证，无法接单");
        }
        if (rider.getWorkStatus() == null || rider.getWorkStatus() == 0) {
            return Result.error("当前处于休息模式，请切换为【在线接单】");
        }


        int done = rider.getTodayOrderCount() == null ? 0 : rider.getTodayOrderCount();
        int max = rider.getDailyOrderLimit() == null ? 15 : rider.getDailyOrderLimit();
        if (done >= max) {
            return Result.error("今日接单已达上限：" + max + "单");
        }
        if (rider.getCreditScore() != null && rider.getCreditScore() < 60) {
            return Result.error("信用分不足，无法接单");
        }
        // 只有订单状态=1（待接单），才能被抢
        int rows = orderMapper.grabOrder(orderId, riderId);
        if (rows <= 0) {
            return Result.error("手慢啦，该订单已被其他骑手抢走");
        }
        // ======================================================================

        // 今日接单 +1
        rider.setTodayOrderCount(done + 1);
        riderService.updateById(rider);

        return Result.success("抢单成功！");
    }

    // 骑手：查看订单详情
    @GetMapping("/detail/{orderId}")
    public Result riderOrderDetail(@PathVariable String orderId) {
        // 1. 查询订单
        OrderMaster order = orderService.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 2. 商品
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);

        // 3. 基础字段
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", orderId);
        map.put("orderType", order.getOrderType());
        map.put("orderStatus", order.getOrderStatus());
        map.put("note", order.getNote());
        map.put("createTime", order.getCreateTime());
        map.put("totalAmount", order.getTotalAmount());
        map.put("deliveryFee", order.getDeliveryFee());
        map.put("items", items);

        if (order.getOrderType() == 1) {
            // 外卖
            String startAddress = "商家门店";
            String endAddress = "用户收货地址";

            // 安全获取商家
            if (order.getMerchantId() != null) {
                Merchant merchant = merchantMapper.selectById(order.getMerchantId());
                if (merchant != null && merchant.getAddress() != null) {
                    startAddress = merchant.getAddress();
                }
            }

            // 安全获取用户地址
            if (order.getAddressId() != null) {
                try {
                    Address address = addressService.getById(Integer.valueOf(order.getAddressId()));
                    if (address != null) {
                        String full = address.getFullLocation() + " " + address.getAddress();
                        endAddress = full.trim();
                    }
                } catch (Exception e) {
                    // 不报错
                }
            }

            map.put("startAddress", startAddress);
            map.put("endAddress", endAddress);

        } else if (order.getOrderType() == 2) {
            // 跑腿
            Errand errand = errandMapper.selectByOrderId(orderId);
            if (errand != null) {
                map.put("startAddress", errand.getStartAddress());
                map.put("endAddress", errand.getEndAddress());
            } else {
                map.put("startAddress", "取件地址");
                map.put("endAddress", "送达地址");
            }
        }

        // 期望时间
        String expectText = switch (order.getDeliveryTimeType() != null ? order.getDeliveryTimeType() : 1) {
            case 1 -> "时间不限";
            case 2 -> "工作日送 (周一至周五)";
            case 3 -> "周末配送 (周六至周日)";
            default -> "尽快送达";
        };
        map.put("expectTime", expectText);

        return Result.success(map);
    }

    //骑手：更新配送状态（开始/完成/取消）
    @PutMapping("/updateStatus")
    public Result updateStatus(@RequestBody OrderMaster order) {
        OrderMaster exist = orderService.getById(order.getOrderId());
        if (exist == null) {
            return Result.error("订单不存在");
        }

        // 状态规则
        if (order.getOrderStatus() == 3) {
            if (exist.getOrderStatus() != 2) {
                return Result.error("只能对待配送订单进行配送");
            }
        }
        // 订单完成 → 给骑手加钱（核心功能）
        else if (order.getOrderStatus() == 4) {
            if (exist.getOrderStatus() != 3) {
                return Result.error("只能对配送中订单完成配送");
            }
            order.setFinishTime(new Date());

            // 开始给骑手加钱
            Integer riderId = exist.getDeliveryId();
            BigDecimal deliveryFee = exist.getDeliveryFee();
            if (riderId != null && deliveryFee != null) {
                // 给骑手余额 + 配送费
                riderService.addBalance(riderId, deliveryFee);
            }
        }
        else if (order.getOrderStatus() == 5) {
            if (exist.getOrderStatus() != 3) {
                return Result.error("只能对配送中订单取消配送");
            }
        }

        // 更新订单
        orderService.updateById(order);

        // 跑腿单同步状态
        if (exist.getOrderType() == 2) {
            Errand errand = errandMapper.selectByOrderId(order.getOrderId());
            if (errand != null) {
                if (order.getOrderStatus() == 2) {
                    errand.setStatus(1);
                } else if (order.getOrderStatus() == 4) {
                    errand.setStatus(2);
                } else if (order.getOrderStatus() == 5) {
                    errand.setStatus(3);
                }
                errandMapper.update(errand);
            }
        }

        return Result.success("操作成功");
    }

    @GetMapping("/list")
    public Result riderOrderList(
            @RequestParam(defaultValue = "1") String type,
            HttpServletRequest request
    ) {
        Integer riderId = (Integer) request.getAttribute("adminId");
        if (riderId == null) {
            return Result.error("请先登录");
        }

        OrderMaster query = new OrderMaster();
        query.setDeliveryId(riderId);

        switch (type) {
            case "1":
                // 待接单（所有人可抢）
                query.setDeliveryId(null);
                query.setOrderStatus(1);
                break;
            case "2":
                // 待配送
                query.setOrderStatus(2);
                break;
            case "3":
                // 配送中
                query.setOrderStatus(3);
                break;
            case "4":
                // 已完成
                query.setOrderStatus(4);
                break;
        }

        List<OrderMaster> list = orderService.list(query);
        return Result.success(list);
    }
    @GetMapping("/delivering")
    public Result deliveringList(HttpServletRequest request) {
        Integer riderId = (Integer) request.getAttribute("adminId");
        List<OrderMaster> list = orderMapper.findDeliveringOrdersByRider(riderId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (OrderMaster order : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", order.getOrderId());
            map.put("orderType", order.getOrderType());
            map.put("totalAmount", order.getTotalAmount());
            map.put("note", order.getNote());

            // ==============================
            // 1. 外卖订单（你原有逻辑）
            // ==============================
            if (order.getOrderType() == 1) {
                Merchant merchant = merchantMapper.selectById(order.getMerchantId());
                if (merchant != null) {
                    map.put("startAddress", merchant.getAddress());
                    map.put("shopName", merchant.getMerchantName());
                    map.put("shopLat", merchant.getLat());
                    map.put("shopLng", merchant.getLng());
                }

                Address addr = addressMapper.selectById(Integer.valueOf(order.getAddressId()));
                if (addr != null) {
                    map.put("endAddress", addr.getFullLocation() + " " + addr.getAddress());
                    map.put("userLat", addr.getLat());
                    map.put("userLng", addr.getLng());
                    map.put("userName", addr.getReceiver());
                    map.put("userPhone", addr.getContact());
                }
            }

            else if (order.getOrderType() == 2) {
                Errand errand = errandMapper.selectByOrderId(order.getOrderId());

                if (errand != null) {
                    User user = userMapper.selectById(errand.getUserId());
                    map.put("startAddress", errand.getStartAddress());
                    map.put("endAddress", errand.getEndAddress());
                    // 给地图用默认坐标（你没存经纬度，我给安全默认值，不报错）
                    map.put("shopLat", 0.0);
                    map.put("shopLng", 0.0);
                    map.put("userLat", 0.0);
                    map.put("userLng", 0.0);
                    // 联系人信息（给隐私通话用）
                    map.put("userName", user.getNickname());
                    map.put("userPhone", user.getTel());
                }
            }

            result.add(map);
        }
        return Result.success(result);
    }
    @GetMapping("/nearbyOrders")
    public Result nearby(
            @RequestParam Double lat,
            @RequestParam Double lng) {

        List<OrderMaster> orders = orderMapper.selectGrabNearby();
        List<Map<String, Object>> list = new ArrayList<>();

        // 校验前端经纬度
        if (lat == null || lng == null) {
            return Result.error("经纬度不能为空");
        }

        for (OrderMaster o : orders) {
            try {
                Map<String, Object> item = new HashMap<>();
                item.put("orderId", o.getOrderId());
                item.put("orderType", o.getOrderType());
                item.put("totalAmount", o.getTotalAmount());

                Merchant m = merchantMapper.selectById(o.getMerchantId());
                // 跳过商家信息缺失或无坐标的订单
                if (m == null || m.getLat() == null || m.getLng() == null) {
                    continue;
                }
                item.put("shopLat", m.getLat());
                item.put("shopLng", m.getLng());
                item.put("startAddress", m.getAddress() != null ? m.getAddress() : "");

                Address a = null;
                if (o.getAddressId() != null) {
                    a = addressMapper.selectById(Integer.valueOf(o.getAddressId()));
                }
                if (a != null) {
                    item.put("userLat", a.getLat());
                    item.put("userLng", a.getLng());
                    String fullAddress = (a.getFullLocation() != null ? a.getFullLocation() : "")
                            + " " + (a.getAddress() != null ? a.getAddress() : "");
                    item.put("endAddress", fullAddress.trim());
                }

                // 计算距离（米）
                double dist = getDistance(lat, lng, m.getLat(), m.getLng());
                //只保留 5000米（5公里）以内的订单
                if (dist > 5000) {
                    continue;
                }
                item.put("distance", (int) dist);

                list.add(item);
            } catch (Exception e) {
                // 单个订单异常不影响整体
                continue;
            }
        }

        //按距离由近到远排序
        list.sort((o1, o2) -> {
            Integer d1 = (Integer) o1.get("distance");
            Integer d2 = (Integer) o2.get("distance");
            return Integer.compare(d1, d2);
        });

        return Result.success(list);
    }

    // 球面距离计算工具（米）
    private double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return r * 2 * Math.asin(Math.sqrt(a)) * 1000;
    }
    @PostMapping("/finishDelivery")
    @Transactional
    public Result finish(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String orderId = (String) params.get("orderId");
        if (orderId == null || orderId.isEmpty()) {
            return Result.error("订单ID不能为空");
        }

        String finishImg = (String) params.get("finishImg");
        Integer riderId = (Integer) request.getAttribute("adminId");
        if (riderId == null) {
            return Result.error("未登录");
        }

        OrderMaster order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getOrderStatus() != 3) {
            return Result.error("只有配送中订单可完成");
        }
        if (!riderId.equals(order.getDeliveryId())) {
            return Result.error("无权限");
        }

        // 更新订单为已完成
        order.setOrderStatus(4);
        order.setFinishTime(new Date());
        order.setFinishImg(finishImg);
        orderMapper.updateById(order);

        BigDecimal deliveryFee = order.getDeliveryFee() == null ? BigDecimal.ZERO : order.getDeliveryFee();
        if (deliveryFee.compareTo(BigDecimal.ZERO) > 0) {
            riderMapper.addBalance(riderId, deliveryFee);
        }
        // ==============================================================================

        return Result.success("送达成功");
    }
    // 骑手上传实时位置
    @PostMapping("/updateLocation")
    public Result updateLocation(
            @RequestParam Integer riderId,
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng) {
        riderService.updateLocation(riderId, lat, lng);
        return Result.success("位置上传成功");
    }
    // 一键提现：余额清零
    @PostMapping("/withdraw/{riderId}")
    @Transactional  // 事务保证安全
    public Result withdraw(@PathVariable Integer riderId) {
        // 1. 查询骑手
        Rider rider = riderMapper.selectById(riderId);
        if (rider == null) {
            return Result.error("骑手不存在");
        }

        BigDecimal balance = rider.getBalance();
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("暂无余额可提现");
        }


        int rows = riderMapper.withdrawAllBalance(riderId);

        if (rows <= 0) {
            return Result.error("提现失败，请重试");
        }

        // 3. 返回成功（可自行加提现流水）
        return Result.success("提现成功，本次提现金额：" + balance);
    }

    // 收入排行榜：按余额降序
    @GetMapping("/rank")
    public Result rank() {
        List<Rider> list = riderService.getBalanceRank();
        return Result.success(list);
    }
    @GetMapping("/{riderId}")
    public Result getById(@PathVariable Integer riderId) {
        Rider rider = riderService.selectById(riderId);
        return Result.success(rider);
    }
    @PutMapping("/batchPassAuth")
    public Result batchPassAuth(@RequestBody Map<String, List<Integer>> map) {
        List<Integer> ids = map.get("ids");
        riderService.batchUpdateStatus(ids, 1);
        return Result.success();
    }
}