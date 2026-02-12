package com.jzo2o.market.handler;

import com.jzo2o.api.customer.CommonUserApi;
import com.jzo2o.api.customer.dto.response.CommonUserResDTO;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.utils.NumberUtils;
import com.jzo2o.common.utils.ObjectUtils;
import com.jzo2o.market.enums.CouponStatusEnum;
import com.jzo2o.market.mapper.ActivityMapper;
import com.jzo2o.market.mapper.CouponMapper;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.redis.handler.SyncProcessHandler;
import com.jzo2o.redis.model.SyncMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static com.jzo2o.market.constants.RedisConstants.RedisKey.COUPON_SEIZE_SYNC_QUEUE_NAME;

/**
 * 抢券同步队列处理器
 * Bean 名称规则：队列名称 QUEUE:COUPON:SEIZE:SYNC:{index}，去掉 QUEUE: 和 :{index} 后为 COUPON:SEIZE:SYNC
 */
@Component(COUPON_SEIZE_SYNC_QUEUE_NAME)
@Slf4j
public class SeizeCouponSyncProcessHandler implements SyncProcessHandler<Object> {

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private CommonUserApi commonUserApi;

    @Override
    public void batchProcess(List<SyncMessage<Object>> multiData) {
        throw new UnsupportedOperationException("不支持批量处理");
    }

    @Override
    public void singleProcess(SyncMessage<Object> singleData) {
        log.info("获取要同步抢券结果数据： {}", singleData);
        long userId = NumberUtils.parseLong(singleData.getKey());
        long activityId = NumberUtils.parseLong(singleData.getValue().toString());

        log.info("userId={}, activityId={}", userId, activityId);

        // 1. 根据活动 id 查询活动
        Activity activity = activityMapper.selectById(activityId);
        if (ObjectUtils.isNull(activity)) {
            return;
        }

        // 2. 根据 userId 查询用户信息
        CommonUserResDTO userResDTO = commonUserApi.findById(userId);
        if (ObjectUtils.isNull(userResDTO)) {
            return;
        }

        // 3. 向优惠券表插入数据
        Coupon coupon = new Coupon();
        coupon.setName(activity.getName());
        coupon.setUserId(userId);
        coupon.setUserName(userResDTO.getNickname());
        coupon.setUserPhone(userResDTO.getPhone());
        coupon.setActivityId(activityId);
        coupon.setType(activity.getType());
        coupon.setDiscountRate(activity.getDiscountRate());
        coupon.setDiscountAmount(activity.getDiscountAmount());
        coupon.setAmountCondition(activity.getAmountCondition());
        coupon.setValidityTime(LocalDateTime.now().plusDays(activity.getValidityDays() != null ? activity.getValidityDays() : 0));
        coupon.setStatus(CouponStatusEnum.NO_USE.getStatus());
        couponMapper.insert(coupon);

        // 4. 扣减数据库活动库存
        int rows = activityMapper.deductStock(activityId);
        if (rows <= 0) {
            throw new CommonException("同步失败，库存扣减失败");
        }
    }
}
