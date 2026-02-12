import request from '@/utils/request'

const prefix = '/market/consumer/coupon'

/** 我的优惠券列表 status: 1未使用 2已使用 3已过期，不传为全部；lastId 游标分页 */
export const getMyCouponList = (params) =>
  request({ url: `${prefix}/my`, method: 'get', params })

/** 抢券 id: 活动id */
export const seizeCoupon = (params) =>
  request({ url: `${prefix}/seize`, method: 'post', params })

/** 下单页可用优惠券 serveId 服务id purNum 购买数量 */
export const getAvailableCoupons = (params) =>
  request({ url: '/orders-manager/consumer/orders/getAvailableCoupons', method: 'get', params })
