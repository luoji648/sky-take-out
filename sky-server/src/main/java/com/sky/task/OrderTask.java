package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟触发一次
    public void processTimeOrder() {
        log.info("定时处理超时订单:{}", LocalDateTime.now());
        List<Orders> ordersList = ordersMapper.
                listByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));

        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时自动取消");
                orders.setCancelTime(LocalDateTime.now());
            }
            ordersMapper.updateById(ordersList);
        }
    }

    /**
     * 处理一直处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * *  ?") // 每天凌晨1点处理
    public void processDeliverOrder() {
        log.info("定时处理派送中的订单:{}", LocalDateTime.now());
        List<Orders> ordersList = ordersMapper.
                listByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));

        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
            }
            ordersMapper.updateById(ordersList);
        }
    }
}
