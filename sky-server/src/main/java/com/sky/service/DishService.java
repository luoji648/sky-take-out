package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.entity.DishFlavor;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

/**
* @author liu
* @description 针对表【dish(菜品)】的数据库操作Service
* @createDate 2025-04-02 11:07:43
*/
public interface DishService extends IService<Dish> {

    void saveDishWithFlavor(Dish dish, List<DishFlavor> flavors);

    PageResult pageDish(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatchByIds(List<Long> ids);

    DishVO getByIdMy(Long id);

    void updateMy(DishDTO dishDTO);

    void startOrStop(Integer status,Long id);

    List<DishVO> listByCategoryId(Long categoryId);
}
