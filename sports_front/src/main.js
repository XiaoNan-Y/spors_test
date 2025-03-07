import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import axios from 'axios'

Vue.use(ElementUI)
Vue.config.productionTip = false

// 设置基础URL - 修改这里，去掉重复的 /api
axios.defaults.baseURL = 'http://localhost:8080'  // 不在这里加 /api
axios.defaults.timeout = 10000  // 设置超时时间为10秒

// 请求拦截器
axios.interceptors.request.use(
  config => {
    console.log('Request:', config)
    // 从 localStorage 获取 token（如果有的话）
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
axios.interceptors.response.use(
  response => {
    console.log('Response:', response)
    return response
  },
  error => {
    console.error('Response Error:', error)
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权，清除 token 信息并跳转到登录页面
          localStorage.removeItem('token')
          router.push('/login')
          break
        case 404:
          ElementUI.Message.error('请求的资源不存在')
          break
        case 500:
          ElementUI.Message.error('服务器内部错误')
          break
        default:
          ElementUI.Message.error(error.response.data.msg || '请求失败')
      }
    } else if (error.request) {
      ElementUI.Message.error('网络连接失败，请检查网络')
    } else {
      ElementUI.Message.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

Vue.prototype.$http = axios

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app') 