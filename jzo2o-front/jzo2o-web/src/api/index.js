import request from '@/utils/request'

export const getHomeService = (params) =>
  request({
    url: '/foundations/customer/serve/firstPageServeList',
    method: 'get',
    params
  })

export const getHotServe = (params) =>
  request({
    url: '/foundations/customer/serve/hotServeList',
    method: 'get',
    params
  })

export const getServeSearch = (params) =>
  request({
    url: '/foundations/customer/serve/search',
    method: 'get',
    params
  })
