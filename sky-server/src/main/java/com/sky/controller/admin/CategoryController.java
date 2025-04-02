package com.sky.controller.admin;


import cn.hutool.core.bean.BeanUtil;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(("/admin/category"))
@Slf4j
@Tag(name = "套餐相关接口")
public class CategoryController
{
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @GetMapping("/page")
    @Operation(summary = "套餐分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO catePageQueryDTO)
    {
        log.info("套餐分页查询");
        PageResult pageResult = categoryService.pageQueryCategory(catePageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    @Operation(summary = "新增套餐")
    public Result save(@RequestBody CategoryDTO categoryDTO)
    {
        log.info("新增套餐");
        categoryService.saveCategory(categoryDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "套餐起售停售")
    public Result startOrStop(@PathVariable Integer status,Long id)
    {
        log.info("启动、禁用员工账号：{}，{}",status,id);
        categoryService.startOrStop(status,id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询套餐")
    public Result<Category> getById(@PathVariable Long id)
    {
        log.info("查询id为{}的套餐",id);
        Category category = categoryService.getById(id);
        return Result.success(category);
    }

    @PutMapping
    @Operation(summary = "修改套餐")
    public Result update(@RequestBody CategoryDTO categoryDTO)
    {
        categoryService.updateCategory(categoryDTO);
        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "批量删除套餐")
    public Result deleteById(Integer id)
    {
        categoryService.removeById(id);
        return Result.success();
    }
}
