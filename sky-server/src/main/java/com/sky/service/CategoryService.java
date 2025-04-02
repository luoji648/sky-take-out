package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

/**
* @author liu
* @description 针对表【category(菜品及套餐分类)】的数据库操作Service
* @createDate 2025-04-02 11:03:58
*/
public interface CategoryService extends IService<Category> {

    PageResult pageQueryCategory(CategoryPageQueryDTO catePageQueryDTO);

    void startOrStop(Integer status, Long id);

    void saveCategory(CategoryDTO categoryDTO);

    void updateCategory(CategoryDTO categoryDTO);
}
