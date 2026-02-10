import request from '@/utils/request'

const prefix = '/orders-manager/consumer/orders'

/** 下单 */
export const placeOrder = (data) =>
  request({ url: `${prefix}/place`, method: 'post', data })

/** 订单详情 */
export const getOrderDetail = (id) =>
  request({ url: `${prefix}/${id}`, method: 'get' })

/** 订单列表（滚动分页）ordersStatus: 0待支付 100派单中 200待服务 300服务中 400待评价 500完成 600取消 700关闭 */
export const getOrderList = (params) =>
  request({ url: `${prefix}/consumerQueryList`, method: 'get', params })

/** 创建 Stripe 支付意图 */
export const createPaymentIntent = (data) =>
  request({ url: `${prefix}/create-payment-intent`, method: 'post', data })

/** 确认支付成功 */
export const confirmPaid = (data) =>
  request({ url: `${prefix}/confirm-paid`, method: 'post', data })

/** 取消订单（id 建议传字符串避免大数精度丢失） */
export const cancelOrder = (data) =>
  request({ url: `${prefix}/cancel`, method: 'put', data })
