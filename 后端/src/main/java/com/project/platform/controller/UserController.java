package com.project.platform.controller;

import com.github.pagehelper.PageInfo;
import com.project.platform.config.Result;
import com.project.platform.dto.AvatarDTO;
import com.project.platform.entity.User;
import com.project.platform.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 普通用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 分页查询
     */
    @GetMapping("page")
    public Result page(User user, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<User> pageInfo = userService.page(user, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 根据id查询
     */
    @GetMapping("selectById/{id}")
    public Result selectById(@PathVariable("id") Integer id) {
        User entity = userService.selectById(id);
        return Result.success(entity);
    }

    /**
     * 列表
     */
    @GetMapping("list")
    public Result list(User user) {
        return Result.success(userService.list(user));
    }

    /**
     * 新增
     */
    @PostMapping("add")
    public Result add(@RequestBody User entity) {
        try {
            userService.insert(entity);
            return Result.success();
        } catch (Exception e) {
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 更新
     */
    @PutMapping("update")
    public Result update(@RequestBody User entity) {
        userService.updateById(entity);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("delBatch")
    public Result delBatch(@RequestBody List<Integer> ids) {
        userService.removeByIds(ids);
        return Result.success();
    }


    // 新增到 UserController
    @GetMapping("/info")
    public Result getInfo(HttpServletRequest request) {
        // 从 Token 解析出当前用户ID
        Integer Id = (Integer) request.getAttribute("adminId");
        User user = userService.selectById(Id);
        return Result.success(user);
    }

    @PutMapping("AppUpdate")
    public Result update(@RequestBody User entity, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("adminId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        entity.setId(userId);
        userService.updateById(entity);
        return Result.success(entity);
    }
}