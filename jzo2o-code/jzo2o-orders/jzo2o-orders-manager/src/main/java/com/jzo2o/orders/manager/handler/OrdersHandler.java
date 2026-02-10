package com.jzo2o.orders.manager.handler;

import cn.hutool.core.collection.CollUtil;
import com.jzo2o.api.trade.RefundRecordApi;
import com.jzo2o.api.trade.dto.response.ExecutionResultResDTO;
import com.jzo2o.common.constants.UserType;
import com.jzo2o.orders.base.enums.OrderPayStatusEnum;
import com.jzo2o.orders.base.enums.OrderRefundStatusEnum;
import com.jzo2o.orders.base.enums.OrderStatusEnum;
import com.jzo2o.orders.base.model.domain.Orders;
import com.jzo2o.orders.base.model.domain.OrdersRefund;
import com.jzo2o.orders.manager.model.dto.OrderCancelDTO;
import com.jzo2o.orders.manager.service.IOrdersManagerService;
import com.jzo2o.orders.manager.service.IOrdersRefundService;
import com.jzo2o.orders.manager.strategy.OrderCancelStrategyManager;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单处理器：定时处理待退款记录，调用支付服务完成退款
 */
@Component
@Slf4j
public class OrdersHandler {

    @Autowired
    private IOrdersRefundService ordersRefundService;

    @Autowired
    private RefundRecordApi refundRecordApi;

    @Autowired
    private IOrdersManagerService ordersManagerService;

    @Autowired
    private OrdersHandler owner;

    @Autowired
    private OrderCancelStrategyManager orderCancelStrategyManager;

    /**
     * 取消超时未支付订单：查询创建超过 15 分钟仍待支付的订单，按策略取消（更新状态 + 保存取消记录）
     */
    @XxlJob("cancelOverTimePayOrder")
    public void cancelOverTimePayOrder() {
        List<Orders> list = ordersManagerService.lambdaQuery()
                .eq(Orders::getOrdersStatus, OrderStatusEnum.NO_PAY.getStatus())
                .eq(Orders::getPayStatus, OrderPayStatusEnum.NO_PAY.getStatus())
                .lt(Orders::getCreateTime, LocalDateTime.now().minusMinutes(15))
                .last("limit 100")
                .list();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (Orders orders : list) {
            try {
                OrderCancelDTO orderCancelDTO = new OrderCancelDTO();
                orderCancelDTO.setId(orders.getId());
                orderCancelDTO.setCurrentUserId(0L);
                orderCancelDTO.setCurrentUserName("系统定时任务");
                orderCancelDTO.setCurrentUserType(UserType.SYSTEM);
                orderCancelDTO.setCancelReason("超时未支付");
                orderCancelStrategyManager.cancel(orderCancelDTO, OrderStatusEnum.NO_PAY);
            } catch (Exception e) {
                log.error("取消超时订单失败, orderId={}", orders.getId(), e);
            }
        }
    }

    /**
     * 定时读取退款表中的数据，调用支付服务的退款接口（Stripe 退款由支付服务根据 tradingOrderNo 映射处理）
     */
    @XxlJob("handleRefundOrders")
    public void handleRefundOrders() {
        List<OrdersRefund> list = ordersRefundService.queryRefundOrderListByCount(100);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (OrdersRefund ordersRefund : list) {
            try {
                ExecutionResultResDTO result = refundRecordApi.refundTrading(
                        ordersRefund.getTradingOrderNo(),
                        ordersRefund.getRealPayAmount());
                if (result == null) {
                    continue;
                }
                if (Integer.valueOf(OrderRefundStatusEnum.REFUNDING.getStatus()).equals(result.getRefundStatus())) {
                    continue;
                }
                owner.afterRefund(ordersRefund, result);
            } catch (Exception e) {
                log.error("退款失败, orderId={}", ordersRefund.getId(), e);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void afterRefund(OrdersRefund ordersRefund, ExecutionResultResDTO result) {
        Orders orders = new Orders();
        orders.setId(ordersRefund.getId());
        orders.setRefundNo(result.getRefundNo());
        orders.setRefundId(result.getRefundId());
        orders.setRefundStatus(result.getRefundStatus());
        boolean ok = ordersManagerService.updateById(orders);
        if (ok) {
            ordersRefundService.removeById(ordersRefund.getId());
        }
    }
}
