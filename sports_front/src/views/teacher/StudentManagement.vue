<template>
  <div class="student-management">
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm">
        <el-form-item>
          <el-input
            v-model="filterForm.keyword"
            placeholder="搜索姓名/学号"
            clearable
            @clear="handleSearch">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button type="success" @click="handleAdd">添加学生</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="students" border v-loading="loading">
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="realName" label="姓名"></el-table-column>
      <el-table-column prop="studentNumber" label="学号"></el-table-column>
      <el-table-column prop="className" label="班级"></el-table-column>
      <el-table-column prop="email" label="邮箱"></el-table-column>
      <el-table-column prop="phone" label="电话"></el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      @current-change="handlePageChange"
      :current-page.sync="currentPage"
      :page-size="pageSize"
      layout="total, prev, pager, next"
      :total="total">
    </el-pagination>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible">
      <user-form
        ref="userForm"
        :user-type="'STUDENT'"
        @saved="handleSaved">
      </user-form>
    </el-dialog>
  </div>
</template>

<script>
import UserForm from '../admin/UserForm.vue'

export default {
  name: 'TeacherStudentManagement',
  components: {
    UserForm
  },
  data() {
    return {
      students: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      filterForm: {
        keyword: ''
      },
      dialogVisible: false,
      dialogTitle: '添加学生',
      currentUser: null
    }
  },
  methods: {
    async fetchStudents() {
      try {
        this.loading = true;
        const response = await this.$axios.get('/api/teacher/students', {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            keyword: this.filterForm.keyword
          }
        });
        if (response.data.code === 200) {
          this.students = response.data.data.content;
          this.total = response.data.data.totalElements;
        } else {
          this.$message.error(response.data.msg || '获取学生列表失败');
        }
      } catch (error) {
        this.$message.error(error.response?.data?.msg || error.message || '获取学生列表失败');
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchStudents()
    },
    handlePageChange(page) {
      this.currentPage = page
      this.fetchStudents()
    },
    handleAdd() {
      this.dialogTitle = '添加学生'
      this.currentUser = null
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑学生'
      this.currentUser = row
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.userForm.setFormData(row)
      })
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确认删除该学生?', '提示', {
          type: 'warning'
        })
        const response = await this.$axios.delete(`/api/teacher/students/${row.id}`)
        if (response.data.code === 200) {
          this.$message.success('删除成功')
          this.fetchStudents()
        } else {
          this.$message.error(response.data.msg || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败：' + error.message)
        }
      }
    },
    handleSaved() {
      this.dialogVisible = false
      this.fetchStudents()
    }
  },
  created() {
    this.fetchStudents()
  }
}
</script>

<style scoped>
.student-management {
  padding: 20px;
}
.filter-section {
  margin-bottom: 20px;
}
</style>