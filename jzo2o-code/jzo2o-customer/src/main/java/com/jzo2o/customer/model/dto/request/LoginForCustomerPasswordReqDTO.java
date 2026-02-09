package com.jzo2o.customer.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * C端用户账号密码登录模型
 */
@Data
@ApiModel("C端用户账号密码登录模型")
public class LoginForCustomerPasswordReqDTO {

    @ApiModelProperty(value = "手机号", required = true)
    private String phone;

    @ApiModelProperty(value = "密码", required = true)
    private String password;
}
