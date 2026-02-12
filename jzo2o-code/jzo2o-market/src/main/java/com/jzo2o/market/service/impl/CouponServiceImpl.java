package com.jzo2o.market.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.db.DbRuntimeException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.api.market.dto.request.CouponUseReqDTO;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.api.market.dto.response.CouponUseResDTO;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.utils.ObjectUtils;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.expcetions.DBException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.*;
import com.jzo2o.market.enums.ActivityStatusEnum;
import com.jzo2o.market.enums.CouponStatusEnum;
import com.jzo2o.market.mapper.CouponMapper;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.domain.CouponUseBack;
import com.jzo2o.market.model.domain.CouponWriteOff;
import com.jzo2o.market.model.dto.request.CouponOperationPageQueryReqDTO;
import com.jzo2o.market.model.dto.request.SeizeCouponReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.market.service.ICouponUseBackService;
import com.jzo2o.market.service.ICouponWriteOffService;
import com.jzo2o.market.utils.CouponUtils;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.mysql.utils.PageUtils;
import com.jzo2o.redis.utils.RedisSyncQueueUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.jzo2o.common.constants.ErrorInfo.Code.SEIZE_COUPON_FAILD;
import static com.jzo2o.market.constants.RedisConstants.RedisKey.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {

    @Resource(name = "seizeCouponScript")
    private DefaultRedisScript<String> seizeCouponScript;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private IActivityService activityService;

    @Resource
    private ICouponUseBackService couponUseBackService;

    @Resource
    private ICouponWriteOffService couponWriteOffService;

    @Override
    public PageResult<CouponInfoResDTO> findByPage(CouponOperationPageQueryReqDTO dto) {
        if (dto.getActivityId() == null) {
            throw new BadRequestException("请指定活动id");
        }
        Page<Coupon> page = PageUtils.parsePageQuery(dto, Coupon.class);
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getActivityId, dto.getActivityId());
        page = this.page(page, wrapper);
        return PageUtils.toPage(page, CouponInfoResDTO.class);
    }

    @Override
    public void processExpireCoupon() {
        // 未使用且有效期已过的优惠券改为已失效
        this.lambdaUpdate()
                .eq(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus())
                .le(Coupon::getValidityTime, LocalDateTime.now())
                .set(Coupon::getStatus, CouponStatusEnum.INVALID.getStatus())
                .update();
    }

    @Override
    public List<CouponInfoResDTO> findMyCoupons(Long userId, Integer status, Long lastId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getUserId, userId);
        if (status != null) {
            wrapper.eq(Coupon::getStatus, status);
        }
        wrapper.orderByDesc(Coupon::getCreateTime);
        if (lastId != null) {
            wrapper.lt(Coupon::getId, lastId);
        }
        wrapper.last("LIMIT 10");
        List<Coupon> list = this.list(wrapper);
        return list.stream().map(e -> BeanUtil.copyProperties(e, CouponInfoResDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<CouponInfoResDTO> queryForList(Long lastId, Long userId, Integer status) {
        List<Coupon> list = this.lambdaQuery()
                .eq(Coupon::getUserId, userId)
                .eq(status != null, Coupon::getStatus, status)
                .lt(lastId != null, Coupon::getId, lastId)
                .orderByDesc(Coupon::getCreateTime)
                .last("limit 10")
                .list();
        return BeanUtils.copyToList(list, CouponInfoResDTO.class);
    }

    @Override
    public List<AvailableCouponsResDTO> getAvailable(BigDecimal totalAmount) {
        Long userId = UserContext.currentUserId();
        if (userId == null || totalAmount == null) {
            return new ArrayList<>();
        }
        // 1. 查询优惠券：当前用户、未使用、有效期内、满减条件 <= 订单总额
        List<Coupon> list = this.lambdaQuery()
                .eq(Coupon::getUserId, userId)
                .eq(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus())
                .ge(Coupon::getValidityTime, LocalDateTime.now())
                .le(Coupon::getAmountCondition, totalAmount)
                .list();
        // 2. 计算每张券对应当前订单的优惠金额，过滤 0 < 优惠金额 < 订单金额，按优惠金额从大到小排序
        List<Coupon> collect = list.stream()
                .peek(e -> e.setDiscountAmount(CouponUtils.calDiscountAmount(e, totalAmount)))
                .filter(e -> e.getDiscountAmount() != null
                        && e.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0
                        && e.getDiscountAmount().compareTo(totalAmount) < 0)
                .sorted(Comparator.comparing(Coupon::getDiscountAmount).reversed())
                .collect(Collectors.toList());
        // 3. 转为 DTO 返回
        return collect.stream()
                .map(e -> BeanUtil.copyProperties(e, AvailableCouponsResDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponUseResDTO use(CouponUseReqDTO couponUseReqDTO) {
        if (couponUseReqDTO == null || couponUseReqDTO.getId() == null || couponUseReqDTO.getOrdersId() == null || couponUseReqDTO.getTotalAmount() == null) {
            throw new ForbiddenOperationException("优惠券核销失败");
        }
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new ForbiddenOperationException("优惠券核销失败");
        }
        // 1. 校验优惠券：当前用户、未使用、有效期内、满减条件<=订单总额、指定券id
        Coupon coupon = this.lambdaQuery()
                .eq(Coupon::getUserId, userId)
                .eq(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus())
                .ge(Coupon::getValidityTime, LocalDateTime.now())
                .le(Coupon::getAmountCondition, couponUseReqDTO.getTotalAmount())
                .eq(Coupon::getId, couponUseReqDTO.getId())
                .one();
        if (ObjectUtils.isNull(coupon)) {
            throw new ForbiddenOperationException("优惠券核销失败");
        }
        // 2. 更新优惠券：已使用、使用时间、订单id
        coupon.setStatus(CouponStatusEnum.USED.getStatus());
        coupon.setUseTime(LocalDateTime.now());
        coupon.setOrdersId(couponUseReqDTO.getOrdersId().toString());
        this.updateById(coupon);
        // 3. 插入核销记录
        CouponWriteOff couponWriteOff = new CouponWriteOff();
        couponWriteOff.setCouponId(couponUseReqDTO.getId());
        couponWriteOff.setUserId(userId);
        couponWriteOff.setOrdersId(couponUseReqDTO.getOrdersId());
        couponWriteOff.setActivityId(coupon.getActivityId());
        couponWriteOff.setWriteOffTime(LocalDateTime.now());
        couponWriteOff.setWriteOffManPhone(coupon.getUserPhone());
        couponWriteOff.setWriteOffManName(coupon.getUserName());
        couponWriteOffService.save(couponWriteOff);
        // 4. 返回优惠金额
        BigDecimal discountAmount = CouponUtils.calDiscountAmount(coupon, couponUseReqDTO.getTotalAmount());
        CouponUseResDTO res = new CouponUseResDTO();
        res.setDiscountAmount(discountAmount);
        return res;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useBack(CouponUseBackReqDTO couponUseBackReqDTO) {
        // 1. 检查优惠券是否有核销记录，没有则不需要退回
        CouponWriteOff couponWriteOff = couponWriteOffService.lambdaQuery()
                .eq(CouponWriteOff::getOrdersId, couponUseBackReqDTO.getOrdersId())
                .eq(CouponWriteOff::getUserId, couponUseBackReqDTO.getUserId())
                .one();
        if (ObjectUtils.isNull(couponWriteOff)) {
            return;
        }

        //2. 在优惠券退回表中添加记录
        CouponUseBack couponUseBack = new CouponUseBack();
        couponUseBack.setCouponId(couponWriteOff.getCouponId());//优惠券id 千万不要从couponUseBackReqDTO对象中获取
        couponUseBack.setUserId(couponUseBackReqDTO.getUserId());
        couponUseBack.setUseBackTime(LocalDateTime.now());
        couponUseBack.setWriteOffTime(couponWriteOff.getWriteOffTime());
        couponUseBackService.save(couponUseBack);

        //3. 更新优惠券表中的状态字段，并清空订单id及使用时间字段
        // 3-1 根据优惠券 id 查询信息
        Coupon coupon = this.getById(couponWriteOff.getCouponId());
        if (ObjectUtils.isNull(coupon)) {
            throw new ForbiddenOperationException("优惠券退回失败,原因: 没有对应的优惠券信息");
        }

        // 3-2 根据活动 id 查询信息
        Activity activity = activityService.getById(coupon.getActivityId());
        if (ObjectUtils.isNull(activity)) {
            throw new ForbiddenOperationException("优惠券退回失败,原因: 没有对应的优惠券活动信息");
        }

        //3-3 如果优惠券已过期则标记为已失效，如果未过期，则标记为未使用
        CouponStatusEnum couponStatusEnum = coupon.getValidityTime().isAfter(LocalDateTime.now())
                ? CouponStatusEnum.NO_USE : CouponStatusEnum.INVALID;

        //3-4 如果优惠券对应的活动已作废则标记为已作废
        if (activity.getStatus().equals(ActivityStatusEnum.VOIDED.getStatus())){
            couponStatusEnum = CouponStatusEnum.VOIDED;
        }

        //3-5 执行优惠券的更新
//下面的写法无法对数据表字段进行空值更新,要改为使用lambdaUpdate来处理. 此问题在测试视频中专门有讲解
//        coupon.setStatus(couponStatusEnum.getStatus());
//        coupon.setOrdersId(null);
//        coupon.setUseTime(null);
//        boolean b = this.updateById(coupon);

        boolean b = this.lambdaUpdate()
                .set(Coupon::getStatus, couponStatusEnum.getStatus())
                .set(Coupon::getOrdersId, null)
                .set(Coupon::getUseTime, null)
                .eq(Coupon::getId, coupon.getId())
                .update();
        if (!b) {
            throw new ForbiddenOperationException("优惠券退回失败,原因: 更新优惠券失败");
        }

        //4. 删除优惠券核销表中的相关记录
        boolean b1 = couponWriteOffService.removeById(couponWriteOff.getId());
        if (!b1) {
            throw new ForbiddenOperationException("优惠券退回失败,原因: 删除优惠券核销记录失败");
        }
    }

    @Override
    public void seize(Long activityId) {
        if (activityId == null) {
            throw new BadRequestException("活动id不能为空");
        }
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new ForbiddenOperationException("请先登录");
        }
        int index = (int) (activityId % 10);
        String syncQueueKey = RedisSyncQueueUtils.getQueueRedisKey(COUPON_SEIZE_SYNC_QUEUE_NAME, index);
        String stockKey = String.format(COUPON_RESOURCE_STOCK, index);
        String seizeListKey = String.format(COUPON_SEIZE_LIST, activityId, index);
        List<String> keys = Arrays.asList(syncQueueKey, stockKey, seizeListKey);
        StringRedisSerializer strSerializer = new StringRedisSerializer();
        // 必须用 String 序列化器传参，否则默认 value 序列化器(Jackson) 会把 ARGV 写成 JSON 带引号，Lua 里 HGET 的 field 对不上
        Object result = redisTemplate.execute(seizeCouponScript, strSerializer, strSerializer, keys, activityId.toString(), userId.toString());
        if (result == null) {
            throw new BadRequestException("抢券失败");
        }
        String code = result.toString();
        if ("-2".equals(code)) {
            // Redis 无库存：可能是预热未跑或未同步该活动，用与脚本一致的序列化方式写入库存后重试
            Activity activity = activityService.getById(activityId);
            if (activity != null && activity.getStockNum() != null && activity.getStockNum() > 0
                    && activity.getStatus() != null && activity.getStatus().equals(ActivityStatusEnum.DISTRIBUTING.getStatus())) {
                String stockVal = String.valueOf(activity.getStockNum());
                StringRedisSerializer strSer = new StringRedisSerializer();
                redisTemplate.execute((RedisCallback<Void>) conn -> {
                    byte[] k = strSer.serialize(stockKey);
                    byte[] f = strSer.serialize(activityId.toString());
                    byte[] v = strSer.serialize(stockVal);
                    conn.hSet(k, f, v);
                    return null;
                });
                log.info("seize fallback: synced stock key={} activityId={} stock={}", stockKey, activityId, stockVal);
                result = redisTemplate.execute(seizeCouponScript, strSerializer, strSerializer, keys, activityId.toString(), userId.toString());
                if (result != null) {
                    code = result.toString();
                }
            }
        }
        switch (code) {
            case "-1":
                throw new ForbiddenOperationException("该活动每人限领一张");
            case "-2":
                throw new BadRequestException("库存不足，已抢光");
            case "-3":
            case "-4":
            case "-5":
                throw new BadRequestException("抢券失败，请稍后重试");
            default:
                break;
        }
    }
}
