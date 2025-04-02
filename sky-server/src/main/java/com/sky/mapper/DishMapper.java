package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
* @author liu
* @description 针对表【dish(菜品)】的数据库操作Mapper
* @createDate 2025-04-02 11:07:43
* @Entity com.sky.domain.Dish
*/
@Mapper
public interface DishMapper extends BaseMapper<com.sky.entity.Dish> {

}




