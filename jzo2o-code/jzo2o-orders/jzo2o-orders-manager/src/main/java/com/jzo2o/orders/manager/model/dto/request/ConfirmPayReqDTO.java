package com.jzo2o.orders.manager.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 确认支付成功请求（Stripe 前端 confirm 成功后调用）
 */
@Data
@ApiModel("确认支付成功请求")
public class ConfirmPayReqDTO {

    @NotNull
    @ApiModelProperty(value = "订单id（建议前端传字符串避免大数精度丢失）", required = true)
    private String orderId;

    @ApiModelProperty("Stripe PaymentIntent 或 payment_intent id（第三方交易号）")
    private String paymentIntentId;
}
