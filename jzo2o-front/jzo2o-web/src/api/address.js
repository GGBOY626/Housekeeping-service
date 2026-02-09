import request from '@/utils/request'

export const getAddress = (params) =>
  request({
    url: '/publics/map/location',
    method: 'get',
    params
  })

export const getCityList = () =>
  request({
    url: '/foundations/consumer/region/activeRegionList',
    method: 'get'
  })

/** 获取用户默认地址 */
export const getDefaultAddress = () =>
  request({
    url: '/customer/consumer/address-book/defaultAddress',
    method: 'get'
  })

/** 地址薄分页查询 */
export const getAddressBookPage = (params) =>
  request({
    url: '/customer/consumer/address-book/page',
    method: 'get',
    params
  })

/** 新增地址 */
export const addAddressBook = (data) =>
  request({
    url: '/customer/consumer/address-book',
    method: 'post',
    data
  })

/** 地址详情 */
export const getAddressBookDetail = (id) =>
  request({
    url: `/customer/consumer/address-book/${id}`,
    method: 'get'
  })

/** 修改地址 */
export const updateAddressBook = (id, data) =>
  request({
    url: `/customer/consumer/address-book/${id}`,
    method: 'put',
    data
  })

/** 批量删除地址 */
export const deleteAddressBookBatch = (ids) =>
  request({
    url: '/customer/consumer/address-book/batch',
    method: 'delete',
    data: ids
  })

/** 设为默认/取消默认 */
export const setDefaultAddress = (id, flag) =>
  request({
    url: `/customer/consumer/address-book/default?id=${id}&flag=${flag}`,
    method: 'put'
  })
