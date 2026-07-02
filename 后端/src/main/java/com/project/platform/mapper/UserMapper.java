package com.project.platform.mapper;

import com.project.platform.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {

    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Integer id);

    List<User> list(User user);

    int insert(User entity);

    int updateById(User entity);

    boolean removeByIds(List<Integer> ids);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUserName(String username);

    @Select("SELECT * FROM user WHERE tel = #{tel}")
    User selectByTel(String tel);


}