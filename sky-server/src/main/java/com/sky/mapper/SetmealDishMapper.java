package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.annotation.AutoFill;
import com.sky.entity.Category;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish>
{
    List<Long> listSetmealIdsByDishId(Long DishId);

    @Override
    @AutoFill(OperationType.INSERT)
    int insert(SetmealDish entity);

    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> listBySetmealId(Long setmealId);

    void deleteBatchBySetmealIds(List<Long> ids);

    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);
}
