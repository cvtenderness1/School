package com.project.platform.service;

import com.github.pagehelper.PageInfo;
import com.project.platform.entity.Admin;

import java.util.List;

/**
 * 用户信息表 服务类
 */
public interface AdminService  {

    PageInfo<Admin> page(Admin admin,Integer pageNum, Integer pageSize);

    Admin selectById(Integer id);

    List<Admin> list(Admin admin);

    void insert(Admin entity);

    void updateById(Admin entity);

    void removeByIds(List<Integer> id);

    Admin login(Admin admin);

    void register(Admin admin);
}
