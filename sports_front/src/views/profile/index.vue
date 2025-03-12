<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>个人信息</span>
      </div>
      
      <el-form ref="form" :model="userInfo" label-width="120px">
        <el-form-item label="用户名">
          <el-input v-model="userInfo.username" disabled></el-input>
        </el-form-item>
        
        <el-form-item label="真实姓名">
          <el-input v-model="userInfo.realName"></el-input>
        </el-form-item>
        
        <el-form-item label="邮箱">
          <el-input v-model="userInfo.email"></el-input>
        </el-form-item>
        
        <el-form-item label="手机号">
          <el-input v-model="userInfo.phone"></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="updateProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
      
      <el-divider></el-divider>
      
      <div class="password-section">
        <h3>修改密码</h3>
        <el-form ref="passwordForm" :model="passwordForm" label-width="120px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password"></el-input>
          </el-form-item>
          
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password"></el-input>
          </el-form-item>
          
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password"></el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="changePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getUserInfo, updateUserInfo, changePassword } from '@/api/user'

export default {
  name: 'Profile',
  data() {
    return {
      userInfo: {
        username: '',
        realName: '',
        email: '',
        phone: ''
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
  },
  created() {
    this.fetchUserInfo()
  },
  methods: {
    async fetchUserInfo() {
      try {
        const { data } = await getUserInfo()
        this.userInfo = data
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },
    async updateProfile() {
      try {
        await updateUserInfo(this.userInfo)
        this.$message.success('个人信息更新成功')
      } catch (error) {
        this.$message.error('更新失败：' + error.message)
      }
    },
    async changePassword() {
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.$message.error('两次输入的新密码不一致')
        return
      }
      
      try {
        await changePassword({
          oldPassword: this.passwordForm.oldPassword,
          newPassword: this.passwordForm.newPassword
        })
        this.$message.success('密码修改成功')
        this.passwordForm = {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
      } catch (error) {
        this.$message.error('密码修改失败：' + error.message)
      }
    }
  }
}
</script>

<style scoped>
.password-section {
  margin-top: 20px;
}
</style> 