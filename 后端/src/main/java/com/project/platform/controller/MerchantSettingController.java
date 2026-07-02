package com.project.platform.controller;

import com.project.platform.config.Result;
import com.project.platform.entity.*;
import com.project.platform.service.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/merchant/setting")
public class MerchantSettingController {

    @Resource FreightService freightService;
    @Resource BidService bidService;
    @Resource PeakService peakService;
    @Resource CouponService couponService;

    //运费模板
    @GetMapping("/freight/list")
    public Result freightList(@RequestParam Integer merchantId) {
        List<FreightTemplate> list = freightService.list(merchantId);
        return Result.success(list);
    }
    @PostMapping("/freight/add")
    public Result freightAdd(@RequestBody FreightTemplate t) {
        freightService.add(t);
        return Result.success();
    }
    @PostMapping("/freight/update")
    public Result freightUpdate(@RequestBody FreightTemplate t) {
        freightService.update(t);
        return Result.success();
    }

    @DeleteMapping("/freight/del")
    public Result freightDel(@RequestParam Integer id) {
        freightService.del(id);
        return Result.success();
    }

    // 竞价策略
    @GetMapping("/bid/list")
    public Result bidList(@RequestParam Integer merchantId) {
        return Result.success(bidService.list(merchantId));
    }
    @PostMapping("/bid/add")
    public Result bidAdd(@RequestBody BidStrategy b) {
        bidService.add(b); return Result.success();
    }
    @PostMapping("/bid/update")
    public Result bidUpdate(@RequestBody BidStrategy b) {
        bidService.update(b); return Result.success();
    }
    @DeleteMapping("/bid/del")  // 改成 DeleteMapping
    public Result bidDel(@RequestParam Integer id) {
        bidService.del(id);
        return Result.success();
    }

    //高峰溢价
    @GetMapping("/peak/list")
    public Result peakList(@RequestParam Integer merchantId) {
        return Result.success(peakService.list(merchantId));
    }
    @PostMapping("/peak/add")
    public Result peakAdd(@RequestBody PeakPrice p) {
        peakService.add(p); return Result.success();
    }
    @PostMapping("/peak/update")
    public Result peakUpdate(@RequestBody PeakPrice p) {
        peakService.update(p); return Result.success();
    }
    @DeleteMapping("/peak/del")  // 改成 DeleteMapping
    public Result peakDel(@RequestParam Integer id) {
        peakService.del(id);
        return Result.success();
    }

    // 优惠券
    @GetMapping("/coupon/list")
    public Result couponList(@RequestParam Integer merchantId) {
        return Result.success(couponService.list(merchantId));
    }
    @PostMapping("/coupon/add")
    public Result couponAdd(@RequestBody Coupon c) {
        couponService.add(c); return Result.success();
    }
    @PostMapping("/coupon/update")
    public Result couponUpdate(@RequestBody Coupon c) {
        couponService.update(c); return Result.success();
    }
    @DeleteMapping("/coupon/del")  // 改成 DeleteMapping
    public Result couponDel(@RequestParam Integer id) {
        couponService.del(id);
        return Result.success();
    }
}