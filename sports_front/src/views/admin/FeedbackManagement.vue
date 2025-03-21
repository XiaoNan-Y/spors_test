<template>
  <div class="feedback-management">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="反馈类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable>
            <el-option label="功能建议" value="SUGGESTION"/>
            <el-option label="问题反馈" value="BUG"/>
            <el-option label="其他" value="OTHER"/>
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="待处理" value="PENDING"/>
            <el-option label="已解决" value="RESOLVED"/>
            <el-option label="处理中" value="PROCESSING"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchFeedbacks">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="feedbackList" border style="width: 100%">
      <el-table-column prop="studentName" label="学生姓名" width="120"/>
      <el-table-column prop="studentNumber" label="学号" width="120"/>
      <el-table-column prop="className" label="班级" width="120"/>
      <el-table-column prop="title" label="标题" width="150"/>
      <el-table-column prop="type" label="类型" width="100">
        <template slot-scope="scope">
          <el-tag :type="getTypeTag(scope.row.type)">
            {{ getTypeLabel(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="反馈内容"/>
      <el-table-column prop="createTime" label="提交时间" width="180"/>
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusTag(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button 
            type="primary" 
            size="small" 
            @click="handleReply(scope.row)"
            :disabled="scope.row.status === 'CLOSED'"
          >
            回复
          </el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click="handleClose(scope.row)"
            :disabled="scope.row.status === 'CLOSED'"
          >
            关闭
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page.sync="currentPage"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 回复对话框 -->
    <el-dialog
      title="回复反馈"
      :visible.sync="replyDialogVisible"
      width="50%"
    >
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="反馈内容">
          <div class="feedback-content">{{ currentFeedback ? currentFeedback.content : '' }}</div>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.reply"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'FeedbackManagement',
  data() {
    return {
      feedbackList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      replyDialogVisible: false,
      currentFeedback: null,
      searchForm: {
        type: '',
        status: ''
      },
      replyForm: {
        reply: ''
      }
    }
  },

  created() {
    this.getFeedbacks()
  },

  methods: {
    // 获取反馈列表
    async getFeedbacks() {
      try {
        console.log('Fetching feedbacks...', {
          type: this.searchForm.type,
          status: this.searchForm.status,
          page: this.currentPage,
          size: this.pageSize
        });

        const response = await this.$axios.get('/api/admin/feedback', {
          params: {
            type: this.searchForm.type,
            status: this.searchForm.status,
            page: this.currentPage,
            size: this.pageSize
          }
        });

        console.log('Response:', response.data);

        if (response.data.code === 200) {
          this.feedbackList = response.data.data.content;
          this.total = response.data.data.totalElements;
          console.log('Feedbacks loaded:', this.feedbackList);
        } else {
          this.$message.error(response.data.message || '获取数据失败');
        }
      } catch (error) {
        console.error('Error fetching feedbacks:', error);
        this.$message.error('获取反馈列表失败: ' + (error.response?.data?.message || error.message));
      }
    },

    // 搜索
    searchFeedbacks() {
      this.currentPage = 1
      this.getFeedbacks()
    },

    // 处理回复
    handleReply(row) {
      this.currentFeedback = row
      this.replyForm.reply = ''
      this.replyDialogVisible = true
    },

    // 提交回复
    async submitReply() {
      if (!this.replyForm.reply.trim()) {
        this.$message.warning('请输入回复内容')
        return
      }

      try {
        const response = await this.$axios.put(`/api/admin/feedback/${this.currentFeedback.id}/reply`, {
          reply: this.replyForm.reply
        })
        if (response.data.code === 200) {
          this.$message.success('回复成功')
          this.replyDialogVisible = false
          this.getFeedbacks()
        }
      } catch (error) {
        this.$message.error('回复失败')
      }
    },

    // 关闭反馈
    handleClose(row) {
      this.$confirm('确定要关闭该反馈吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.put(`/api/admin/feedback/${row.id}/status`, null, {
            params: { status: 'CLOSED' }
          })
          if (response.data.code === 200) {
            this.$message.success('操作成功')
            this.getFeedbacks()
          }
        } catch (error) {
          this.$message.error('操作失败')
        }
      }).catch(() => {})
    },

    // 页码改变
    handlePageChange(page) {
      this.currentPage = page
      this.getFeedbacks()
    },

    // 类型标签
    getTypeLabel(type) {
      const types = {
        'SUGGESTION': '功能建议',
        'BUG': '问题反馈',
        'OTHER': '其他'
      }
      return types[type] || type
    },

    getTypeTag(type) {
      const tags = {
        'SUGGESTION': 'success',
        'BUG': 'danger',
        'OTHER': 'info'
      }
      return tags[type] || ''
    },

    // 状态标签
    getStatusLabel(status) {
      const statuses = {
        'PENDING': '待处理',
        'PROCESSING': '处理中',
        'RESOLVED': '已解决'
      }
      return statuses[status] || status
    },

    getStatusTag(status) {
      const tags = {
        'PENDING': 'warning',
        'PROCESSING': 'primary',
        'RESOLVED': 'success'
      }
      return tags[status] || ''
    }
  }
}
</script>

<style scoped>
.feedback-management {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.feedback-content {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  min-height: 60px;
}

.dialog-footer {
  text-align: right;
}
</style> 