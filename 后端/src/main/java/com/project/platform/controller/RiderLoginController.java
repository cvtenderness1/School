package com.project.platform.controller;

import cn.hutool.core.util.StrUtil;
import com.project.platform.config.JwtUtil;
import com.project.platform.config.Result;
import com.project.platform.dto.RiderLoginDTO;
import com.project.platform.dto.RiderRegisterDTO;
import com.project.platform.dto.RiderSimpleLoginDTO;
import com.project.platform.entity.Rider;
import com.project.platform.service.RiderService;
import com.project.platform.vo.RiderLoginVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rider")
public class RiderLoginController {

    @Resource
    private RiderService riderService;

    @Resource
    private JwtUtil jwtUtil;

    // 1. 账号密码登录
    @PostMapping("/login")
    public Result login(@RequestBody RiderLoginDTO dto) {
        if (StrUtil.isBlank(dto.getAccount()) || StrUtil.isBlank(dto.getPassword())) {
            return Result.error("账号或密码不能为空");
        }

        Rider rider = riderService.getByAccount(dto.getAccount());
        if (rider == null) {
            return Result.error("骑手账号不存在");
        }

        if (!dto.getPassword().equals(rider.getPassword())) {
            return Result.error("密码错误");
        }

        RiderLoginVO vo = buildVO(rider);
        return Result.success(vo);
    }

    // 2. 模拟快捷登录
    @PostMapping("/login/simple")
    public Result simpleLogin(@RequestBody RiderSimpleLoginDTO dto) {
        String phone = dto.getPhoneNumber();
        if (StrUtil.isBlank(phone)) {
            return Result.error("手机号不能为空");
        }

        Rider rider = riderService.getByPhone(phone);
        if (rider == null) {
            rider = new Rider();
            rider.setAccount(phone);
            rider.setPhone(phone);
            rider.setName("测试骑手");
            rider.setStatus(0);
            rider.setPassword("123456");
            riderService.insert(rider);
        }

        RiderLoginVO vo = buildVO(rider);
        return Result.success(vo);
    }

    // 构建返回VO
    private RiderLoginVO buildVO(Rider rider) {
        RiderLoginVO vo = new RiderLoginVO();
        vo.setRiderId(rider.getRiderId());
        vo.setAccount(rider.getAccount());
        vo.setName(rider.getName());
        vo.setPhone(rider.getPhone());
        vo.setStudentId(rider.getStudentId());
        vo.setStatus(rider.getStatus());
        vo.setToken(jwtUtil.generateToken(rider.getRiderId(), rider.getAccount()));
        return vo;
    }
    @GetMapping("/info")
    public Result getInfo(HttpServletRequest request) {
        Integer riderId = (Integer) request.getAttribute("adminId");
        Rider rider = riderService.selectById(riderId);
        return Result.success(rider);
    }
    @PutMapping("updates")
    public Result update(@RequestBody Rider rider, HttpServletRequest request) {
        // 从 request 里拿到当前登录的 riderId
        Integer riderId = (Integer) request.getAttribute("adminId");
        if (riderId == null) {
            return Result.error("未登录");
        }
        // 强制设置 riderId，防止越权修改别人的数据
        rider.setRiderId(riderId);
        riderService.updateById(rider);
        return Result.success();
    }
    @PostMapping("/register")
    public Result register(@RequestBody RiderRegisterDTO dto) {
        if (StrUtil.isBlank(dto.getAccount()) || StrUtil.isBlank(dto.getPassword())
                || StrUtil.isBlank(dto.getName()) || StrUtil.isBlank(dto.getPhone())) {
            return Result.error("账号、密码、姓名、手机号不能为空");
        }

        // 检查账号是否已存在
        Rider exist = riderService.getByAccount(dto.getAccount());
        if (exist != null) {
            return Result.error("该账号已被注册");
        }

        // 检查手机号是否已注册
        exist = riderService.getByPhone(dto.getPhone());
        if (exist != null) {
            return Result.error("该手机号已被注册");
        }

        Rider rider = new Rider();
        rider.setAccount(dto.getAccount());
        rider.setPassword(dto.getPassword());
        rider.setName(dto.getName());
        rider.setPhone(dto.getPhone());
        rider.setStudentId(dto.getStudentId());
        rider.setStatus(0);          // 未认证
        rider.setWorkStatus(0);      // 休息
        rider.setTodayOrderCount(0);
        rider.setDailyOrderLimit(20); // 默认上限，可配置
        rider.setCreditScore(100);
        rider.setFrozen(0);

        riderService.insert(rider);

        // 注册成功直接返回登录信息（自动登录）
        RiderLoginVO vo = buildVO(rider);
        return Result.success(vo);
    }
}