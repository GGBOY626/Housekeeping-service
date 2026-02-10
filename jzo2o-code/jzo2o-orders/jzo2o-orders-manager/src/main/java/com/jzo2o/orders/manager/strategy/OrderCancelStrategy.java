package com.jzo2o.orders.manager.strategy;

import com.jzo2o.orders.manager.model.dto.OrderCancelDTO;

/**
 * 订单取消策略接口（按用户类型+订单状态选择不同策略）
 */
public interface OrderCancelStrategy {

    /**
     * 执行取消逻辑
     *
     * @param orderCancelDTO 取消参数
     */
    void cancel(OrderCancelDTO orderCancelDTO);
}
