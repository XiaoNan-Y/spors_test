<template>
  <div class="profile">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="profile-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-user"></i> 个人信息</span>
          </div>
          <el-form :model="userForm" :rules="rules" ref="userForm" label-width="100px">
            <el-form-item label="学号" prop="studentNumber">
              <el-input v-model="userForm.studentNumber" disabled></el-input>
            </el-form-item>
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="userForm.realName" disabled></el-input>
            </el-form-item>
            <el-form-item label="班级" prop="className">
              <el-input v-model="userForm.className" disabled></el-input>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userForm.phone" placeholder="请输入手机号"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="password-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-lock"></i> 修改密码</span>
          </div>
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input type="password" v-model="passwordForm.oldPassword" show-password placeholder="请输入原密码"></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input type="password" v-model="passwordForm.newPassword" show-password placeholder="请输入新密码"></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input type="password" v-model="passwordForm.confirmPassword" show-password placeholder="请确认新密码"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updatePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'StudentProfile',
  data() {
    // 密码确认验证
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    return {
      userForm: {
        studentNumber: '',
        realName: '',
        className: '',
        email: '',
        phone: ''
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ]
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchUserInfo()
  },
  methods: {
    async fetchUserInfo() {
      try {
        const res = await this.$http.get('/api/student/info')
        if (res.data.code === 200) {
          const { real_name, student_number, class_name, email, phone } = res.data.data
          this.userForm.realName = real_name
          this.userForm.studentNumber = student_number
          this.userForm.className = class_name
          this.userForm.email = email || ''
          this.userForm.phone = phone || ''
        }
      } catch (error) {
        this.$message.error('获取用户信息失败')
        console.error(error)
      }
    },
    async updateProfile() {
      try {
        await this.$refs.userForm.validate()
        const res = await this.$http.put('/api/student/profile', {
          email: this.userForm.email,
          phone: this.userForm.phone
        })
        if (res.data.code === 200) {
          this.$message.success('个人信息更新成功')
        }
      } catch (error) {
        this.$message.error(error.message || '更新失败')
      }
    },
    async updatePassword() {
      try {
        await this.$refs.passwordForm.validate()
        
        // 添加loading
        const loading = this.$loading({
          lock: true,
          text: '正在修改密码...',
          spinner: 'el-icon-loading'
        })
        
        try {
          const res = await this.$http.put('/api/student/password', {
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword
          })
          
          if (res.data.code === 200) {
            this.$message.success('密码修改成功，请重新登录')
            // 清空表单
            this.$refs.passwordForm.resetFields()
            // 延迟跳转，让用户看到成功提示
            setTimeout(() => {
              localStorage.clear()
              this.$router.push('/login')
            }, 1500)
          } else {
            this.$message.error(res.data.message || '修改密码失败')
          }
        } finally {
          loading.close()
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        this.$message.error(error.response?.data?.message || '修改密码失败，请重试')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.profile {
  padding: 20px;
  
  .card-header {
    display: flex;
    align-items: center;
    
    i {
      margin-right: 8px;
      font-size: 18px;
    }
    
    span {
      font-size: 16px;
      font-weight: 500;
    }
  }
  
  .profile-card, .password-card {
    margin-bottom: 20px;
    
    .el-form {
      padding: 20px;
    }
  }
  
  .el-input.is-disabled .el-input__inner {
    color: #606266;
    background-color: #f5f7fa;
  }
}
</style> 