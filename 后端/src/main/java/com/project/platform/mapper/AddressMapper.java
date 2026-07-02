package com.project.platform.mapper;

import com.project.platform.entity.Address;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AddressMapper {
    // 列表
    List<Address> list(Integer userId);

    // 新增
    void insert(Address address);

    // 根据ID查询
    Address selectById(Integer id);

    // 修改
    void updateById(Address address);

    // 删除
    void deleteById(Integer id);

    // 取消其他默认地址
    void cancelDefault(Integer userId);
}