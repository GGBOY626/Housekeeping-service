package com.jzo2o.market.handler;

import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.redis.constants.RedisSyncQueueConstants;
import com.jzo2o.redis.sync.SyncManager;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

import static com.jzo2o.market.constants.RedisConstants.RedisKey.COUPON_SEIZE_SYNC_QUEUE_NAME;

@Component
@Slf4j
public class XxlJobHandler {

    @Resource
    private SyncManager syncManager;

    @Resource
    private IActivityService activityService;

    @Resource
    private ICouponService couponService;

    @Resource(name = "syncThreadPool")
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 活动状态到期变更任务：待生效→进行中、待生效/进行中→已失效
     */
    @XxlJob("updateActivityStatus")
    public void updateActivityStatus() {
        log.info("定时修改活动状态...");
        try {
            activityService.updateStatus();
        } catch (Exception e) {
            log.error("updateActivityStatus error", e);
        }
    }

    /**
     * 已领取优惠券自动过期任务
     */
    @XxlJob("processExpireCoupon")
    public void processExpireCoupon() {
        log.info("已领取优惠券自动过期任务...");
        try {
            couponService.processExpireCoupon();
        } catch (Exception e) {
            log.error("processExpireCoupon error", e);
        }
    }

    /**
     * 活动预热：将满足条件的活动同步到 Redis 供抢券页查询
     */
    @XxlJob("activityPreheat")
    public void activityPreHeat() {
        log.info("优惠券活动定时预热...");
        try {
            activityService.preHeat();
        } catch (Exception e) {
            log.error("activityPreHeat error", e);
        }
    }

    /**
     * 抢券同步任务：从 Redis 同步队列消费抢券记录，落库优惠券表并扣减活动库存
     */
    @XxlJob("seizeCouponSyncJob")
    public void seizeCouponSyncJob() {
        log.info("抢券同步队列任务开始...");
        try {
            syncManager.start(
                    COUPON_SEIZE_SYNC_QUEUE_NAME,
                    RedisSyncQueueConstants.STORAGE_TYPE_HASH,
                    RedisSyncQueueConstants.MODE_SINGLE,
                    threadPoolExecutor
            );
        } catch (Exception e) {
            log.error("seizeCouponSyncJob error", e);
        }
    }
}
