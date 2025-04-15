package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @AutoFill(value = OperationType.INSERT)
    @Transactional
    public void saveDishWithFlavor(Dish dish, List<DishFlavor> flavors) {
        // 清除redis中的缓存数据
        String key = "dish_" + dish.getCategoryId();
        clearCache(key);

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
        // 清除redis中的缓存数据
        clearCache("dish_*");

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
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //封装成VO
        DishVO dishVO = BeanUtil.copyProperties(dish, DishVO.class);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    @Override
    public void updateMy(DishDTO dishDTO) {
        // 清除redis中的缓存数据
        clearCache("dish_*");

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
        // 清除redis中的缓存数据
        clearCache("dish_*");

        // 如果是停售操作，还要将菜品关联的套餐删了
        if (status == StatusConstant.DISABLE) {
            List<Long> setmealIds = setmealDishMapper.listSetmealIdsByDishId(id);
            if (setmealIds != null && setmealIds.size() > 0) {
                setmealMapper.StopBatchByIds(setmealIds);
            }
        }

        // 菜品状态更改
        dishMapper.startOrStop(status, id);
    }

    @Override
    public List<DishVO> listByCategoryId(Long categoryId) {
        return dishMapper.listByCategoryId(categoryId);
    }

    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        // 查询redis中是否有数据
        String key = "dish_" + dish.getCategoryId();
        String jsonDishVOList = redisTemplate.opsForValue().get(key);
        // 手动反序列化
        List<DishVO> dishVOS = JSON.parseArray(jsonDishVOList, DishVO.class);

        // 如果存在，直接返回，无需查询数据库
        if (dishVOS != null && dishVOS.size() > 0) {
            return dishVOS;
        }

        // 如果不存在，查询数据库，将查询到的数据放入redis中
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            // 根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        // 手动序列化，存入redis
        String json = JSON.toJSONString(dishVOList);
        redisTemplate.opsForValue().set(key, json);

        return dishVOList;
    }

    /**
     * 清除redis中的缓存数据
     * @param pattern
     */
    private void clearCache(String pattern) {
        Set<String> keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
    }
}




