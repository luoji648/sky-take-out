package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.mapper.SetmealMapper;
import com.sky.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author liu
 * @description 针对表【setmeal(套餐)】的数据库操作Service实现
 * @createDate 2025-04-02 11:07:43
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>
        implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional
    public void saveSetmeal(SetmealDTO setmealDTO) {
        /*
         * 必须先插入setmeal表，再插入setmeal_dish表（主键回显）
         * */
        // 新增套餐
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);
        setmealMapper.saveSetmeal(setmeal);

        // 获取套餐和菜品的关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(e -> {
            e.setSetmealId(setmeal.getId());
        });
        setmealDishMapper.insert(setmealDishes);

    }

    @Override
    public SetmealVO getSetmealById(Long id) {
        // 查套餐
        Setmeal setmeal = setmealMapper.selectById(id);
        // 查套餐分类
        Category category = categoryMapper.selectById(setmeal.getCategoryId());
        // 查套餐关联菜品关系
        List<SetmealDish> setmealDishList = setmealDishMapper.listBySetmealId(id);
        //封装成VO
        SetmealVO setmealVO = BeanUtil.copyProperties(setmeal, SetmealVO.class);
        setmealVO.setCategoryName(category.getName());
        setmealVO.setSetmealDishes(setmealDishList);

        return setmealVO;
    }

    @Override
    @Transactional
    public void deleteBatchByIds(List<Long> ids) {
        // 判断套餐能否被删除--是否在启售中
        List<Setmeal> setmealList = setmealMapper.listByIds(ids);
        if (setmealList != null && setmealList.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        // 删除套餐
        setmealMapper.deleteByIds(ids);
        // 删除套餐和菜品的对应关系
        setmealDishMapper.deleteBatchBySetmealIds(ids);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        // 如果是启售，判断套餐关联的菜品是否停售中
        if (status == StatusConstant.ENABLE) {
            List<SetmealDish> setmealDishList = setmealDishMapper.listBySetmealId(id);
            if (setmealDishList != null && setmealDishList.size() > 0) {
                for (SetmealDish setmealDish : setmealDishList) {
                    Dish dish = dishMapper.selectById(setmealDish.getDishId());
                    if (dish.getStatus() == StatusConstant.DISABLE) {
                        throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                }
            }

        }

        setmealMapper.startOrStop(status,id);
    }

    @Override
    public PageResult pageSetmeal(SetmealPageQueryDTO setmealPageQueryDTO) {
        Page<SetmealVO> page = new Page<>(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        page = setmealMapper.pageSetmeal(page, setmealPageQueryDTO);

        return new PageResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void updateSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);
        setmealMapper.updateById(setmeal);
        // 修改套餐与菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmeal.getId());
        }
        // 先删后插入
        setmealDishMapper.deleteBySetmealId(setmeal.getId());
        setmealDishMapper.insert(setmealDishes);
    }
}




