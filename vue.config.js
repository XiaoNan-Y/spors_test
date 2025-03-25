module.exports = {
  devServer: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://10.90.56.69:8080',
        changeOrigin: true,
        ws: true
      }
    }
  }
} 