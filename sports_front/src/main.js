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
axios.defaults.baseURL = process.env.VUE_APP_BASE_API
axios.defaults.timeout = 5000

// 将 axios 和封装的 request 都挂载到 Vue 原型上
Vue.prototype.$http = axios  // 原始 axios
Vue.prototype.$axios = request  // 封装的 request

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app') 