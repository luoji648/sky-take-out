package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail>
{
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> listByOrderId(Long orderId);
}
