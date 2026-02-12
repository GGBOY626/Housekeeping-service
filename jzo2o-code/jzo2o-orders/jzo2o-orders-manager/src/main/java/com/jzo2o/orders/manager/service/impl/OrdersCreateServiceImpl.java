package com.jzo2o.orders.manager.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.customer.AddressBookApi;
import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.api.foundations.ServeApi;
import com.jzo2o.api.foundations.dto.response.ServeAggregationResDTO;
import com.jzo2o.api.market.CouponApi;
import com.jzo2o.api.market.dto.request.CouponUseReqDTO;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.api.market.dto.response.CouponUseResDTO;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.utils.DateUtils;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.orders.base.constants.RedisConstants;
import com.jzo2o.orders.base.model.domain.Orders;
import com.jzo2o.orders.base.mapper.OrdersMapper;
import com.jzo2o.orders.manager.model.dto.request.PlaceOrderReqDTO;
import com.jzo2o.orders.manager.model.dto.response.PlaceOrderResDTO;
import com.jzo2o.orders.manager.service.IOrdersCreateService;
import com.jzo2o.redis.annotations.Lock;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * <p>
 * 下单服务类
 * </p>
 *
 * @author itcast
 * @since 2023-07-10
 */
@Slf4j
@Service
public class OrdersCreateServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersCreateService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ServeApi serveApi;

    @Autowired
    private AddressBookApi addressBookApi;

    @Autowired
    private CouponApi couponApi;

    @Autowired
    private IOrdersCreateService owner;

    @Override
    public PlaceOrderResDTO placeOrder(PlaceOrderReqDTO placeOrderReqDTO) {
        return owner.placeOrder(UserContext.currentUserId(), placeOrderReqDTO);
    }

    @Override
    public List<AvailableCouponsResDTO> getAvailableCoupons(Long serveId, Integer purNum) {
        ServeAggregationResDTO serveResDTO = serveApi.findById(serveId);
        if (serveResDTO == null || serveResDTO.getSaleStatus() != 2) {
            throw new ForbiddenOperationException("服务不可用");
        }
        BigDecimal totalAmount = serveResDTO.getPrice().multiply(new BigDecimal(purNum));
        return couponApi.getAvailable(totalAmount);
    }

    @Lock(formatter = "ORDERS:CREATE:LOCK:#{userId}:#{placeOrderReqDTO.serveId}", time = 30, waitTime = 1, unlock = false)
    @Override
    public PlaceOrderResDTO placeOrder(Long userId, PlaceOrderReqDTO placeOrderReqDTO) {
        // 1. 调用运营微服务，根据服务id查询
        ServeAggregationResDTO serveDto = serveApi.findById(placeOrderReqDTO.getServeId());
        if (ObjectUtil.isNull(serveDto) || serveDto.getSaleStatus() != 2) {
            throw new ForbiddenOperationException("服务不存在或者状态有误");
        }

        // 2. 调用 customer 微服务，根据地址id查询信息
        AddressBookResDTO addressDto = addressBookApi.detail(placeOrderReqDTO.getAddressBookId());
        if (ObjectUtil.isNull(addressDto)) {
            throw new ForbiddenOperationException("服务地址有误");
        }

        // 3. 准备 Orders 实体
        Orders orders = new Orders();
        orders.setId(generateOrderId());
        orders.setUserId(userId);
        orders.setServeId(placeOrderReqDTO.getServeId());

        orders.setServeTypeId(serveDto.getServeTypeId());
        orders.setServeTypeName(serveDto.getServeTypeName());
        orders.setServeItemId(serveDto.getServeItemId());
        orders.setServeItemName(serveDto.getServeItemName());
        orders.setServeItemImg(serveDto.getServeItemImg());
        orders.setUnit(serveDto.getUnit());
        orders.setPrice(serveDto.getPrice());
        orders.setCityCode(serveDto.getCityCode());

        orders.setOrdersStatus(0);
        orders.setPayStatus(2);

        int purNum = placeOrderReqDTO.getPurNum() == null || placeOrderReqDTO.getPurNum() < 1 ? 1 : placeOrderReqDTO.getPurNum();
        orders.setPurNum(purNum);
        orders.setTotalAmount(serveDto.getPrice().multiply(BigDecimal.valueOf(purNum)));
        orders.setDiscountAmount(BigDecimal.ZERO);
        orders.setRealPayAmount(orders.getTotalAmount().subtract(orders.getDiscountAmount()));

        orders.setServeAddress(addressDto.getAddress());
        orders.setContactsPhone(addressDto.getPhone());
        orders.setContactsName(addressDto.getName());
        orders.setLon(addressDto.getLon());
        orders.setLat(addressDto.getLat());

        orders.setServeStartTime(placeOrderReqDTO.getServeStartTime());
        orders.setDisplay(1);
        long sortBy = placeOrderReqDTO.getServeStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + orders.getId() % 100000;
        orders.setSortBy(sortBy);

        // 4. 保存订单（有优惠券则先核销再落库，无则直接落库）
        if (placeOrderReqDTO.getCouponId() != null && placeOrderReqDTO.getCouponId() > 0) {
            owner.saveOrdersWithCoupon(orders, placeOrderReqDTO.getCouponId());
        } else {
            owner.saveOrders(orders);
        }

        // 5. 返回（含实付金额，前端用于支付页展示与发起支付）
        return new PlaceOrderResDTO(orders.getId(), orders.getRealPayAmount());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrders(Orders orders) {
        this.save(orders);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @GlobalTransactional
    public void saveOrdersWithCoupon(Orders orders, Long couponId) {
        CouponUseReqDTO couponUseReqDTO = new CouponUseReqDTO();
        couponUseReqDTO.setId(couponId);
        couponUseReqDTO.setOrdersId(orders.getId());
        couponUseReqDTO.setTotalAmount(orders.getTotalAmount());
        CouponUseResDTO couponUseResDTO = couponApi.use(couponUseReqDTO);

        BigDecimal discountAmount = couponUseResDTO.getDiscountAmount();
        orders.setDiscountAmount(discountAmount);
        orders.setRealPayAmount(orders.getTotalAmount().subtract(discountAmount));

        this.save(orders);
    }

    /**
     * 生成订单id
     * 19位：2位年+2位月+2位日+13位序号(自增)
     *
     * @return 订单id
     */
    private Long generateOrderId() {
        Long yyMMdd = DateUtils.getFormatDate(LocalDateTime.now(), "yyMMdd");
        Long num = redisTemplate.opsForValue().increment(RedisConstants.Lock.ORDERS_SHARD_KEY_ID_GENERATOR, 1);
        return yyMMdd * 10000000000000L + num;
    }
}
