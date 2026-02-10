package com.jzo2o.market.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.market.service.ICouponWriteOffService;
import com.jzo2o.mysql.utils.PageUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        this.lambdaUpdate()
                .eq(Activity::getStatus, NO_DISTRIBUTE.getStatus())
                .le(Activity::getDistributeStartTime, LocalDateTime.now())
                .gt(Activity::getDistributeEndTime, LocalDateTime.now())
                .set(Activity::getStatus, DISTRIBUTING.getStatus())
                .update();
        this.lambdaUpdate()
                .in(Activity::getStatus, NO_DISTRIBUTE.getStatus(), DISTRIBUTING.getStatus())
                .lt(Activity::getDistributeEndTime, LocalDateTime.now())
                .set(Activity::getStatus, ActivityStatusEnum.LOSE_EFFICACY.getStatus())
                .update();
    }
}
