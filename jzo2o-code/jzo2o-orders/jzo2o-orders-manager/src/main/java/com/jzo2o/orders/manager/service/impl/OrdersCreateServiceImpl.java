package com.jzo2o.orders.manager.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.customer.AddressBookApi;
import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.api.foundations.ServeApi;
import com.jzo2o.api.foundations.dto.response.ServeAggregationResDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
    private IOrdersCreateService owner;

    @Override
    public PlaceOrderResDTO placeOrder(PlaceOrderReqDTO placeOrderReqDTO) {
        return owner.placeOrder(UserContext.currentUserId(), placeOrderReqDTO);
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

        // 4. 保存（通过代理调用，使 saveOrders 上的 @Transactional 生效，避免同类内部调用导致事务失效）
        owner.saveOrders(orders);

        // 5. 返回
        return new PlaceOrderResDTO(orders.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrders(Orders orders) {
        this.save(orders);
        // 模拟异常测试事务回滚时可取消注释：int i = 1 / 0;
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
