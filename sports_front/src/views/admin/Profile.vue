<template>
  <div class="profile">
    <el-card class="profile-card">
      <div slot="header">
        <span>个人信息</span>
        <el-button 
          style="float: right; padding: 3px 0" 
          type="text"
          @click="handleEdit"
        >
          修改
        </el-button>
      </div>
      
      <el-form label-width="80px" :model="userInfo" :disabled="!isEditing">
        <el-form-item label="用户名">
          <el-input v-model="userInfo.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="userInfo.realName"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userInfo.email"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userInfo.phone"></el-input>
        </el-form-item>
      </el-form>

      <div class="button-group" v-if="isEditing">
        <el-button @click="isEditing = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </el-card>

    <el-card class="password-card">
      <div slot="header">
        <span>修改密码</span>
      </div>
      
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px">
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
          <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'Profile',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      isEditing: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' }
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
  computed: {
    ...mapState({
      userInfo: state => state.user
    })
  },
  methods: {
    handleEdit() {
      this.isEditing = true
    },
    async handleSave() {
      try {
        const res = await this.$http.put('/api/admin/users/update', this.userInfo)
        if (res.data.code === 200) {
          this.$message.success('保存成功')
          this.isEditing = false
          // 更新vuex中的用户信息
          this.$store.dispatch('setUser', this.userInfo)
        } else {
          this.$message.error(res.data.msg)
        }
      } catch (error) {
        this.$message.error('保存失败')
      }
    },
    async handleChangePassword() {
      this.$refs.passwordForm.validate(async valid => {
        if (valid) {
          try {
            const changePasswordDTO = {
              userId: this.userInfo.id,
              oldPassword: this.passwordForm.oldPassword,
              newPassword: this.passwordForm.newPassword
            };
            
            const res = await this.$http.put('/api/auth/change-password', changePasswordDTO);
            if (res.data.code === 200) {
              this.$message.success('密码修改成功，请重新登录');
              this.$store.dispatch('clearUser');
              this.$router.push('/login');
            } else {
              this.$message.error(res.data.msg);
            }
          } catch (error) {
            this.$message.error('修改密码失败');
          }
        }
      });
    }
  }
}
</script>

<style scoped>
.profile {
  padding: 20px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.profile-card, .password-card {
  min-height: 400px;
}
.button-group {
  text-align: right;
  margin-top: 20px;
}
</style> 