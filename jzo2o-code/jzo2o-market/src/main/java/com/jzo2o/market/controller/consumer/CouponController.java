package com.jzo2o.market.controller.consumer;

import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.mvc.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("consumerCouponController")
@RequestMapping("/consumer/coupon")
@Api(tags = "用户端-优惠券相关接口")
public class CouponController {

    @Autowired
    private ICouponService couponService;

    @ApiOperation("我的优惠券列表（查 DB 用户已领取的优惠券，不走 Redis）")
    @GetMapping("/my")
    public List<CouponInfoResDTO> myList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long lastId) {
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new ForbiddenOperationException("请先登录");
        }
        return couponService.findMyCoupons(userId, status, lastId);
    }
}
