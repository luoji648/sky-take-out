package com.sky.controller.admin;


import cn.hutool.core.bean.BeanUtil;
import com.sky.context.BaseContext;
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
@Tag(name = "分类相关接口")
public class CategoryController
{
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @GetMapping("/page")
    @Operation(summary = "分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO catePageQueryDTO)
    {
        log.info("套餐分页查询");
        PageResult pageResult = categoryService.pageQueryCategory(catePageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    @Operation(summary = "新增分类")
    public Result save(@RequestBody CategoryDTO categoryDTO)
    {
        log.info("新增分类");
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        categoryService.saveCategory(category);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "启用禁用分类")
    public Result startOrStop(@PathVariable Integer status,Long id)
    {
        log.info("启动、禁用分类：{}，{}",status,id);
        categoryService.startOrStop(status,id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "根据类型查询分类")
    public Result<List<Category>> list(Integer type)
    {
        log.info("查询类型为{}的分类",type);
        List<Category> list = categoryService.getByType(type);
        return Result.success(list);
    }

    @PutMapping
    @Operation(summary = "修改分类")
    public Result update(@RequestBody CategoryDTO categoryDTO)
    {
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        categoryService.updateCategory(category);
        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "删除分类")
    public Result deleteById(Long id)
    {
        categoryService.removeById(id);
        return Result.success();
    }
}
