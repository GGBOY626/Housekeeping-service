package com.jzo2o.market.service;

import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.api.market.dto.request.CouponUseReqDTO;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.api.market.dto.response.CouponUseResDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.market.model.domain.Coupon;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.market.model.dto.request.CouponOperationPageQueryReqDTO;
import com.jzo2o.market.model.dto.request.SeizeCouponReqDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
public interface ICouponService extends IService<Coupon> {

    /**
     * 根据活动id查询优惠券领取记录（运营端分页）
     *
     * @param dto 查询条件（含 activityId）
     * @return 优惠券领取记录分页
     */
    PageResult<CouponInfoResDTO> findByPage(CouponOperationPageQueryReqDTO dto);

    /**
     * 定时任务：已领取优惠券自动过期
     */
    void processExpireCoupon();

    /**
     * 用户端-我的优惠券列表（游标分页）
     *
     * @param userId  用户id
     * @param status  状态 1未使用 2已使用 3已过期，null 表示全部
     * @param lastId  上一页最后一条id，首次传 null
     * @return 列表，最多 10 条
     */
    List<CouponInfoResDTO> findMyCoupons(Long userId, Integer status, Long lastId);
}
