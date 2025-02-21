module.exports = {
  devServer: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'  // 不重写路径
        }
      }
    }
  },
  // 关闭 ESLint 检查
  lintOnSave: false
} 