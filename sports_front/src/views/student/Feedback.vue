<template>
  <div class="feedback">
    <!-- 顶部说明卡片 -->
    <el-card class="info-card">
      <div class="info-content">
        <i class="el-icon-chat-line-round"></i>
        <div class="info-text">
          <h3>意见反馈</h3>
          <p>欢迎提供您的宝贵意见，帮助我们改进系统。我们会认真对待每一条反馈。</p>
        </div>
      </div>
    </el-card>

    <!-- 操作栏 -->
    <div class="operation-bar">
      <div class="filter-area">
        <el-select v-model="filterType" placeholder="反馈类型" clearable>
          <el-option label="全部" value=""></el-option>
          <el-option label="建议" value="SUGGESTION"></el-option>
          <el-option label="问题反馈" value="BUG"></el-option>
          <el-option label="其他" value="OTHER"></el-option>
        </el-select>
      </div>
      <el-button type="primary" @click="handleAdd">
        提交反馈
      </el-button>
    </div>

    <!-- 反馈列表 -->
    <el-table :data="feedbackList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="createTime" label="提交时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="100">
        <template slot-scope="scope">
          <el-tag :type="getTypeTagType(scope.row.type)">
            {{ getTypeLabel(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template slot-scope="scope">
          <el-button type="text" @click="handleView(scope.row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="[10, 20, 50]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total">
    </el-pagination>

    <!-- 提交反馈对话框 -->
    <el-dialog
      title="提交反馈"
      :visible.sync="dialogVisible"
      width="600px"
      :destroy-on-close="true"
    >
      <el-form :model="form" :rules="rules" ref="form" label-width="80px">
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择反馈类型" style="width: 100%">
            <el-option label="建议" value="SUGGESTION"></el-option>
            <el-option label="问题反馈" value="BUG"></el-option>
            <el-option label="其他" value="OTHER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题"></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            type="textarea"
            v-model="form.content"
            :rows="6"
            placeholder="请详细描述您的反馈内容"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitFeedback" :loading="submitting">
          提 交
        </el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      title="反馈详情"
      :visible.sync="detailVisible"
      width="600px"
    >
      <div v-if="currentFeedback" class="feedback-detail">
        <div class="detail-item">
          <label>反馈类型：</label>
          <el-tag :type="getTypeTagType(currentFeedback.type)">
            {{ getTypeLabel(currentFeedback.type) }}
          </el-tag>
        </div>
        <div class="detail-item">
          <label>提交时间：</label>
          <span>{{ formatDateTime(currentFeedback.createTime) }}</span>
        </div>
        <div class="detail-item">
          <label>标题：</label>
          <span>{{ currentFeedback.title }}</span>
        </div>
        <div class="detail-item">
          <label>内容：</label>
          <p class="content">{{ currentFeedback.content }}</p>
        </div>
        <div class="detail-item" v-if="currentFeedback.reply">
          <label>回复：</label>
          <div class="reply">
            <p class="reply-content">{{ currentFeedback.reply }}</p>
            <div class="reply-info">
              <span>回复人：{{ currentFeedback.replyByName }}</span>
              <span>回复时间：{{ formatDateTime(currentFeedback.replyTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Feedback',
  data() {
    return {
      loading: false,
      submitting: false,
      filterType: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      feedbackList: [],
      dialogVisible: false,
      detailVisible: false,
      currentFeedback: null,
      form: {
        type: '',
        title: '',
        content: ''
      },
      rules: {
        type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.fetchFeedbackList()
  },
  methods: {
    async fetchFeedbackList() {
      try {
        this.loading = true
        const response = await this.$axios.get('/api/student/feedback', {
          params: {
            type: this.filterType,
            page: this.currentPage - 1,
            size: this.pageSize
          }
        })
        
        if (response.data.code === 200) {
          const data = response.data.data
          this.feedbackList = data.content
          this.total = data.totalElements
        }
      } catch (error) {
        this.$message.error('获取反馈列表失败')
      } finally {
        this.loading = false
      }
    },
    handleAdd() {
      this.form = {
        type: '',
        title: '',
        content: ''
      }
      this.dialogVisible = true
    },
    async submitFeedback() {
      try {
        await this.$refs.form.validate()
        this.submitting = true
        
        const response = await this.$axios.post('/api/student/feedback', this.form)
        
        if (response.data.code === 200) {
          this.$message.success('反馈提交成功')
          this.dialogVisible = false
          this.fetchFeedbackList()
        }
      } catch (error) {
        this.$message.error('提交失败：' + (error.message || '未知错误'))
      } finally {
        this.submitting = false
      }
    },
    handleView(row) {
      this.currentFeedback = row
      this.detailVisible = true
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchFeedbackList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchFeedbackList()
    },
    formatDateTime(datetime) {
      if (!datetime) return ''
      return new Date(datetime).toLocaleString()
    },
    getTypeTagType(type) {
      const types = {
        'SUGGESTION': 'success',
        'BUG': 'danger',
        'OTHER': 'info'
      }
      return types[type] || 'info'
    },
    getTypeLabel(type) {
      const labels = {
        'SUGGESTION': '建议',
        'BUG': '问题反馈',
        'OTHER': '其他'
      }
      return labels[type] || type
    },
    getStatusTagType(status) {
      const types = {
        'PENDING': 'info',
        'PROCESSING': 'warning',
        'RESOLVED': 'success'
      }
      return types[status] || 'info'
    },
    getStatusLabel(status) {
      const labels = {
        'PENDING': '待处理',
        'PROCESSING': '处理中',
        'RESOLVED': '已解决'
      }
      return labels[status] || status
    }
  }
}
</script>

<style lang="scss" scoped>
.feedback {
  padding: 20px;

  .info-card {
    margin-bottom: 20px;
    
    .info-content {
      display: flex;
      align-items: flex-start;

      i {
        font-size: 24px;
        color: #409EFF;
        margin-right: 15px;
        margin-top: 3px;
      }

      .info-text {
        h3 {
          margin: 0 0 10px;
          color: #303133;
        }

        p {
          margin: 0;
          color: #606266;
        }
      }
    }
  }

  .operation-bar {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .feedback-detail {
    .detail-item {
      margin-bottom: 15px;

      label {
        font-weight: bold;
        color: #606266;
        margin-right: 10px;
      }

      .content {
        margin: 10px 0;
        padding: 10px;
        background: #f8f8f8;
        border-radius: 4px;
        white-space: pre-wrap;
      }

      .reply {
        margin-top: 10px;
        padding: 10px;
        background: #f0f9eb;
        border-radius: 4px;

        .reply-content {
          margin: 0 0 10px;
          white-space: pre-wrap;
        }

        .reply-info {
          color: #909399;
          font-size: 13px;

          span {
            margin-right: 15px;
          }
        }
      }
    }
  }

  .el-pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style> 