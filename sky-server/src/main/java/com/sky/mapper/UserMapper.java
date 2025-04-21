package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
* @author liu
* @description 针对表【setmeal(套餐)】的数据库操作Mapper
* @createDate 2025-04-02 11:07:43
* @Entity com.sky.domain.Setmeal
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user where openid = #{openid}")
    User getByOpenId(String openid);

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    Integer countByMap(Map<String, Object> map);
}




