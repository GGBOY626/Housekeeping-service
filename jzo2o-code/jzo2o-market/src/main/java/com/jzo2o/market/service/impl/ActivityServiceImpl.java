package com.jzo2o.market.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.market.enums.ActivityStatusEnum;
import com.jzo2o.market.enums.CouponStatusEnum;
import com.jzo2o.market.mapper.ActivityMapper;
import com.jzo2o.market.mapper.CouponMapper;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.domain.CouponWriteOff;
import com.jzo2o.market.model.dto.request.ActivityQueryForPageReqDTO;
import com.jzo2o.market.model.dto.request.ActivitySaveReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.model.dto.response.CountResDTO;
import com.jzo2o.market.model.dto.response.SeizeCouponInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.market.service.ICouponWriteOffService;
import com.jzo2o.mysql.utils.PageUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jzo2o.market.constants.RedisConstants.RedisKey.ACTIVITY_CACHE_LIST;
import static com.jzo2o.market.enums.ActivityStatusEnum.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements IActivityService {

    @Resource
    private ICouponService couponService;

    @Resource
    private ICouponWriteOffService couponWriteOffService;

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void saveOrUpdateActivity(ActivitySaveReqDTO dto) {
        dto.check();
        Activity activity = BeanUtil.copyProperties(dto, Activity.class);
        activity.setStatus(NO_DISTRIBUTE.getStatus());
        activity.setStockNum(activity.getTotalNum() != null ? activity.getTotalNum() : 0);
        this.saveOrUpdate(activity);
    }

    @Override
    public PageResult<ActivityInfoResDTO> findByPage(ActivityQueryForPageReqDTO dto) {
        Page<Activity> page = PageUtils.parsePageQuery(dto, Activity.class);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getId() != null, Activity::getId, dto.getId())
                .like(StringUtils.isNotEmpty(dto.getName()), Activity::getName, dto.getName())
                .eq(dto.getType() != null, Activity::getType, dto.getType())
                .eq(dto.getStatus() != null, Activity::getStatus, dto.getStatus());
        page = this.page(page, wrapper);
        if (CollUtil.isEmpty(page.getRecords())) {
            return new PageResult<>(page.getPages(), page.getTotal(), List.of());
        }
        List<Long> activityIdList = page.getRecords().stream().map(Activity::getId).collect(Collectors.toList());
        List<CountResDTO> countResDTOList = couponMapper.countByActivityIdList(activityIdList);
        Map<Long, Integer> receiveNumMap = countResDTOList.stream().collect(Collectors.toMap(CountResDTO::getActivityId, CountResDTO::getNum));
        List<ActivityInfoResDTO> list = page.getRecords().stream().map(e -> {
            ActivityInfoResDTO res = BeanUtil.copyProperties(e, ActivityInfoResDTO.class);
            res.setReceiveNum(receiveNumMap.getOrDefault(e.getId(), 0));
            Integer writeOffNum = couponWriteOffService.lambdaQuery().eq(CouponWriteOff::getActivityId, e.getId()).count();
            res.setWriteOffNum(writeOffNum);
            return res;
        }).collect(Collectors.toList());
        return new PageResult<>(page.getPages(), page.getTotal(), list);
    }

    @Override
    public ActivityInfoResDTO findById(Long id) {
        Activity activity = this.getById(id);
        if (activity == null) {
            throw new ForbiddenOperationException("当前优惠券活动不存在");
        }
        ActivityInfoResDTO res = BeanUtil.copyProperties(activity, ActivityInfoResDTO.class);
        Integer receiveNum = couponService.lambdaQuery().eq(Coupon::getActivityId, id).count();
        res.setReceiveNum(receiveNum);
        Integer writeOffNum = couponWriteOffService.lambdaQuery().eq(CouponWriteOff::getActivityId, id).count();
        res.setWriteOffNum(writeOffNum);
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long id) {
        Activity activity = this.getById(id);
        if (activity == null) {
            throw new ForbiddenOperationException("当前活动不存在");
        }
        if (activity.getStatus() != NO_DISTRIBUTE.getStatus() && activity.getStatus() != DISTRIBUTING.getStatus()) {
            throw new ForbiddenOperationException("当前活动状态不允许撤销");
        }
        boolean flag = this.lambdaUpdate()
                .eq(Activity::getId, id)
                .in(Activity::getStatus, NO_DISTRIBUTE.getStatus(), DISTRIBUTING.getStatus())
                .set(Activity::getStatus, ActivityStatusEnum.VOIDED.getStatus())
                .update();
        if (flag) {
            couponService.lambdaUpdate()
                    .eq(Coupon::getActivityId, id)
                    .eq(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus())
                    .set(Coupon::getStatus, CouponStatusEnum.VOIDED.getStatus())
                    .update();
        }
    }

    @Override
    public void updateStatus() {
        LocalDateTime now = LocalDateTime.now();
        // 待生效(1)：到达发放开始时间且未到结束时间 -> 进行中(2)
        this.lambdaUpdate()
                .eq(Activity::getStatus, NO_DISTRIBUTE.getStatus())
                .le(Activity::getDistributeStartTime, now)
                .gt(Activity::getDistributeEndTime, now)
                .set(Activity::getStatus, DISTRIBUTING.getStatus())
                .update();
        // 待生效(1)或进行中(2)：到达发放结束时间 -> 已失效(3)
        this.lambdaUpdate()
                .in(Activity::getStatus, NO_DISTRIBUTE.getStatus(), DISTRIBUTING.getStatus())
                .lt(Activity::getDistributeEndTime, now)
                .set(Activity::getStatus, ActivityStatusEnum.LOSE_EFFICACY.getStatus())
                .update();
    }

    @Override
    public void preHeat() {
        // 查询状态为待生效(1)或进行中(2)，且发放开始时间距离现在不足1个月的活动，按开始时间升序
        List<Activity> list = this.lambdaQuery()
                .in(Activity::getStatus, NO_DISTRIBUTE.getStatus(), DISTRIBUTING.getStatus())
                .lt(Activity::getDistributeStartTime, LocalDateTime.now().plusMonths(1))
                .orderByAsc(Activity::getDistributeStartTime)
                .list();
        if (CollUtil.isEmpty(list)) {
            list = new ArrayList<>();
        }
        List<SeizeCouponInfoResDTO> dtoList = list.stream()
                .map(e -> BeanUtil.copyProperties(e, SeizeCouponInfoResDTO.class))
                .collect(Collectors.toList());
        String jsonStr = JSONUtil.toJsonStr(dtoList);
        redisTemplate.opsForValue().set(ACTIVITY_CACHE_LIST, jsonStr);
    }

    @Override
    public List<SeizeCouponInfoResDTO> queryForListFromCache(Integer tabType) {
        String jsonStr = (String) redisTemplate.opsForValue().get(ACTIVITY_CACHE_LIST);
        if (StringUtils.isEmpty(jsonStr)) {
            return List.of();
        }
        JSONArray array = JSONUtil.parseArray(jsonStr);
        if (array == null || array.isEmpty()) {
            return List.of();
        }
        List<SeizeCouponInfoResDTO> list = new ArrayList<>();
        ZoneId zone = ZoneId.systemDefault();
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            SeizeCouponInfoResDTO dto = obj.toBean(SeizeCouponInfoResDTO.class);
            // Redis 中可能是毫秒时间戳，需转成 LocalDateTime 才能正确计算真实状态
            if (dto.getDistributeStartTime() == null && obj.containsKey("distributeStartTime")) {
                Object v = obj.get("distributeStartTime");
                if (v instanceof Number) {
                    dto.setDistributeStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(((Number) v).longValue()), zone));
                }
            }
            if (dto.getDistributeEndTime() == null && obj.containsKey("distributeEndTime")) {
                Object v = obj.get("distributeEndTime");
                if (v instanceof Number) {
                    dto.setDistributeEndTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(((Number) v).longValue()), zone));
                }
            }
            list.add(dto);
        }
        return list.stream()
                .filter(e -> {
                    int realStatus = getStatus(e.getDistributeStartTime(), e.getDistributeEndTime(), e.getStatus());
                    if (tabType != null && tabType == 1) {
                        return realStatus == DISTRIBUTING.getStatus();
                    }
                    return realStatus == NO_DISTRIBUTE.getStatus();
                })
                .peek(e -> e.setRemainNum(e.getStockNum() != null ? e.getStockNum() : 0))
                .collect(Collectors.toList());
    }

    /**
     * 根据活动开始/结束时间与当前时间计算真实状态（解决缓存状态延迟）
     * 1. 待生效 且 开始时间<=当前<结束时间 -> 进行中
     * 2. 待生效 且 结束时间<当前 -> 已失效
     * 3. 进行中 且 结束时间<当前 -> 已失效
     * 4. 进行中 且 当前<开始时间 -> 待生效（未到开始时间）
     * 5. 其它保持原状态
     */
    private int getStatus(LocalDateTime distributeStartTime, LocalDateTime distributeEndTime, Integer status) {
        if (distributeStartTime == null || distributeEndTime == null || status == null) {
            return status != null ? status : 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (status == NO_DISTRIBUTE.getStatus()
                && !distributeStartTime.isAfter(now)
                && distributeEndTime.isAfter(now)) {
            return DISTRIBUTING.getStatus();
        }
        if (status == NO_DISTRIBUTE.getStatus() && !distributeEndTime.isAfter(now)) {
            return ActivityStatusEnum.LOSE_EFFICACY.getStatus();
        }
        if (status == DISTRIBUTING.getStatus() && !distributeEndTime.isAfter(now)) {
            return ActivityStatusEnum.LOSE_EFFICACY.getStatus();
        }
        if (status == DISTRIBUTING.getStatus() && distributeStartTime.isAfter(now)) {
            return NO_DISTRIBUTE.getStatus();
        }
        return status;
    }
}
