import request from '@/utils/request'

// 账号密码登录
export const loginByPassword = (data) =>
  request({
    url: '/customer/open/login/common/user/password',
    method: 'post',
    data
  })

// 注册
export const register = (data) =>
  request({
    url: '/customer/open/login/common/user/register',
    method: 'post',
    data
  })
