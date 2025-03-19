<template>
  <div class="profile">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>个人信息</span>
        <el-button
          style="float: right; padding: 3px 0"
          type="text"
          @click="handleEdit"
        >
          修改
        </el-button>
      </div>

      <el-form :model="userInfo" label-width="100px" :disabled="!isEditing">
        <el-form-item label="学号">
          <el-input v-model="userInfo.studentNumber" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="userInfo.realName" disabled></el-input>
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="userInfo.className" disabled></el-input>
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input v-model="userInfo.phone"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userInfo.email"></el-input>
        </el-form-item>
        <el-form-item v-if="isEditing">
          <el-button type="primary" @click="handleSave" :loading="saving">
            保存
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="box-card" style="margin-top: 20px">
      <div slot="header" class="clearfix">
        <span>修改密码</span>
      </div>

      <el-form
        :model="passwordForm"
        :rules="passwordRules"
        ref="passwordForm"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="handleChangePassword"
            :loading="changingPassword"
          >
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'StudentProfile',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      userInfo: {},
      isEditing: false,
      saving: false,
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
      },
      changingPassword: false
    }
  },
  created() {
    this.fetchUserInfo()
  },
  methods: {
    async fetchUserInfo() {
      try {
        const response = await this.$http.get('/api/user/info')
        if (response.data.code === 200) {
          this.userInfo = response.data.data
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        this.$message.error('获取用户信息失败')
      }
    },
    handleEdit() {
      this.isEditing = true
    },
    async handleSave() {
      this.saving = true
      try {
        const response = await this.$http.put('/api/user/info', this.userInfo)
        if (response.data.code === 200) {
          this.$message.success('保存成功')
          this.isEditing = false
        }
      } catch (error) {
        console.error('保存用户信息失败:', error)
        this.$message.error('保存用户信息失败')
      } finally {
        this.saving = false
      }
    },
    handleCancel() {
      this.isEditing = false
      this.fetchUserInfo()
    },
    async handleChangePassword() {
      this.$refs.passwordForm.validate(async valid => {
        if (valid) {
          this.changingPassword = true
          try {
            const response = await this.$http.put('/api/user/password', {
              oldPassword: this.passwordForm.oldPassword,
              newPassword: this.passwordForm.newPassword
            })
            if (response.data.code === 200) {
              this.$message.success('密码修改成功，请重新登录')
              this.$refs.passwordForm.resetFields()
              setTimeout(() => {
                this.$router.push('/login')
              }, 1500)
            }
          } catch (error) {
            console.error('修改密码失败:', error)
            this.$message.error('修改密码失败')
          } finally {
            this.changingPassword = false
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.profile {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}
</style> 