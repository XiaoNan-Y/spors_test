<template>
  <div class="teacher-management">
    <div class="operation-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="输入用户名或姓名搜索"
        style="width: 200px"
        @keyup.enter.native="handleSearch"
      >
        <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
      </el-input>
      
      <el-button type="primary" @click="handleAdd">添加教师</el-button>
    </div>

    <el-table :data="teacherList" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="realName" label="姓名"></el-table-column>
      <el-table-column prop="email" label="邮箱"></el-table-column>
      <el-table-column prop="phone" label="手机号"></el-table-column>
      <el-table-column label="操作" width="250">
        <template slot-scope="scope">
          <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="mini" type="warning" @click="handleResetPassword(scope.row)">重置密码</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="form.id"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TeacherManagement',
  data() {
    return {
      searchKeyword: '',
      teacherList: [],
      loading: false,
      dialogVisible: false,
      dialogTitle: '',
      form: {
        id: null,
        username: '',
        realName: '',
        email: '',
        phone: '',
        userType: 'TEACHER'
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入姓名', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchTeacherList()
  },
  methods: {
    async fetchTeacherList() {
      try {
        this.loading = true
        const res = await this.$http.get('/admin/users', {
          params: {
            userType: 'TEACHER',
            keyword: this.searchKeyword
          }
        })
        if (res.data.code === 200) {
          this.teacherList = res.data.data
        } else {
          this.$message.error(res.data.msg || '获取教师列表失败')
        }
      } catch (error) {
        console.error('获取教师列表失败:', error)
        this.$message.error('获取教师列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.fetchTeacherList()
    },
    handleAdd() {
      this.dialogTitle = '添加教师'
      this.form = {
        id: null,
        username: '',
        realName: '',
        email: '',
        phone: '',
        userType: 'TEACHER'
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑教师'
      this.form = { ...row }
      this.dialogVisible = true
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          try {
            const url = this.form.id ? '/admin/users/update' : '/admin/users/add'
            const res = await this.$http[this.form.id ? 'put' : 'post'](url, this.form)
            if (res.data.code === 200) {
              this.$message.success(this.form.id ? '更新成功' : '添加成功')
              this.dialogVisible = false
              this.fetchTeacherList()
            } else {
              this.$message.error(res.data.msg || '操作失败')
            }
          } catch (error) {
            console.error('操作失败:', error)
            this.$message.error('操作失败，请检查网络连接或联系管理员')
          }
        }
      })
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确认删除该教师吗？', '提示', {
          type: 'warning'
        })
        const res = await this.$http.delete(`/api/admin/users/${row.id}`)
        if (res.data.code === 200) {
          this.$message.success('删除成功')
          this.fetchTeacherList()
        } else {
          this.$message.error(res.data.msg || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    async handleResetPassword(row) {
      try {
        await this.$confirm('确认重置该教师的密码吗？重置后密码为：123456', '提示', {
          type: 'warning'
        })
        const res = await this.$http.put(`/api/admin/users/reset-password/${row.id}`)
        if (res.data.code === 200) {
          this.$message.success('密码重置成功')
        } else {
          this.$message.error(res.data.msg || '密码重置失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('重置密码失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.teacher-management {
  padding: 20px;
}
.operation-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}
</style> 