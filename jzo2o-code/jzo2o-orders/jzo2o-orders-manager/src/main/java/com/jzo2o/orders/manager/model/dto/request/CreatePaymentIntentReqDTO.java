package com.jzo2o.orders.manager.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建 Stripe 支付意图请求
 */
@Data
@ApiModel("创建支付意图请求")
public class CreatePaymentIntentReqDTO {

    @NotNull
    @ApiModelProperty(value = "订单id（建议前端传字符串避免大数精度丢失）", required = true)
    private String orderId;

    @NotNull
    @ApiModelProperty(value = "支付金额（元）", required = true)
    private BigDecimal amount;
}
