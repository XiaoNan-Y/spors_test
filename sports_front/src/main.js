import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import request from '@/utils/request'  // 只使用封装的 request

Vue.use(ElementUI, {
  tableOptions: {
    mousewheel: {
      passive: true
    }
  }
})
Vue.config.productionTip = false

// 只使用封装的 request 实例
Vue.prototype.$http = request  // 使用封装的 request
Vue.prototype.$axios = request  // 保持兼容性

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app') 