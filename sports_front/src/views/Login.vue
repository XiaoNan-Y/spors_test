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
      loginRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      loading: false
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (valid) {
          try {
            this.loading = true
            const response = await this.$http.post('/api/users/login', this.loginForm)
            
            if (response.data.code === 200) {
              localStorage.setItem('user', JSON.stringify(response.data.data))
              localStorage.setItem('token', response.data.data.token)
              
              const userType = response.data.data.userType
              if (userType === 'ADMIN') {
                this.$router.push('/admin/dashboard')
              } else if (userType === 'TEACHER') {
                this.$router.push('/teacher/dashboard')
              } else if (userType === 'STUDENT') {
                this.$router.push('/student/dashboard')
              }
            } else {
              this.$message.error(response.data.msg || '登录失败')
            }
          } catch (error) {
            console.error('Login error:', error)
            this.$message.error(error.message || '登录失败')
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