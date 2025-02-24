<template>
  <div class="notice-management">
    <div class="operation-bar">
      <div class="search-area">
        <el-input
          v-model="searchKeyword"
          placeholder="输入标题搜索"
          style="width: 200px"
          @keyup.enter.native="handleSearch"
        >
          <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
        </el-input>
        <el-select v-model="searchType" placeholder="通知类型" style="width: 120px; margin-left: 10px">
          <el-option label="全部" value=""></el-option>
          <el-option label="体测安排" value="TEST_SCHEDULE"></el-option>
          <el-option label="成绩公布" value="SCORE_RELEASE"></el-option>
          <el-option label="系统维护" value="SYSTEM_MAINTENANCE"></el-option>
          <el-option label="其他通知" value="OTHER"></el-option>
        </el-select>
      </div>
      
      <el-button type="primary" @click="handleAdd">
        <i class="el-icon-plus"></i> 发布通知
      </el-button>
    </div>

    <el-table
      :data="noticeList"
      border
      stripe
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="type" label="类型" width="100">
        <template slot-scope="scope">
          <el-tag :type="getTypeTagType(scope.row.type)">
            {{ getTypeLabel(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" show-overflow-tooltip>
        <template slot-scope="scope">
          <span :class="{'important-notice': scope.row.priority === 'HIGH'}">
            {{ scope.row.title }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
            active-color="#13ce66"
            inactive-color="#ff4949"
          >
          </el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="handleView(scope.row)"
          >查看</el-button>
          <el-button
            size="mini"
            type="warning"
            @click="handleEdit(scope.row)"
          >编辑</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page.current"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="page.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="page.total"
      >
      </el-pagination>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px">
      <el-form :model="form" :rules="rules" ref="form" label-width="80px">
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择通知类型" style="width: 100%">
            <el-option label="体测安排" value="TEST_SCHEDULE"></el-option>
            <el-option label="成绩公布" value="SCORE_RELEASE"></el-option>
            <el-option label="系统维护" value="SYSTEM_MAINTENANCE"></el-option>
            <el-option label="其他通知" value="OTHER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="重要程度" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择重要程度" style="width: 100%">
            <el-option label="普通" value="NORMAL"></el-option>
            <el-option label="重要" value="HIGH"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            type="textarea"
            v-model="form.content"
            :rows="10"
            maxlength="2000"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          >
          </el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog title="查看通知" :visible.sync="viewDialogVisible" width="700px">
      <div class="notice-header">
        <el-tag :type="getTypeTagType(currentNotice.type)" class="notice-type">
          {{ getTypeLabel(currentNotice.type) }}
        </el-tag>
        <h3 class="notice-title">{{ currentNotice.title }}</h3>
      </div>
      <div class="notice-meta">
        <span>发布时间：{{ formatDate(currentNotice.createTime) }}</span>
        <span v-if="currentNotice.priority === 'HIGH'" class="notice-priority">
          <i class="el-icon-warning"></i> 重要通知
        </span>
      </div>
      <div class="notice-content">{{ currentNotice.content }}</div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'NoticeManagement',
  data() {
    return {
      searchKeyword: '',
      searchType: '',
      noticeList: [],
      loading: false,
      dialogVisible: false,
      viewDialogVisible: false,
      dialogTitle: '',
      currentNotice: {},
      page: {
        current: 1,
        size: 10,
        total: 0
      },
      form: {
        id: null,
        type: 'OTHER',
        priority: 'NORMAL',
        title: '',
        content: '',
        status: 1
      },
      rules: {
        type: [
          { required: true, message: '请选择通知类型', trigger: 'change' }
        ],
        priority: [
          { required: true, message: '请选择重要程度', trigger: 'change' }
        ],
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' },
          { max: 100, message: '长度不能超过100个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入内容', trigger: 'blur' },
          { max: 2000, message: '长度不能超过2000个字符', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchNoticeList()
  },
  methods: {
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString()
    },
    getTypeTagType(type) {
      const typeMap = {
        'TEST_SCHEDULE': 'primary',
        'SCORE_RELEASE': 'success',
        'SYSTEM_MAINTENANCE': 'warning',
        'OTHER': 'info'
      }
      return typeMap[type] || 'info'
    },
    getTypeLabel(type) {
      const typeMap = {
        'TEST_SCHEDULE': '体测安排',
        'SCORE_RELEASE': '成绩公布',
        'SYSTEM_MAINTENANCE': '系统维护',
        'OTHER': '其他通知'
      }
      return typeMap[type] || '其他通知'
    },
    async fetchNoticeList() {
      try {
        this.loading = true
        const res = await this.$http.get('/admin/notices', {
          params: {
            page: this.page.current,
            size: this.page.size,
            keyword: this.searchKeyword,
            type: this.searchType
          }
        })
        if (res.data.code === 200) {
          this.noticeList = res.data.data.records
          this.page.total = res.data.data.total
        } else {
          this.$message.error(res.data.msg || '获取通知列表失败')
        }
      } catch (error) {
        console.error('获取通知列表失败:', error)
        this.$message.error('获取通知列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.page.current = 1
      this.fetchNoticeList()
    },
    handleSizeChange(val) {
      this.page.size = val
      this.fetchNoticeList()
    },
    handleCurrentChange(val) {
      this.page.current = val
      this.fetchNoticeList()
    },
    handleAdd() {
      this.dialogTitle = '发布通知'
      this.form = {
        id: null,
        type: 'OTHER',
        priority: 'NORMAL',
        title: '',
        content: '',
        status: 1
      }
      this.dialogVisible = true
    },
    handleView(row) {
      this.currentNotice = { ...row }
      this.viewDialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑通知'
      this.form = { ...row }
      this.dialogVisible = true
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          try {
            const url = this.form.id ? '/admin/notices/update' : '/admin/notices/add'
            const res = await this.$http[this.form.id ? 'put' : 'post'](url, this.form)
            if (res.data.code === 200) {
              this.$message.success(this.form.id ? '更新成功' : '发布成功')
              this.dialogVisible = false
              this.fetchNoticeList()
            } else {
              this.$message.error(res.data.msg || '操作失败')
            }
          } catch (error) {
            console.error('操作失败:', error)
            this.$message.error('操作失败')
          }
        }
      })
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确认删除该通知吗？', '提示', {
          type: 'warning'
        })
        const res = await this.$http.delete(`/admin/notices/${row.id}`)
        if (res.data.code === 200) {
          this.$message.success('删除成功')
          this.fetchNoticeList()
        } else {
          this.$message.error(res.data.msg || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    async handleStatusChange(row) {
      try {
        const res = await this.$http.put(`/admin/notices/${row.id}/status`, {
          status: row.status
        })
        if (res.data.code === 200) {
          this.$message.success(`${row.status === 1 ? '启用' : '禁用'}成功`)
        } else {
          row.status = row.status === 1 ? 0 : 1 // 恢复状态
          this.$message.error(res.data.msg || '操作失败')
        }
      } catch (error) {
        row.status = row.status === 1 ? 0 : 1 // 恢复状态
        this.$message.error('操作失败')
      }
    }
  }
}
</script>

<style scoped>
.notice-management {
  padding: 20px;
}
.operation-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}
.search-area {
  display: flex;
  align-items: center;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
.notice-header {
  text-align: center;
  margin-bottom: 20px;
}
.notice-type {
  margin-bottom: 10px;
}
.notice-title {
  margin: 10px 0;
  font-size: 20px;
}
.notice-meta {
  color: #666;
  margin-bottom: 20px;
  text-align: center;
  display: flex;
  justify-content: center;
  gap: 20px;
}
.notice-priority {
  color: #E6A23C;
}
.notice-content {
  line-height: 1.8;
  white-space: pre-wrap;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}
.important-notice {
  color: #E6A23C;
  font-weight: bold;
}
.notice-priority i {
  margin-right: 5px;
}
</style> 