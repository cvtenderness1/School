package com.project.platform.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.project.platform.entity.User;
import com.project.platform.exception.CustomException;
import com.project.platform.mapper.UserMapper;
import com.project.platform.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Value("${resetPassword:123456}")
    private String resetPassword;

    @Override
    public PageInfo page(User user, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> page = userMapper.list(user);
        return PageInfo.of(page);
    }

    @Override
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<User> list(User user) {
        return userMapper.list(user);
    }

    @Override
    public void insert(User entity) {
        check(entity);
        if (entity.getPassword() == null) {
            entity.setPassword(resetPassword);
        }
        if (entity.getStatus() == null) {
            entity.setStatus("启用");
        }
        if (entity.getNickname() == null) {
            entity.setNickname("普通用户");
        }
        if (entity.getAvatarUrl() == null) {
            entity.setAvatarUrl("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        }
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        userMapper.insert(entity);
    }

    @Override
    public void updateById(User entity) {
        check(entity);
        userMapper.updateById(entity);
    }

    private void check(User entity) {
        User user = userMapper.selectByUserName(entity.getUsername());
        if (user != null && !user.getId().equals(entity.getId())) {
            throw new CustomException("用户名已存在");
        }
    }

    @Override
    public void removeByIds(List<Integer> ids) {
        userMapper.removeByIds(ids);
    }
    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUserName(username);
    }

    @Override
    public User getByTel(String tel) {
        return userMapper.selectByTel(tel);
    }
}