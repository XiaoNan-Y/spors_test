import axios from 'axios'
import { Message } from 'element-ui'

// 创建 axios 实例
const request = axios.create({
  baseURL: process.env.VUE_APP_API_URL || 'http://localhost:8080', // 后端服务地址
  timeout: 5000 // 请求超时时间
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token 和 userId
    const token = localStorage.getItem('token')
    const userId = localStorage.getItem('userId')
    const username = localStorage.getItem('username')
    
    // 添加详细的请求日志
    console.log('请求拦截器:', {
      url: config.url,
      method: config.method,
      token,
      userId,
      username,
      existingHeaders: config.headers
    })
    
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    if (userId) {
      config.headers['userId'] = userId
      console.log('设置请求头 userId:', userId)
    }
    
    // 打印最终的请求配置
    console.log('最终请求配置:', {
      url: config.url,
      method: config.method,
      headers: config.headers
    })
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 添加调试日志
    console.log('Response data:', res)
    
    if (res.code !== 200) {
      Message({
        message: res.message || res.msg || '错误',
        type: 'error',
        duration: 5 * 1000
      })
      return Promise.reject(res.message || res.msg || '错误')
    }
    return response
  },
  error => {
    console.error('Response error:', error)
    if (error.response && error.response.status === 401) {
      // 清除本地存储的用户信息
      localStorage.clear()
      // 重定向到登录页
      window.location.href = '/login'
    }
    Message.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  }
)

export default request 