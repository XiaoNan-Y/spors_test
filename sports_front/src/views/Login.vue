<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="title">体育测试管理系统</div>
      <el-form :model="loginForm" :rules="rules" ref="loginForm" label-width="0">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            prefix-icon="el-icon-user"
            placeholder="用户名"
          ></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            prefix-icon="el-icon-lock"
            type="password"
            placeholder="密码"
            @keyup.enter.native="handleLogin"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
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
      rules: {
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
            const res = await this.$http.post('/user/login', this.loginForm)
            if (res.data.code === 200) {
              // 保存用户信息到vuex
              this.$store.dispatch('setUser', res.data.data)
              this.$message.success('登录成功')
              
              // 根据用户类型跳转到不同的页面
              const userType = res.data.data.userType
              switch (userType) {
                case 'ADMIN':
                  this.$router.push('/admin/dashboard')
                  break
                case 'TEACHER':
                  this.$router.push('/teacher/dashboard')
                  break
                case 'STUDENT':
                  this.$router.push('/student/dashboard')
                  break
                default:
                  this.$message.error('未知的用户类型')
              }
            } else {
              this.$message.error(res.data.msg)
            }
          } catch (error) {
            console.error('Login error:', error)
            this.$message.error('登录失败，请检查网络连接')
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
}
.title {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #409EFF;
}
</style> 