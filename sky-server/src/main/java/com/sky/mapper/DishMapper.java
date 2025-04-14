package com.sky.mapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DishMapper extends BaseMapper<Dish>
{

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    Page<DishVO> pageMy(@Param("page") Page<DishVO> page, @Param("dto") DishPageQueryDTO dishPageQueryDTO);

    @AutoFill(OperationType.UPDATE)
    default void updateMy(Dish dish)
    {
        update(dish,new LambdaUpdateWrapper<Dish>()
                .eq(dish.getId() != null,Dish::getId,dish.getId()));
    }

    @Update("update dish set status = #{status} where id = #{id}")
    void startOrStop(Integer status, Long id);

    List<DishVO> listByCategoryId(Long categoryId);

    List<Dish> list(Dish dish);
}
