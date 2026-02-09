package com.jzo2o.customer.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * C端用户注册模型
 */
@Data
@ApiModel("C端用户注册模型")
public class RegisterForCustomerReqDTO {

    @ApiModelProperty(value = "手机号", required = true)
    private String phone;

    @ApiModelProperty(value = "密码", required = true)
    private String password;

    @ApiModelProperty("昵称")
    private String nickname;
}
