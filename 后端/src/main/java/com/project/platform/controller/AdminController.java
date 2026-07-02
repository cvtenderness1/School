package com.project.platform.controller;

import com.github.pagehelper.PageInfo;
import com.project.platform.config.Result;
import com.project.platform.entity.Admin;
import com.project.platform.mapper.AdminMapper;
import com.project.platform.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Resource
    private AdminService adminService;
    @Resource
    private AdminMapper adminMapper;

    /**
     * 分页查询
     *
     */
    @GetMapping("page")
    public Result page(Admin admin, @RequestParam(defaultValue = "1")Integer pageNum, @RequestParam(defaultValue = "10")Integer pageSize) {
        PageInfo<Admin> pageInfo = adminService.page(admin,pageNum, pageSize);
        return Result.success(pageInfo);
    }
    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody Map<String, Object> params) {
        Integer adminId = (Integer) params.get("adminId");
        String oldPassword = (String) params.get("oldPassword");
        String newPassword = (String) params.get("newPassword");

        if (adminId == null || oldPassword == null || newPassword == null) {
            return Result.error("参数不能为空");
        }

        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            return Result.error("管理员不存在");
        }

        // 原密码错误
        if (!admin.getPassword().equals(oldPassword)) {
            return Result.error("原密码错误");
        }

        // 新密码太短
        if (newPassword.length() < 6) {
            return Result.error("密码长度不能小于6位");
        }

        admin.setPassword(newPassword);
        adminMapper.updateById(admin);

        return Result.success("密码修改成功");
    }
    /**
     * 根据id查询
     */
    @GetMapping("selectById/{id}")
    public Result selectById(@PathVariable("id") Integer id) {
        Admin entity = adminService.selectById(id);
        return Result.success(entity);
    }


    /**
     * 列表
     */
    @GetMapping("list")
    public Result list(Admin admin) {
        return Result.success(adminService.list(admin));
    }


    /**
     * 新增
     *
     */
    @PostMapping("add")
    public Result add(@RequestBody Admin entity) {
        try {
        adminService.insert(entity);
        return Result.success();}
        catch (Exception e) {
            return Result.error("新增失败：" + e.getMessage());

        }
    }

    /**
     * 更新
     *
     */
    @PutMapping("update")
    public Result update(@RequestBody Admin entity) {
        adminService.updateById(entity);
        return Result.success();
    }

    /**
     * 批量删除
     *
     */
    @DeleteMapping("delBatch")
    public Result delBatch(@RequestBody List<Integer> ids) {
        adminService.removeByIds(ids);
        return Result.success();
    }
}