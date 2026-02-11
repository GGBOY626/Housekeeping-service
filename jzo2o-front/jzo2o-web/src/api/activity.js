import request from '@/utils/request'

const prefix = '/market/consumer/activity'

/** 抢券活动列表 tabType: 1 疯抢中 2 即将开始 */
export const getActivityList = (params) =>
  request({ url: `${prefix}/list`, method: 'get', params })
