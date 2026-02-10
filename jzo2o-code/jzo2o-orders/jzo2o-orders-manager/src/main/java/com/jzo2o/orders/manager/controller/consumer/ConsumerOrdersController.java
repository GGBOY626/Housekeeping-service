package com.jzo2o.orders.manager.controller.consumer;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.api.orders.dto.request.OrderCancelReqDTO;
import com.jzo2o.api.orders.dto.response.OrderResDTO;
import com.jzo2o.api.orders.dto.response.OrderSimpleResDTO;
import com.jzo2o.common.constants.ErrorInfo;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.orders.manager.model.dto.OrderCancelDTO;
import com.jzo2o.orders.manager.model.dto.request.ConfirmPayReqDTO;
import com.jzo2o.orders.manager.model.dto.request.CreatePaymentIntentReqDTO;
import com.jzo2o.orders.manager.model.dto.request.OrdersPayReqDTO;
import com.jzo2o.orders.manager.model.dto.request.PlaceOrderReqDTO;
import com.jzo2o.orders.manager.model.dto.response.CreatePaymentIntentResDTO;
import com.jzo2o.orders.manager.model.dto.response.OrdersPayResDTO;
import com.jzo2o.orders.manager.model.dto.response.PlaceOrderResDTO;
import com.jzo2o.orders.manager.service.IOrdersCreateService;
import com.jzo2o.orders.manager.service.IOrdersManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author itcast
 */
@RestController("consumerOrdersController")
@Api(tags = "用户端-订单相关接口")
@RequestMapping("/consumer/orders")
public class ConsumerOrdersController {

    @Resource
    private IOrdersManagerService ordersManagerService;

    @Resource
    private IOrdersCreateService ordersCreateService;

    @ApiOperation("下单接口")
    @PostMapping("/place")
    public PlaceOrderResDTO place(@RequestBody PlaceOrderReqDTO placeOrderReqDTO) {
        try {
            return ordersCreateService.placeOrder(UserContext.currentUserId(), placeOrderReqDTO);
        } catch (BadRequestException e) {
            if (ErrorInfo.Msg.REQUEST_OPERATE_FREQUENTLY.equals(e.getMessage())) {
                throw new BadRequestException("请勿重复下单");
            }
            throw e;
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据订单id查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单id", required = true, dataTypeClass = Long.class)
    })
    public OrderResDTO detail(@PathVariable("id") Long id) {
        return ordersManagerService.getDetail(id);
    }
    @GetMapping("/consumerQueryList")
    @ApiOperation("订单滚动分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ordersStatus", value = "订单状态，0：待支付，100：派单中，200：待服务，300：服务中，400：待评价，500：订单完成，600：订单取消，700：已关闭", required = false, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "sortBy", value = "排序字段", required = false, dataTypeClass = Long.class)
    })
    public List<OrderSimpleResDTO> consumerQueryList(@RequestParam(value = "ordersStatus", required = false) Integer ordersStatus,
                                                     @RequestParam(value = "sortBy", required = false) Long sortBy) {
        return ordersManagerService.consumerQueryList(UserContext.currentUserId(), ordersStatus, sortBy);
    }

    @PostMapping("/create-payment-intent")
    @ApiOperation("创建 Stripe 支付意图")
    public CreatePaymentIntentResDTO createPaymentIntent(@RequestBody CreatePaymentIntentReqDTO req) {
        Long orderId = parseOrderId(req.getOrderId());
        String clientSecret = ordersManagerService.createPaymentIntent(
                orderId, UserContext.currentUserId(), req.getAmount());
        return new CreatePaymentIntentResDTO(clientSecret);
    }

    @PostMapping("/confirm-paid")
    @ApiOperation("确认支付成功（Stripe 前端支付成功后调用）")
    public void confirmPaid(@RequestBody ConfirmPayReqDTO req) {
        Long orderId = parseOrderId(req.getOrderId());
        ordersManagerService.markPaySuccess(
                orderId, UserContext.currentUserId(), req.getPaymentIntentId());
    }

    private static Long parseOrderId(Object orderId) {
        if (orderId == null || "".equals(orderId.toString().trim())) {
            throw new BadRequestException("订单id不能为空");
        }
        try {
            return Long.parseLong(orderId.toString().trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("订单id格式错误");
        }
    }
}
