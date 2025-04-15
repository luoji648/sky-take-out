package com.sky.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Tag(name = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @GetMapping("/page")
    @Operation(summary = "菜品分页查询")
    private Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询");
        PageResult pageResult = dishService.pageDish(dishPageQueryDTO);

        return Result.success(pageResult);
    }

    @PostMapping
    @Operation(summary = "新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品");
        List<DishFlavor> flavors = dishDTO.getFlavors();
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        dishService.saveDishWithFlavor(dish, flavors);

        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "批量删除菜品")
    public Result deleteBatchByIds(@RequestParam List<Long> ids) {
        log.info("批量删除id分别为{}的菜品们", ids);
        dishService.deleteBatchByIds(ids);

        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("查询id为{}的菜品", id);
        DishVO dishVO = dishService.getByIdMy(id);

        return Result.success(dishVO);
    }

    @PutMapping
    @Operation(summary = "修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品");
        dishService.updateMy(dishDTO);

        return Result.success();
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "菜品起售、停售")
    public Result startOrStop(@PathVariable Integer status,Long id) {
        log.info("菜品起售、停售");
        dishService.startOrStop(status,id);

        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "根据分类id查询菜品")
    public Result<List<DishVO>> listByCategoryId(Long categoryId) {
        log.info("根据分类id查询菜品");
        List<DishVO> dishVOS = dishService.listByCategoryId(categoryId);

        return Result.success(dishVOS);
    }

}
