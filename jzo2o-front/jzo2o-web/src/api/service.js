import request from '@/utils/request'

/** 查询当前区域下上架服务对应的分类 - 用于全部服务左侧 tab */
export const getServeTypeList = (params) =>
  request({ url: '/foundations/customer/serve/serveTypeList', method: 'get', params })

export const getServeCategory = (params) =>
  request({ url: '/foundations/customer/serve/serveTypeList', method: 'get', params })

export const getServeList = (params) =>
  request({ url: '/foundations/customer/serve/list', method: 'get', params })

export const getServeById = (id) =>
  request({ url: `/foundations/customer/serve/${id}`, method: 'get' })

/** 下单（立即预约） */
export const placeOrder = (data) =>
  request({ url: '/orders-manager/consumer/orders/place', method: 'post', data })
