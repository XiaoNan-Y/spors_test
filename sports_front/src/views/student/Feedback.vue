<template>
  <div class="feedback-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>反馈建议</span>
      </div>
      <el-form :model="feedbackForm" :rules="rules" ref="feedbackForm" label-width="100px">
        <el-form-item label="反馈类型" prop="type">
          <el-select v-model="feedbackForm.type" placeholder="请选择反馈类型">
            <el-option label="体测体验" value="test_experience"></el-option>
            <el-option label="系统建议" value="system_suggestion"></el-option>
            <el-option label="其他问题" value="other"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="反馈标题" prop="title">
          <el-input v-model="feedbackForm.title" placeholder="请输入反馈标题"></el-input>
        </el-form-item>
        <el-form-item label="反馈内容" prop="content">
          <el-input
            type="textarea"
            :rows="4"
            placeholder="请详细描述您的反馈内容"
            v-model="feedbackForm.content">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitFeedback" :loading="submitting">提交反馈</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 历史反馈列表 -->
      <div class="history-feedback">
        <h3>历史反馈</h3>
        <el-table
          v-loading="loading"
          :data="feedbackHistory"
          style="width: 100%">
          <el-table-column
            prop="type"
            label="类型"
            width="120">
            <template slot-scope="scope">
              {{ getFeedbackTypeName(scope.row.type) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="title"
            label="标题"
            width="180">
          </el-table-column>
          <el-table-column
            prop="content"
            label="内容">
          </el-table-column>
          <el-table-column
            prop="createTime"
            label="提交时间"
            width="180">
            <template slot-scope="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            label="状态"
            width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusName(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[5, 10, 20, 30]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total">
          </el-pagination>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'
import moment from 'moment'

export default {
  name: 'Feedback',
  data() {
    return {
      feedbackForm: {
        type: '',
        title: '',
        content: ''
      },
      rules: {
        type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
        title: [{ required: true, message: '请输入反馈标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }]
      },
      submitting: false,
      loading: false,
      feedbackHistory: [],
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.fetchFeedbackHistory()
  },
  methods: {
    async submitFeedback() {
      try {
        await this.$refs.feedbackForm.validate()
        this.submitting = true
        await axios.post('/api/feedback', this.feedbackForm)
        this.$message.success('反馈提交成功')
        this.resetForm()
        this.fetchFeedbackHistory()
      } catch (error) {
        if (error.response) {
          this.$message.error(error.response.data.message || '提交失败')
        } else {
          this.$message.error('提交失败，请稍后重试')
        }
        console.error('Error submitting feedback:', error)
      } finally {
        this.submitting = false
      }
    },
    resetForm() {
      this.$refs.feedbackForm.resetFields()
    },
    async fetchFeedbackHistory() {
      this.loading = true
      try {
        const response = await axios.get('/api/feedback/history', {
          params: {
            page: this.currentPage - 1,
            size: this.pageSize
          }
        })
        this.feedbackHistory = response.data.content
        this.total = response.data.totalElements
      } catch (error) {
        this.$message.error('获取历史反馈失败')
        console.error('Error fetching feedback history:', error)
      } finally {
        this.loading = false
      }
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchFeedbackHistory()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchFeedbackHistory()
    },
    formatDate(date) {
      return moment(date).format('YYYY-MM-DD HH:mm')
    },
    getFeedbackTypeName(type) {
      const types = {
        test_experience: '体测体验',
        system_suggestion: '系统建议',
        other: '其他问题'
      }
      return types[type] || type
    },
    getStatusType(status) {
      const types = {
        pending: 'info',
        processing: 'warning',
        resolved: 'success',
        rejected: 'danger'
      }
      return types[status] || 'info'
    },
    getStatusName(status) {
      const names = {
        pending: '待处理',
        processing: '处理中',
        resolved: '已解决',
        rejected: '已驳回'
      }
      return names[status] || status
    }
  }
}
</script>

<style scoped>
.feedback-container {
  padding: 20px;
}
.history-feedback {
  margin-top: 30px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>