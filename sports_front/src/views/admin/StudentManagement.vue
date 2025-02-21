<template>
  <div class="student-management">
    <div class="operation-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入学生姓名或用户名"
        style="width: 200px"
        prefix-icon="el-icon-search"
        @keyup.enter.native="handleSearch"
      />
      <el-button type="primary" @click="handleAdd">
        <i class="el-icon-plus"></i> 添加学生
      </el-button>
    </div>

    <el-table
      :data="studentList"
      border
      stripe
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="电话" width="120" />
      <el-table-column label="操作" width="250" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="handleEdit(scope.row)"
          >编辑</el-button>
          <el-button
            size="mini"
            type="warning"
            @click="handleResetPassword(scope.row)"
          >重置密码</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      />
    </div>

    <el-dialog
      :title="dialogType === 'add' ? '添加学生' : '编辑学生'"
      :visible.sync="dialogVisible"
      width="500px"
    >
      <el-form
        :model="studentForm"
        :rules="rules"
        ref="studentForm"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="studentForm.username" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="studentForm.realName" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="studentForm.email" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="studentForm.phone" />
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
  name: 'StudentManagement',
  data() {
    return {
      searchKeyword: '',
      studentList: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      dialogType: 'add', // 'add' or 'edit'
      studentForm: {
        username: '',
        realName: '',
        email: '',
        phone: ''
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
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchStudentList()
  },
  methods: {
    async fetchStudentList() {
      try {
        this.loading = true
        const res = await this.$http.get('/admin/users', {
          params: {
            userType: 'STUDENT',
            keyword: this.searchKeyword,
            page: this.currentPage - 1,
            size: this.pageSize
          }
        })
        if (res.data.code === 200) {
          this.studentList = res.data.data
          this.total = this.studentList.length
        }
      } catch (error) {
        console.error('获取学生列表失败:', error)
        this.$message.error('获取学生列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchStudentList()
    },
    handleAdd() {
      this.dialogType = 'add'
      this.studentForm = {
        username: '',
        realName: '',
        email: '',
        phone: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogType = 'edit'
      this.studentForm = { ...row }
      this.dialogVisible = true
    },
    async handleSubmit() {
      this.$refs.studentForm.validate(async valid => {
        if (valid) {
          try {
            const data = {
              ...this.studentForm,
              userType: 'STUDENT',
              password: '123456' // 设置默认密码
            }
            const url = this.dialogType === 'add' ? '/admin/users/add' : '/admin/users/update'
            const res = await this.$http.post(url, data)
            if (res.data.code === 200) {
              this.$message.success(this.dialogType === 'add' ? '添加成功' : '更新成功')
              this.dialogVisible = false
              this.fetchStudentList()
            } else {
              this.$message.error(res.data.msg || (this.dialogType === 'add' ? '添加失败' : '更新失败'))
            }
          } catch (error) {
            console.error('操作失败:', error)
            this.$message.error(this.dialogType === 'add' ? '添加失败' : '更新失败')
          }
        }
      })
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确认删除该学生吗？', '提示', {
          type: 'warning'
        })
        const res = await this.$http.delete(`/admin/users/${row.id}`)
        if (res.data.code === 200) {
          this.$message.success('删除成功')
          this.fetchStudentList()
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    async handleResetPassword(row) {
      try {
        await this.$confirm('确认重置该学生的密码吗？', '提示', {
          type: 'warning'
        })
        const res = await this.$http.post(`/admin/users/${row.id}/reset-password`)
        if (res.data.code === 200) {
          this.$message.success('密码重置成功')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('密码重置失败')
        }
      }
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchStudentList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchStudentList()
    }
  }
}
</script>

<style scoped>
.student-management {
  padding: 20px;
}
.operation-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}
.pagination {
  margin-top: 20px;
  text-align: right;
}
</style> 