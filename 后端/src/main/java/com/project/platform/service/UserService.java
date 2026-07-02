package com.project.platform.service;

import com.github.pagehelper.PageInfo;
import com.project.platform.entity.User;

import java.util.List;

public interface UserService {
    PageInfo page(User user, Integer pageNum, Integer pageSize);
    User selectById(Integer id);
    List<User> list(User user);
    void insert(User entity);
    void updateById(User entity);
    void removeByIds(List<Integer> ids);
    User getByUsername(String username);
    User getByTel(String tel);
}