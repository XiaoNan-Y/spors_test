<template>
  <div class="appeal-review">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="待处理" value="PENDING"/>
            <el-option label="已通过" value="APPROVED"/>
            <el-option label="已驳回" value="REJECTED"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchAppeals">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="appealList" border style="width: 100%">
      <el-table-column prop="studentName" label="学生姓名" width="120"/>
      <el-table-column prop="studentNumber" label="学号" width="120"/>
      <el-table-column prop="className" label="班级" width="120"/>
      <el-table-column prop="testItem" label="测试项目" width="120"/>
      <el-table-column label="成绩信息" width="180">
        <template slot-scope="scope">
          <div>原始成绩: {{ scope.row.originalScore }}</div>
          <div>期望成绩: {{ scope.row.expectedScore }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="申诉理由" min-width="200"/>
      <el-table-column prop="createTime" label="申请时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
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
            @click="handleReview(scope.row)"
            :disabled="scope.row.status !== 'PENDING'"
          >
            审核
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

    <!-- 审核对话框 -->
    <el-dialog
      title="申诉审核"
      :visible.sync="reviewDialogVisible"
      width="50%"
    >
      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="学生姓名">
          <span>{{ currentAppeal ? currentAppeal.studentName : '' }}</span>
        </el-form-item>
        <el-form-item label="测试项目">
          <span>{{ currentAppeal ? currentAppeal.testItem : '' }}</span>
        </el-form-item>
        <el-form-item label="原始成绩">
          <span>{{ currentAppeal ? currentAppeal.originalScore : '' }}</span>
        </el-form-item>
        <el-form-item label="期望成绩">
          <span>{{ currentAppeal ? currentAppeal.expectedScore : '' }}</span>
        </el-form-item>
        <el-form-item label="申诉理由">
          <div class="appeal-reason">{{ currentAppeal ? currentAppeal.reason : '' }}</div>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="reviewForm.reviewComment"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ScoreAppealReview',
  data() {
    return {
      appealList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      searchForm: {
        status: ''
      },
      reviewDialogVisible: false,
      currentAppeal: null,
      reviewForm: {
        status: '',
        reviewComment: ''
      }
    }
  },

  created() {
    this.getAppeals()
  },

  methods: {
    // 获取申诉列表
    async getAppeals() {
      try {
        console.log('正在获取申诉列表，参数:', {
          status: this.searchForm.status,
          page: this.currentPage - 1,
          size: this.pageSize
        })
        
        // 修改请求路径，确保与后端匹配
        const response = await this.$axios.get('/api/teacher/score-appeals', {
          params: {
            status: this.searchForm.status || null,  // 如果为空字符串，则传null
            page: this.currentPage - 1,
            size: this.pageSize
          }
        })

        console.log('API响应数据:', response.data)

        if (response.data.code === 200) {
          const { content, totalElements } = response.data.data
          this.appealList = content || []
          this.total = totalElements || 0
          console.log('处理后的申诉列表:', this.appealList)
        } else {
          this.$message.error(response.data.message || '获取数据失败')
        }
      } catch (error) {
        console.error('获取申诉列表失败:', error)
        this.$message.error('获取申诉列表失败: ' + (error.response?.data?.message || error.message))
      }
    },

    // 搜索
    searchAppeals() {
      this.currentPage = 1
      this.getAppeals()
    },

    // 处理审核
    handleReview(row) {
      this.currentAppeal = row
      this.reviewForm = {
        status: '',
        reviewComment: ''
      }
      this.reviewDialogVisible = true
    },

    // 提交审核
    async submitReview() {
      if (!this.reviewForm.status) {
        this.$message.warning('请选择审核结果')
        return
      }

      try {
        const response = await this.$axios.put(
          `/api/teacher/score-appeals/${this.currentAppeal.id}/review`,
          {
            status: this.reviewForm.status,
            reviewComment: this.reviewForm.reviewComment,
            expectedScore: this.currentAppeal.expectedScore
          }
        )

        if (response.data.code === 200) {
          this.$message.success('审核成功')
          this.reviewDialogVisible = false
          this.getAppeals()
        } else {
          this.$message.error(response.data.message || '审核失败')
        }
      } catch (error) {
        console.error('审核失败:', error)
        this.$message.error('审核失败')
      }
    },

    // 页码改变
    handlePageChange(page) {
      this.currentPage = page
      this.getAppeals()
    },

    // 格式化日期时间
    formatDateTime(datetime) {
      if (!datetime) return ''
      const date = new Date(datetime)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },

    // 获取状态标签类型
    getStatusTag(status) {
      const tags = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return tags[status] || ''
    },

    // 获取状态标签文本
    getStatusLabel(status) {
      const labels = {
        'PENDING': '待处理',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
      }
      return labels[status] || status
    }
  }
}
</script>

<style scoped>
.appeal-review {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.appeal-reason {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  min-height: 60px;
}

.dialog-footer {
  text-align: right;
}
</style>
