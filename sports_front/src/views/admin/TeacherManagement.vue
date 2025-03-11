<template>
  <div class="teacher-management">
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm">
        <el-form-item>
          <el-input
            v-model="filterForm.keyword"
            placeholder="搜索姓名"
            clearable
            @clear="handleSearch">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button type="success" @click="handleAdd">添加教师</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="teachers" border v-loading="loading">
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="realName" label="姓名"></el-table-column>
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
        :user-type="'TEACHER'"
        @saved="handleSaved">
      </user-form>
    </el-dialog>
  </div>
</template>

<script>
import UserForm from './UserForm.vue'

export default {
  name: 'TeacherManagement',
  components: {
    UserForm
  },
  data() {
    return {
      teachers: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      filterForm: {
        keyword: ''
      },
      dialogVisible: false,
      dialogTitle: '添加教师',
      currentUser: null
    }
  },
  methods: {
    async fetchTeachers() {
      try {
        this.loading = true;
        console.log('Fetching teachers...'); // 添加调试日志
        const response = await this.$axios.get('/api/users/teachers', {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            keyword: this.filterForm.keyword
          }
        });
        console.log('Response:', response); // 添加调试日志
        if (response.data.code === 200) {
          this.teachers = response.data.data.content;
          this.total = response.data.data.totalElements;
        } else {
          this.$message.error(response.data.msg || '获取教师列表失败');
        }
      } catch (error) {
        console.error('Error details:', error); // 添加详细错误日志
        this.$message.error(error.response?.data?.msg || error.message || '获取教师列表失败');
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchTeachers()
    },
    handlePageChange(page) {
      this.currentPage = page
      this.fetchTeachers()
    },
    handleAdd() {
      this.dialogTitle = '添加教师'
      this.currentUser = null
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑教师'
      this.currentUser = row
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.userForm.setFormData(row)
      })
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确认删除该教师?', '提示', {
          type: 'warning'
        })
        const response = await this.$axios.delete(`/api/users/${row.id}`)
        if (response.data.code === 200) {
          this.$message.success('删除成功')
          this.fetchTeachers()
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
      this.fetchTeachers()
    }
  },
  created() {
    this.fetchTeachers()
  }
}
</script>

<style scoped>
.teacher-management {
  padding: 20px;
}
.filter-section {
  margin-bottom: 20px;
}
</style> 