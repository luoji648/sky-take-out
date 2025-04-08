package com.sky.controller.admin;

import com.alibaba.fastjson.JSON;
import com.sky.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
@Slf4j
@Tag(name = "店铺操作接口")
public class ShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PutMapping("/{status}")
    @Operation(summary = "设置营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺的营业状态为：{}",status == 1 ? "营业中" : "打烊中");
        // 手动序列化
        String json = JSON.toJSONString(status);
        stringRedisTemplate.opsForValue().set(KEY,json);

        return Result.success();
    }

    @GetMapping("/status")
    @Operation(summary = "获取营业状态")
    public Result<Integer> getStatus(){
        String json = stringRedisTemplate.opsForValue().get(KEY);
        // 手动反序列化
        Integer status = JSON.parseObject(json, Integer.class);
        log.info("设置店铺的营业状态为：{}",status == 1 ? "营业中" : "打烊中");

        return Result.success(status);
    }
}
