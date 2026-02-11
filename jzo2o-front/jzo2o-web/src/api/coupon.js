import request from '@/utils/request'

const prefix = '/market/consumer/coupon'

/** 我的优惠券列表 status: 1未使用 2已使用 3已过期，不传为全部；lastId 游标分页 */
export const getMyCouponList = (params) =>
  request({ url: `${prefix}/my`, method: 'get', params })
