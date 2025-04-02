package com.sky.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
* @author liu
* @description 针对表【category(菜品及套餐分类)】的数据库操作Mapper
* @createDate 2025-04-02 11:03:58
* @Entity com.sky.domain.Category
*/
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

}




