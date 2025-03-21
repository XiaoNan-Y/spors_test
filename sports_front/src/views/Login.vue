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
    async handleLogin() {
      try {
        const response = await this.$axios.post('/api/users/login', this.loginForm)
        if (response.data.code === 200) {
          const user = response.data.data
          console.log('登录成功，用户信息：', user)
          
          // 保存用户信息
          localStorage.setItem('token', user.token)
          localStorage.setItem('username', user.username)
          localStorage.setItem('userRole', user.userType)
          localStorage.setItem('userId', user.id.toString())
          
          console.log('保存到localStorage的信息:', {
            token: localStorage.getItem('token'),
            username: localStorage.getItem('username'),
            userRole: localStorage.getItem('userRole'),
            userId: localStorage.getItem('userId')
          })
          
          // 等待一下确保数据已保存
          await this.$nextTick()
          
          // 根据用户角色重定向到不同的首页
          switch (user.userType) {
            case 'STUDENT':
              await this.$router.push('/student/home')
              break
            case 'TEACHER':
              await this.$router.push('/teacher/dashboard')
              break
            case 'ADMIN':
              await this.$router.push('/admin/dashboard')
              break
            default:
              await this.$router.push('/login')
          }
          
          this.$message.success('登录成功')
        } else {
          this.$message.error(response.data.message || '登录失败')
        }
      } catch (error) {
        console.error('登录失败:', error)
        this.$message.error('登录失败')
      }
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