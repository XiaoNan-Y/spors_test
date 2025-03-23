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

    <!-- Excel导入部分 -->
    <el-dialog title="导入教师" :visible.sync="importDialogVisible" width="500px">
      <el-upload
        class="upload-demo"
        drag
        action="/api/admin/users/teacher/import"
        :headers="headers"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        :limit="1"
        accept=".xlsx">
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip" slot="tip">
          <p>只能上传xlsx文件，且文件大小不超过10MB</p>
          <p>请按模板格式填写数据，<a href="javascript:;" @click="downloadTemplate">下载模板</a></p>
        </div>
      </el-upload>
      <div class="import-result" v-if="importResult.total">
        <p>导入结果：</p>
        <p>总数：{{ importResult.total }}</p>
        <p>成功：{{ importResult.success }}</p>
        <p>失败：{{ importResult.failed }}</p>
        <div v-if="importResult.errors.length">
          <p>错误信息：</p>
          <ul>
            <li v-for="(error, index) in importResult.errors" :key="index">{{ error }}</li>
          </ul>
        </div>
      </div>
    </el-dialog>

    <!-- 批量导入入口 -->
    <div class="batch-import">
      <el-button type="primary" @click="importDialogVisible = true">
        <i class="el-icon-upload2"></i> 批量导入
      </el-button>
    </div>
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
      importDialogVisible: false,
      dialogTitle: '添加教师',
      currentUser: null,
      headers: {
        'X-Token': localStorage.getItem('token')
      },
      importResult: {
        total: 0,
        success: 0,
        failed: 0,
        errors: []
      }
    }
  },
  methods: {
    async fetchTeachers() {
      try {
        this.loading = true
        const response = await this.$axios.get('/api/users/teachers', {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            keyword: this.filterForm.keyword
          }
        })
        if (response.data.code === 200) {
          this.teachers = response.data.data.content
          this.total = response.data.data.totalElements
        } else {
          this.$message.error(response.data.msg || '获取教师列表失败')
        }
      } catch (error) {
        this.$message.error(error.response?.data?.msg || error.message || '获取教师列表失败')
      } finally {
        this.loading = false
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
    },
    beforeUpload(file) {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      const isLt10M = file.size / 1024 / 1024 < 10

      if (!isExcel) {
        this.$message.error('只能上传 xlsx 格式的文件!')
        return false
      }
      if (!isLt10M) {
        this.$message.error('文件大小不能超过 10MB!')
        return false
      }
      return true
    },
    handleUploadSuccess(response) {
      if (response.code === 200) {
        this.importResult = {
          total: response.data.length,
          success: response.data.length,
          failed: 0,
          errors: []
        }
        this.$message.success('导入成功')
        this.fetchTeachers()
      } else {
        this.importResult = {
          total: 0,
          success: 0,
          failed: 1,
          errors: [response.message]
        }
        this.$message.error(response.message)
      }
    },
    handleUploadError(err) {
      this.importResult = {
        total: 0,
        success: 0,
        failed: 1,
        errors: [err.message || '导入失败']
      }
      this.$message.error('导入失败')
    },
    downloadTemplate() {
      // 创建一个链接来下载模板
      const link = document.createElement('a')
      link.href = '/templates/teacher_import_template.xlsx' // 这个路径需要后端提供
      link.download = '教师导入模板.xlsx'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
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
.batch-import {
  margin-top: 20px;
}
.import-result {
  margin-top: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
.el-upload__tip {
  line-height: 1.5;
}
</style> 