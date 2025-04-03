package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.annotation.AutoFill;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

/**
* @author liu
* @description 针对表【category(菜品及套餐分类)】的数据库操作Service实现
* @createDate 2025-04-02 11:03:58
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public PageResult pageQueryCategory(CategoryPageQueryDTO c)
    {
        Page<Category> page = Page.of(c.getPage(),c.getPageSize());
        page.addOrder(new OrderItem().setColumn("sort").setAsc(true));

        Page<Category> p = lambdaQuery()
                .like(c.getName() != null, Category::getName, c.getName())
                .page(page);

        PageResult pageResult = new PageResult();
        pageResult.setTotal(p.getTotal());
        pageResult.setRecords(p.getRecords());

        return pageResult;
    }

    @Override
    public void startOrStop(Integer status, Long id)
    {
        lambdaUpdate()
                .set(status != null,Category::getStatus,status)
                .eq(id != null,Category::getId,id)
                .update();

    }

    @AutoFill(value = OperationType.INSERT)
    @Override
    public void saveCategory(Category category)
    {
        category.setStatus(StatusConstant.ENABLE);
        save(category);
    }

    @Override
    public void updateCategory(Category category)
    {

        lambdaUpdate()
                .eq(category.getId() != null,Category::getId,category.getId())
                .update(category);
    }
}




