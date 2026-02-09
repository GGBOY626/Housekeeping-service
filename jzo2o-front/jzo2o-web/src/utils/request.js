import axios from 'axios'
import { baseUrl, notToLoginApiUrl } from './env'
import router from '@/router'

const instance = axios.create({
  baseURL: baseUrl,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json;charset=UTF-8' }
})

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

instance.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data.code === 0 || data.code === 200) {
      return data
    }
    if (data.code === 605) {
      alert(data.msg || '账号异常')
      return Promise.reject(data)
    }
    if (data.code === 401 || response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('nickName')
      if (!notToLoginApiUrl.some((url) => response.config.url?.includes(url))) {
        router.push('/login')
      }
      return Promise.reject(data)
    }
    return data
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('nickName')
      router.push('/login')
    } else {
      alert(error.response?.data?.msg || error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default instance
