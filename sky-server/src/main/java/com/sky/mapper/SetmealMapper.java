package com.sky.mapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author liu
* @description 针对表【setmeal(套餐)】的数据库操作Mapper
* @createDate 2025-04-02 11:07:43
* @Entity com.sky.domain.Setmeal
*/
@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {

    default void startOrStop(List<Long> ids) {
        update(new UpdateWrapper<Setmeal>()
                .set("status", StatusConstant.DISABLE)
                .in("id",ids));
    }
}




