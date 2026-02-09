package com.jzo2o.customer.controller.open;

import com.jzo2o.common.constants.UserType;
import com.jzo2o.customer.model.dto.request.LoginForCustomerPasswordReqDTO;
import com.jzo2o.customer.model.dto.request.LoginForCustomerReqDTO;
import com.jzo2o.customer.model.dto.request.LoginForWorkReqDTO;
import com.jzo2o.customer.model.dto.request.RegisterForCustomerReqDTO;
import com.jzo2o.customer.model.dto.response.LoginResDTO;
import com.jzo2o.customer.service.ILoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author itcast
 */
@RestController("openLoginController")
@RequestMapping("/open/login")
@Api(tags = "白名单接口 - 客户登录相关接口")
public class LoginController {

    @Resource
    private ILoginService loginService;

    @PostMapping("/worker")
    @ApiOperation("服务人员/机构人员登录接口")
    public LoginResDTO loginForWorker(@RequestBody LoginForWorkReqDTO loginForWorkReqDTO) {

        if(UserType.INSTITUTION == loginForWorkReqDTO.getUserType()){
            return loginService.loginForPassword(loginForWorkReqDTO);
        }else{
            return loginService.loginForVerify(loginForWorkReqDTO);
        }

    }

    /**
     * c端用户登录接口（微信，保留兼容）
     */
    @PostMapping("/common/user")
    @ApiOperation("c端用户登录接口-微信")
    public LoginResDTO loginForCommonUser(@RequestBody LoginForCustomerReqDTO loginForCustomerReqDTO) {
        return loginService.loginForCommonUser(loginForCustomerReqDTO);
    }

    /**
     * c端用户账号密码登录接口（Web端）
     */
    @PostMapping("/common/user/password")
    @ApiOperation("c端用户账号密码登录")
    public LoginResDTO loginForCommonUserByPassword(@RequestBody LoginForCustomerPasswordReqDTO reqDTO) {
        return loginService.loginForCommonUserByPassword(reqDTO);
    }

    /**
     * c端用户注册接口
     */
    @PostMapping("/common/user/register")
    @ApiOperation("c端用户注册")
    public void register(@RequestBody RegisterForCustomerReqDTO reqDTO) {
        loginService.register(reqDTO);
    }

}
