package com.jzo2o.orders.manager.model.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建 Stripe 支付意图响应（返回 clientSecret 给前端确认支付）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("创建支付意图响应")
public class CreatePaymentIntentResDTO {

    @ApiModelProperty("Stripe PaymentIntent client_secret")
    private String clientSecret;
}
