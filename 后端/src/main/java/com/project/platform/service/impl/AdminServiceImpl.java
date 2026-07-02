package com.project.platform.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.project.platform.entity.Admin;
import com.project.platform.exception.CustomException;
import com.project.platform.mapper.AdminMapper;
import com.project.platform.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户信息表 服务实现类
 */
@Service
public class AdminServiceImpl implements AdminService {
    @Resource
    private AdminMapper adminMapper;

    @Value("${resetPassword}")
    private String resetPassword;

    @Override
    public PageInfo page(Admin admin, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> page = adminMapper.list(admin);
        return PageInfo.of(page);
    }

    @Override
    public Admin selectById(Integer id) {
        Admin admin = adminMapper.selectById(id);
        return admin;
    }

    @Override
    public List<Admin> list(Admin admin) {
        return adminMapper.list(admin);
    }

    @Override
    public void insert(Admin entity) {
        check(entity);
        if (entity.getPassword() == null) {
            entity.setPassword(resetPassword);
        }
        if (entity.getRoles() == null) {
            entity.setRoles("管理员");
        }
        if (entity.getStatus() == null) {
            entity.setStatus("启用");
        }
        if (entity.getNickname() == null) {
            entity.setNickname("admin");
        }
        if(entity.getAvatarUrl() == null){
            entity.setAvatarUrl("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        }
        adminMapper.insert(entity);
    }

    @Override
    public void updateById(Admin entity) {
        check(entity);
        adminMapper.updateById(entity);
    }

    private void check(Admin entity) {
        Admin admin = adminMapper.selectByUserName(entity.getUsername());
        if (admin != null && admin.getId() != entity.getId()) {
            throw new CustomException("用户名已存在");
        }
    }

    @Override
    public void removeByIds(List<Integer> ids) {
        adminMapper.removeByIds(ids);
    }

    @Override
    public Admin login(Admin admin) {
        String username = admin.getUsername();
        Admin dbadmin= adminMapper.selectByUserName(username);
        if (dbadmin == null) {//未查到
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "账号不存在");
        }
        String password = admin.getPassword();
        if (!password.equals(dbadmin.getPassword())|| "禁用".equals(dbadmin.getStatus())) {
            //密码不对
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "密码错误或者已被禁用");
        }
        return dbadmin;
    }

    @Override
    public void register(Admin admin) {
       this.insert(admin);
    }


//
//    @Override
//    public void updateCurrentUserInfo(CurrentUserDTO currentUserDTO) {
//        Admin admin = adminMapper.selectById(currentUserDTO.getId());
//        admin.setId(currentUserDTO.getId());
//        admin.setNickname(currentUserDTO.getNickname());
//        admin.setAvatarUrl(currentUserDTO.getAvatarUrl());
//        admin.setTel(currentUserDTO.getTel());
//        admin.setEmail(currentUserDTO.getEmail());
//        adminMapper.updateById(admin);
//    }


}
