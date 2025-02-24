import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import axios from 'axios'

Vue.use(ElementUI)
Vue.config.productionTip = false

// axios配置
axios.defaults.baseURL = '/api'  // 确保只有一个 /api
axios.defaults.timeout = 5000

// 请求拦截器
axios.interceptors.request.use(
  config => {
    console.log('Request:', config)
    // 添加日志，方便调试
    console.log('Request URL:', config.url)
    console.log('Request Method:', config.method)
    console.log('Request Data:', config.data)
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
      // 服务器返回错误状态码
      ElementUI.Message.error(error.response.data.message || '服务器错误')
    } else if (error.request) {
      // 请求发出但没有收到响应
      ElementUI.Message.error('网络连接失败，请检查网络')
    } else {
      // 请求配置出错
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