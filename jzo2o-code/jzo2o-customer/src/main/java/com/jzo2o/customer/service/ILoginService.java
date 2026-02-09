package com.jzo2o.customer.service;

import com.jzo2o.customer.model.dto.request.LoginForCustomerPasswordReqDTO;
import com.jzo2o.customer.model.dto.request.LoginForCustomerReqDTO;
import com.jzo2o.customer.model.dto.request.LoginForWorkReqDTO;
import com.jzo2o.customer.model.dto.request.RegisterForCustomerReqDTO;
import com.jzo2o.customer.model.dto.response.LoginResDTO;

/**
 * 客户中心登录业务
 */
public interface ILoginService {


    /**
     * 服务人员验证码登录
     *
     * @param loginForWorkReqDTO 登录参数
     * @return
     */
    LoginResDTO loginForPassword(LoginForWorkReqDTO loginForWorkReqDTO);
    /**
     * 机构人员账号密码登录
     *
     * @param loginForWorkReqDTO 登录参数
     * @return
     */
    LoginResDTO loginForVerify(LoginForWorkReqDTO loginForWorkReqDTO);

    /**
     * 客户人员/机构登录接口
     *
     * @param loginForCustomerReqDTO 登录请求
     * @return token
     */
    LoginResDTO loginForCommonUser(LoginForCustomerReqDTO loginForCustomerReqDTO);

    /**
     * C端用户账号密码登录
     */
    LoginResDTO loginForCommonUserByPassword(LoginForCustomerPasswordReqDTO reqDTO);

    /**
     * C端用户注册
     */
    void register(RegisterForCustomerReqDTO reqDTO);
}
