package com.jzo2o.orders.manager.strategy.impl;

import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.orders.base.enums.OrderStatusEnum;
import com.jzo2o.orders.base.mapper.OrdersCanceledMapper;
import com.jzo2o.orders.base.model.domain.OrdersCanceled;
import com.jzo2o.orders.base.model.dto.OrderUpdateStatusDTO;
import com.jzo2o.orders.base.service.IOrdersCommonService;
import com.jzo2o.orders.manager.model.dto.OrderCancelDTO;
import com.jzo2o.orders.manager.strategy.OrderCancelStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 系统定时任务取消待支付状态订单的策略类
 * 策略标识：用户类型(UserType):订单状态(OrderStatusEnum) = 0:NO_PAY
 */
@Component("0:NO_PAY")
public class SystemNoPayOrderCancelStrategy implements OrderCancelStrategy {

    @Autowired
    private IOrdersCommonService ordersCommonService;

    @Autowired
    private OrdersCanceledMapper ordersCanceledMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(OrderCancelDTO orderCancelDTO) {
        OrderUpdateStatusDTO orderUpdateStatusDTO = OrderUpdateStatusDTO.builder()
                .id(orderCancelDTO.getId())
                .originStatus(OrderStatusEnum.NO_PAY.getStatus())
                .targetStatus(OrderStatusEnum.CANCELED.getStatus())
                .build();
        Integer i = ordersCommonService.updateStatus(orderUpdateStatusDTO);
        if (i == null || i <= 0) {
            throw new ForbiddenOperationException("订单取消失败");
        }

        OrdersCanceled ordersCanceled = new OrdersCanceled();
        ordersCanceled.setId(orderCancelDTO.getId());
        ordersCanceled.setCancellerId(orderCancelDTO.getCurrentUserId());
        ordersCanceled.setCancelerName(orderCancelDTO.getCurrentUserName());
        ordersCanceled.setCancellerType(orderCancelDTO.getCurrentUserType());
        ordersCanceled.setCancelReason(orderCancelDTO.getCancelReason());
        ordersCanceled.setCancelTime(LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();
        ordersCanceled.setCreateTime(now);
        ordersCanceled.setUpdateTime(now);
        ordersCanceledMapper.insert(ordersCanceled);
    }
}
