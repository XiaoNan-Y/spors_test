<template>
  <div class="login-container">
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">体育测试管理系统</h3>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          placeholder="用户名"
          type="text"
          auto-complete="off">
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          placeholder="密码"
          type="password"
          auto-complete="off"
          @keyup.enter.native="handleLogin">
        </el-input>
      </el-form-item>
      <el-button
        :loading="loading"
        type="primary"
        style="width:100%;"
        @click.native.prevent="handleLogin">
        登录
      </el-button>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loading: false,
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (valid) {
          try {
            this.loading = true
            console.log('开始登录请求:', this.loginForm)
            
            const response = await this.$axios.post('/api/users/login', this.loginForm)
            console.log('登录响应:', response.data)
            
            if (response.data.code === 200) {
              // 先清除旧的登录信息
              localStorage.clear()
              
              const { id, token, userType, username, realName, className } = response.data.data
              
              // 存储新的用户信息
              localStorage.setItem('userId', id)
              localStorage.setItem('token', token)
              localStorage.setItem('userRole', userType)
              localStorage.setItem('username', username)
              localStorage.setItem('realName', realName)
              localStorage.setItem('className', className)
              
              // 打印存储的信息用于调试
              console.log('存储的用户信息:', {
                userId: localStorage.getItem('userId'),
                username: localStorage.getItem('username'),
                userRole: localStorage.getItem('userRole')
              })
              
              this.$message.success('登录成功')
              
              // 根据角色跳转
              switch (userType) {
                case 'STUDENT':
                  this.$router.push('/student/home')
                  break
                case 'TEACHER':
                  this.$router.push('/teacher/dashboard')
                  break
                case 'ADMIN':
                  this.$router.push('/admin/dashboard')
                  break
                default:
                  this.$router.push('/login')
              }
            } else {
              this.$message.error(response.data.message || '登录失败')
            }
          } catch (error) {
            console.error('登录失败:', error)
            this.$message.error(error.response?.data?.message || '登录失败')
          } finally {
            this.loading = false
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #2d3a4b;
  
  .login-form {
    width: 400px;
    padding: 30px;
    background: white;
    border-radius: 5px;
    
    .title {
      text-align: center;
      margin-bottom: 30px;
      color: #333;
    }
  }
}
</style> 