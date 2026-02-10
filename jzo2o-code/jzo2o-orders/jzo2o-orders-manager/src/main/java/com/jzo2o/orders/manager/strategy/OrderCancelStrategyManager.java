package com.jzo2o.orders.manager.strategy;

import com.jzo2o.orders.base.enums.OrderStatusEnum;
import com.jzo2o.orders.manager.model.dto.OrderCancelDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 订单取消策略管理器：根据 用户类型 + 订单状态 选择对应策略执行取消
 */
@Component
@Slf4j
public class OrderCancelStrategyManager {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 执行取消（由策略类处理）
     *
     * @param orderCancelDTO 取消参数（需包含 currentUserType）
     * @param orderStatus    当前订单状态
     */
    public void cancel(OrderCancelDTO orderCancelDTO, OrderStatusEnum orderStatus) {
        String key = orderCancelDTO.getCurrentUserType() + ":" + orderStatus.name();
        OrderCancelStrategy strategy;
        try {
            strategy = applicationContext.getBean(key, OrderCancelStrategy.class);
        } catch (Exception e) {
            log.warn("未找到取消策略 bean: {}", key, e);
            throw new IllegalStateException("当前不支持该取消场景: " + key);
        }
        strategy.cancel(orderCancelDTO);
    }
}
