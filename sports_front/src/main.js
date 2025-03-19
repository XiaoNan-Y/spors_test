import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import axios from 'axios'  // 直接引入 axios
import request from '@/utils/request'

Vue.use(ElementUI, {
  tableOptions: {
    mousewheel: {
      passive: true
    }
  }
})
Vue.config.productionTip = false

// 配置 axios 默认值
axios.defaults.baseURL = process.env.VUE_APP_API_URL || 'http://localhost:8081'
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 将 axios 和封装的 request 都挂载到 Vue 原型上
Vue.prototype.$http = axios  // 原始 axios
Vue.prototype.$axios = request  // 封装的 request

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app') 