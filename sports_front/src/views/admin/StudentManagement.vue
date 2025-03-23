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

    <!-- 添加导入按钮和上传组件 -->
    <el-upload
      class="upload-demo"
      action="/api/admin/users/student/import"
      :headers="headers"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :before-upload="beforeUpload">
      <el-button type="primary">导入学生</el-button>
      <div slot="tip" class="el-upload__tip">只能上传xlsx文件</div>
    </el-upload>
  </div>
</template>

<script>
import UserForm from './UserForm.vue'

export default {
  name: 'StudentManagement',
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
      currentUser: null,
      headers: {
        'X-Token': localStorage.getItem('token')
      }
    }
  },
  methods: {
    async fetchStudents() {
      try {
        this.loading = true;
        console.log('Fetching students...'); // 添加调试日志
        const response = await this.$axios.get('/api/users/students', {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            keyword: this.filterForm.keyword
          }
        });
        console.log('Response:', response); // 添加调试日志
        if (response.data.code === 200) {
          this.students = response.data.data.content;
          this.total = response.data.data.totalElements;
        } else {
          this.$message.error(response.data.msg || '获取学生列表失败');
        }
      } catch (error) {
        console.error('Error details:', error); // 添加详细错误日志
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
        const response = await this.$axios.delete(`/api/users/${row.id}`)
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
    },
    beforeUpload(file) {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
      if (!isExcel) {
        this.$message.error('只能上传Excel文件!');
        return false;
      }
      return true;
    },
    handleUploadSuccess(response) {
      if (response.code === 200) {
        this.$message.success('导入成功');
        this.fetchStudents(); // 刷新列表
      } else {
        this.$message.error(response.message);
      }
    },
    handleUploadError() {
      this.$message.error('导入失败');
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