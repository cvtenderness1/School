package com.project.platform.controller;

import com.project.platform.config.Result;
import com.project.platform.entity.Address;
import com.project.platform.service.AddressService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user/address")
public class AddressController {

    @Resource
    private AddressService addressService;

    // 获取收货地址列表
    @GetMapping
    public Result list(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("adminId");
        List<Address> list = addressService.list(userId);
        return Result.success(list);
    }

    // 新增地址
    @PostMapping
    public Result add(@RequestBody Address address, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("adminId");
        address.setUserId(userId);
        addressService.add(address);
        return Result.success();
    }

    // 获取地址详情
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        return Result.success(addressService.getById(id));
    }

    // 修改地址
    @PutMapping("/{id}")
    public Result update(
            @PathVariable Integer id,
            @RequestBody Address address,
            HttpServletRequest request
    ) {
        Integer userId = (Integer) request.getAttribute("adminId");
        address.setId(id);
        address.setUserId(userId);
        addressService.update(address);
        return Result.success();
    }

    // 删除地址
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        addressService.delete(id);
        return Result.success();
    }
}