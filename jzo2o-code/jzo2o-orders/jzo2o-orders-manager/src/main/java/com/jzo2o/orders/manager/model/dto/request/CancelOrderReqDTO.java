package com.jzo2o.orders.manager.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 取消订单请求（orderId 建议传字符串避免大数精度丢失）
 */
@Data
@ApiModel("取消订单请求")
public class CancelOrderReqDTO {

    @ApiModelProperty(value = "订单id", required = true)
    private String orderId;

    @ApiModelProperty("取消原因")
    private String cancelReason;
}
