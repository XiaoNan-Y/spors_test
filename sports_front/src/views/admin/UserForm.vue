<template>
  <div class="user-form">
    <el-form :model="form" :rules="rules" ref="form" label-width="100px">
      <el-form-item label="用户名" prop="username" v-if="!isEdit">
        <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
      </el-form-item>
      
      <el-form-item label="用户类型" prop="userType">
        <el-select v-model="form.userType" placeholder="请选择用户类型">
          <el-option label="学生" value="STUDENT"></el-option>
          <el-option label="教师" value="TEACHER"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="姓名" prop="realName">
        <el-input v-model="form.realName" placeholder="请输入姓名"></el-input>
      </el-form-item>

      <!-- 只在用户类型为学生时显示学号输入框 -->
      <el-form-item v-if="userType === 'STUDENT'" label="学号" prop="studentNumber">
        <el-input v-model="form.studentNumber" placeholder="请输入学号"></el-input>
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
      </el-form-item>

      <el-form-item label="电话" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入电话"></el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm">保存</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'UserForm',
  props: {
    userType: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      isEdit: false,
      form: {
        id: null,
        username: '',
        userType: '',
        realName: '',
        studentNumber: '',
        email: '',
        phone: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        userType: [
          { required: true, message: '请选择用户类型', trigger: 'change' }
        ],
        realName: [
          { required: true, message: '请输入姓名', trigger: 'blur' }
        ],
        studentNumber: [
          { required: true, message: '请输入学号', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    setFormData(user) {
      this.isEdit = true
      this.form = { ...user }
    },
    submitForm() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            const url = this.isEdit ? 
              `/api/users/${this.form.id}` : 
              '/api/users'
            const method = this.isEdit ? 'put' : 'post'
            
            this.form.userType = this.userType
            const response = await this.$axios[method](url, this.form)
            
            if (response.data.code === 200) {
              this.$message.success('保存成功')
              this.$emit('saved')
              this.resetForm()
            } else {
              this.$message.error(response.data.msg || '保存失败')
            }
          } catch (error) {
            this.$message.error('保存失败：' + error.message)
          }
        }
      })
    },
    resetForm() {
      this.$refs.form.resetFields()
      this.isEdit = false
      this.form = {
        id: null,
        username: '',
        userType: '',
        realName: '',
        studentNumber: '',
        email: '',
        phone: ''
      }
    }
  },
  watch: {
    'form.userType'(newVal) {
      // 当用户类型改变时，如果不是学生，清空学号
      if (newVal !== 'STUDENT') {
        this.form.studentNumber = '';
      }
    }
  }
}
</script>

<style scoped>
.user-form {
  max-width: 600px;
  margin: 20px auto;
}
</style> 