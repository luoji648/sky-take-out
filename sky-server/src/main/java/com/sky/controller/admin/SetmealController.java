package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(("/admin/setmeal"))
@Slf4j
@Tag(name = "套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @GetMapping("/page")
    @Operation(summary = "套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询");
        PageResult pageResult = setmealService.pageSetmeal(setmealPageQueryDTO);

        return Result.success(pageResult);
    }

    @GetMapping("{id}")
    @Operation(summary = "根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根据id查询套餐");
        SetmealVO setmealVO = setmealService.getSetmealById(id);

        return Result.success(setmealVO);
    }

    @PostMapping
    @Operation(summary = "新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐");
        setmealService.saveSetmeal(setmealDTO);

        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "批量删除套餐")
    public Result deleteBatchByIds(@RequestParam List<Long> ids) {
        log.info("批量删除套餐");
        setmealService.deleteBatchByIds(ids);

        return Result.success();
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "菜品起售、停售")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("菜品起售、停售");
        setmealService.startOrStop(status, id);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改菜品")
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改菜品");
        setmealService.updateSetmeal(setmealDTO);

        return Result.success();
    }
}
