package com.sky.mapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.annotation.AutoFill;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
}




