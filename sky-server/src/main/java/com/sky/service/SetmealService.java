package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

/**
* @author liu
* @description 针对表【setmeal(套餐)】的数据库操作Service
* @createDate 2025-04-02 11:07:43
*/
public interface SetmealService extends IService<Setmeal> {

    PageResult pageSetmeal(SetmealPageQueryDTO setmealPageQueryDTO);

    void saveSetmeal(SetmealDTO setmealDTO);

    SetmealVO getSetmealById(Long id);

    void deleteBatchByIds(List<Long> ids);

    void startOrStop(Integer status, Long id);

    void updateSetmeal(SetmealDTO setmealDTO);

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
