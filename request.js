// 请求拦截器
axios.interceptors.request.use(
  config => {
    console.log('请求拦截器:', config);
    
    // 添加token和用户信息到请求头
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');
    
    if (token) {
      config.headers['Authorization'] = token;
    }
    
    if (userId) {
      console.log('设置请求头 userId:', userId);
      config.headers['X-User-ID'] = userId;
    }
    
    // 打印最终的请求配置
    console.log('最终请求配置:', config);
    
    return config;
  },
  error => {
    return Promise.reject(error);
  }
); 