package com.jzo2o.orders.manager.model.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author itcast
 */
@ApiModel("下单响应信息")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResDTO {
    @ApiModelProperty("订单id")
    private Long id;

    @ApiModelProperty("实付金额（订单总价-优惠券抵扣），支付页应以此金额发起支付")
    private BigDecimal realPayAmount;
}
