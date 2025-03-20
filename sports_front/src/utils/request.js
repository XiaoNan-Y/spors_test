import axios from 'axios'
import { Message } from 'element-ui'

// 创建 axios 实例
const service = axios.create({
  baseURL: 'http://localhost:8080', // 后端服务地址
  timeout: 5000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    const userId = localStorage.getItem('userId')
    
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    if (userId) {
      config.headers['userId'] = userId
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 打印响应数据，帮助调试
    console.log('API响应:', res)
    
    if (res.code !== 200) {
      Message({
        message: res.message || res.msg || '错误',
        type: 'error',
        duration: 5 * 1000
      })
      
      // 修改这里，返回整个响应，让调用方可以处理错误
      return Promise.reject(res.message || res.msg || '错误')
    }
    return response
  },
  error => {
    console.error('响应错误:', error)
    // 如果有响应数据
    if (error.response && error.response.data) {
      console.log('错误详情:', error.response.data)
      Message({
        message: error.response.data.message || error.message || '请求失败',
        type: 'error',
        duration: 5 * 1000
      })
    } else {
      Message({
        message: error.message || '请求失败',
        type: 'error',
        duration: 5 * 1000
      })
    }
    return Promise.reject(error)
  }
)

export default service 