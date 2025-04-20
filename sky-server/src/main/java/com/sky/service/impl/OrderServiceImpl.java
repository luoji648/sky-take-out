package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 处理各种业务异常（地址簿为空、购物车为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 获取当前用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .id(userId)
                .build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 插入order表
        Orders orders = BeanUtil.copyProperties(ordersSubmitDTO, Orders.class);
        orders.setUserId(userId);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(IdUtil.getSnowflakeNextIdStr());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());

        ordersMapper.insert(orders);

        // 向order_detail表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = BeanUtil.copyProperties(cart, OrderDetail.class);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insert(orderDetailList);

        // 清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        // 得到VO
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//      // 调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        // 生成空JSON，跳过微信支付
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = ordersMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.updateMy(orders);

        // 通过websocket向客户端推送消息 type orderId content
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    @Override
    public PageResult page(int pageNO, int pageSize, Integer status) {
        // 构建分页条件
        Page<Orders> page = new Page<>(pageNO, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        // 分页查询
        page = ordersMapper.page(page, ordersPageQueryDTO);

        // 转VO
        List<OrderVO> orderVOList = new ArrayList<>();

        if (page != null && page.getTotal() > 0) {
            List<Orders> ordersList = page.getRecords();
            for (Orders orders : ordersList) {
                OrderVO orderVO = BeanUtil.copyProperties(orders, OrderVO.class);
                orderVO.setOrderDetailList(orderDetailMapper.listByOrderId(orders.getId()));
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), orderVOList);
    }

    @Override
    public OrderVO details(Long orderId) {
        // 查询order
        Orders orders = ordersMapper.selectById(orderId);

        // 查询order_detail
        List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(orderId);
        OrderVO orderVO = BeanUtil.copyProperties(orders, OrderVO.class);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    @Override
    public void cancel(Long orderId) throws Exception {
        Orders ordersDB = ordersMapper.selectById(orderId);

        // 判断订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 判断订单状态
        Integer status = ordersDB.getStatus();
        if (status > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 用记录了取消信息的新order替换旧order
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        if (status.equals(Orders.TO_BE_CONFIRMED)) {
            // 如果在待接单状态下，调用wx支付API，给用户退款
           /* weChatPayUtil.refund(
                    ordersDB.getNumber(), //商户订单号
                    ordersDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额*/

            // 将支付状态改为已退款
            orders.setPayStatus(Orders.REFUND);
        }
        // 记录取消信息
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason("用户取消");

        ordersMapper.updateMy(orders);
    }

    @Override
    public void repetition(Long orderId) {
        // 查询order_detail
        List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(orderId);

        // 向购物车中加入原先的数据
        List<ShoppingCart> shoppingCartList = new ArrayList<>();

        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = BeanUtil.copyProperties(orderDetail, ShoppingCart.class, "id");
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }

        shoppingCartMapper.insert(shoppingCartList);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 构建查询条件
        Page<Orders> page = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        // 分页查询
        page = ordersMapper.page(page, ordersPageQueryDTO);

        // 转VO
        List<OrderVO> orderVOList = new ArrayList<>();
        if (page != null && page.getSize() > 0) {
            List<Orders> ordersList = page.getRecords();
            for (Orders orders : ordersList) {
                OrderVO orderVO = BeanUtil.copyProperties(orders, OrderVO.class);
                orderVO.setOrderDetailList(orderDetailMapper.listByOrderId(orders.getId()));
                orderVOList.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), orderVOList);
    }

    @Override
    public OrderStatisticsVO statistics() {
        // 根据状态，分别查询出待接单、待派送、派送中的订单数量
        Integer toBeConfirmed = ordersMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = ordersMapper.countByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = ordersMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);

        // 将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        ordersMapper.updateMy(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        // 查询order
        Orders ordersDB = ordersMapper.selectById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        // 如果用户已支付，需要退款
        if (ordersDB.getPayStatus().equals(Orders.PAID)) {
            // 调用微信支付API
            /*String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));*/
            orders.setPayStatus(Orders.REFUND);
        }

        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间

        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        ordersMapper.updateById(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        // 查询order
        Orders ordersDB = ordersMapper.selectById(ordersCancelDTO.getId());

        Orders orders = new Orders();
        // 如果用户已支付，需要退款
        if (ordersDB.getPayStatus().equals(Orders.PAID)) {
            // 调用微信支付API
            /*String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));*/
            orders.setPayStatus(Orders.REFUND);
        }

        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间

        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());

        ordersMapper.updateById(orders);
    }

    @Override
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = ordersMapper.selectById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        ordersMapper.updateById(orders);
    }

    @Override
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = ordersMapper.selectById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        ordersMapper.updateById(orders);
    }

    @Override
    public void reminder(Long id) {
        // 根据id查询订单
        Orders ordersDB = ordersMapper.selectById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 通过websocket向客户端推送消息 type orderId content
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号：" + ordersDB.getNumber());

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }
}
