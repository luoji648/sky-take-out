package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author liu
 * @description 针对表【dish(菜品)】的数据库操作Service实现
 * @createDate 2025-04-02 11:07:43
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>
        implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    @AutoFill(value = OperationType.INSERT)
    @Transactional
    public void saveDishWithFlavor(Dish dish, List<DishFlavor> flavors) {
        //向菜品表中插入1条数据
        dishMapper.insert(dish);

        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(e -> {
                e.setDishId(dish.getId());
            });
        }
        //向口味表中插入n条数据
        dishFlavorMapper.insert(flavors);
    }

    @Override
    public PageResult pageDish(DishPageQueryDTO dishPageQueryDTO) {
        // 创建MyBatis-Plus分页对象
        Page<DishVO> page = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        // 执行分页查询
        page = dishMapper.pageMy(page, dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void deleteBatchByIds(List<Long> ids) {
        List<Dish> dishes = listByIds(ids);
        for (Dish dish : dishes) {
            // 判断当前菜品是否能删除--是否在起售中
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            // 判断当前菜品是否能删除--是否被套餐关联
            List<Long> setmealIds = setmealDishMapper.listSetmealIdsByDishId(dish.getId());
            if (setmealIds != null && setmealIds.size() > 0) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
            // 删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(dish.getId());
        }
        //删除菜品表中的菜品数据
        dishMapper.deleteByIds(ids);
    }

    @Override
    public DishVO getByIdMy(Long id) {
        Dish dish = dishMapper.selectById(id);
        //查菜品关联的口味
        List<DishFlavor> dishFlavors = dishFlavorMapper.getById(id);
        //封装成VO
        DishVO dishVO = BeanUtil.copyProperties(dish, DishVO.class);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    @Override
    public void updateMy(DishDTO dishDTO) {
        // 更新菜品表
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        dishMapper.updateMy(dish);
        // 更新关联的口味
        // 删除再插入
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            for (DishFlavor flavor : flavors) {
                flavors.forEach(e -> {
                    e.setDishId(dishDTO.getId());
                });
            }
        }
        dishFlavorMapper.insert(flavors);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        // 如果是停售操作，还要将菜品关联的套餐删了
        if (status == StatusConstant.DISABLE) {
            List<Long> setmealIds = setmealDishMapper.listSetmealIdsByDishId(id);
            if (setmealIds != null && setmealIds.size() > 0) {
                setmealMapper.startOrStop(setmealIds);
            }
        }
        // 菜品状态更改
        dishMapper.startOrStop(status, id);
    }

    @Override
    public List<DishVO> listByCategoryId(Long categoryId) {
        return dishMapper.listByCategoryId(categoryId);
    }
}




