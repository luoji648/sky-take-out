package com.sky.mapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.annotation.AutoFill;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
* @author liu
* @description 针对表【setmeal(套餐)】的数据库操作Mapper
* @createDate 2025-04-02 11:07:43
* @Entity com.sky.domain.Setmeal
*/
@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {

    default void startOrStop(Integer status, Long id) {
        update(new LambdaUpdateWrapper<Setmeal>()
                .eq(id != null,Setmeal::getId,id)
                .set(Setmeal::getStatus,status));
    }

    default void StopBatchByIds(List<Long> ids) {
        update(new LambdaUpdateWrapper<Setmeal>()
                .set(Setmeal::getStatus, StatusConstant.DISABLE)
                .in(Setmeal::getId,ids));
    }

    Page<SetmealVO> pageSetmeal(@Param("page") Page<SetmealVO> page, @Param("dto") SetmealPageQueryDTO setmealPageQueryDTO);

    @AutoFill(OperationType.INSERT)
    default void saveSetmeal(Setmeal setmeal)
    {
        insert(setmeal);
    }

    @Override
    @AutoFill(OperationType.UPDATE)
    int deleteById(Setmeal entity);

    default List<Setmeal> listByIds(List<Long> ids) {
        return selectList(new LambdaUpdateWrapper<Setmeal>()
                .in(ids != null,Setmeal::getId,ids));
    }

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

    /**
     * 根据条件统计套餐数量
     *
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}




