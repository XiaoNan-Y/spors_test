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

    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
    ></el-pagination>

    <!-- 添加/编辑教师对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="teacherForm" :rules="rules" ref="teacherForm" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="teacherForm.username" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="teacherForm.realName"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="teacherForm.email"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="teacherForm.phone"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
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
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      isEdit: false,
      teacherForm: {
        id: null,
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
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑教师' : '添加教师'
    }
  },
  created() {
    this.fetchTeacherList()
  },
  methods: {
    async fetchTeacherList() {
      try {
        this.loading = true
        const res = await this.$http.get('/api/admin/users', {
          params: {
            userType: 'TEACHER',
            keyword: this.searchKeyword,
            page: this.currentPage - 1,
            size: this.pageSize
          }
        })
        if (res.data.code === 200) {
          if (Array.isArray(res.data.data)) {
            this.teacherList = res.data.data
            this.total = res.data.data.length
          } else if (res.data.data.content) {
            this.teacherList = res.data.data.content
            this.total = res.data.data.totalElements
          } else {
            this.teacherList = res.data.data
            this.total = res.data.data.length
          }
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
      this.currentPage = 1
      this.fetchTeacherList()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchTeacherList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchTeacherList()
    },
    handleAdd() {
      this.isEdit = false
      this.teacherForm = {
        id: null,
        username: '',
        realName: '',
        email: '',
        phone: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEdit = true
      this.teacherForm = { ...row }
      this.dialogVisible = true
    },
    async handleSubmit() {
      this.$refs.teacherForm.validate(async valid => {
        if (valid) {
          try {
            const url = this.isEdit ? `/api/admin/users/${this.teacherForm.id}` : '/api/admin/users'
            const method = this.isEdit ? 'put' : 'post'
            const res = await this.$http[method](url, {
              ...this.teacherForm,
              userType: 'TEACHER'
            })
            if (res.data.code === 200) {
              this.$message.success(this.isEdit ? '更新成功' : '添加成功')
              this.dialogVisible = false
              this.fetchTeacherList()
            } else {
              this.$message.error(res.data.msg || '操作失败')
            }
          } catch (error) {
            console.error('操作失败:', error)
            this.$message.error(error.response?.data?.msg || '操作失败')
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
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    async handleResetPassword(row) {
      try {
        await this.$confirm('确认重置该教师的密码吗？', '提示', {
          type: 'warning'
        })
        const res = await this.$http.put(`/api/admin/users/${row.id}/reset-password`)
        if (res.data.code === 200) {
          this.$message.success('密码重置成功')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('密码重置失败')
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
.pagination {
  margin-top: 20px;
  text-align: right;
}
</style> 