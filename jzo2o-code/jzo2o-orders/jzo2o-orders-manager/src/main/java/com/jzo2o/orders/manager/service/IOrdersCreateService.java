package com.jzo2o.orders.manager.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.api.orders.dto.response.OrderResDTO;
import com.jzo2o.api.orders.dto.response.OrderSimpleResDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.model.msg.TradeStatusMsg;
import com.jzo2o.orders.base.model.domain.Orders;
import com.jzo2o.orders.manager.model.dto.OrderCancelDTO;
import com.jzo2o.orders.manager.model.dto.request.OrderPageQueryReqDTO;
import com.jzo2o.orders.manager.model.dto.request.OrdersPayReqDTO;
import com.jzo2o.orders.manager.model.dto.request.PlaceOrderReqDTO;
import com.jzo2o.orders.manager.model.dto.response.OperationOrdersDetailResDTO;
import com.jzo2o.orders.manager.model.dto.response.OrdersPayResDTO;
import com.jzo2o.orders.manager.model.dto.response.PlaceOrderResDTO;

import java.util.List;

/**
 * <p>
 * 下单服务类
 * </p>
 *
 * @author itcast
 * @since 2023-07-10
 */
public interface IOrdersCreateService extends IService<Orders> {

    /**
     * 创建订单（下单）- 入口，内部转调带 userId 的方法以配合分布式锁
     *
     * @param placeOrderReqDTO 订单参数
     * @return 订单id等响应信息
     */
    PlaceOrderResDTO placeOrder(PlaceOrderReqDTO placeOrderReqDTO);

    /**
     * 创建订单（下单）- 带用户id，用于分布式锁 key，同一用户同一服务 30 秒内仅允许下单一次
     *
     * @param userId           登录用户id
     * @param placeOrderReqDTO 订单参数
     * @return 订单id等响应信息
     */
    PlaceOrderResDTO placeOrder(Long userId, PlaceOrderReqDTO placeOrderReqDTO);

    /**
     * 保存订单（单独事务，通过代理调用以生效）
     *
     * @param orders 订单
     */
    void saveOrders(Orders orders);
}
