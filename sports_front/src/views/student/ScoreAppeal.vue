<template>
  <div class="score-appeal">
    <!-- 顶部说明卡片 -->
    <el-card class="info-card">
      <div class="info-content">
        <i class="el-icon-info"></i>
        <div class="info-text">
          <h3>体测成绩申诉</h3>
          <p>如果您对体测成绩有异议，可以在此提交申诉。请详细说明申诉理由，我们会尽快处理。</p>
        </div>
      </div>
    </el-card>

    <!-- 申诉按钮 -->
    <div class="operation-bar">
      <el-button type="primary" @click="dialogVisible = true">
        提交申诉
      </el-button>
    </div>

    <!-- 申诉记录列表 -->
    <el-table
      :data="appealList"
      border
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="createTime" label="申诉时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="sportsItemName" label="测试项目"></el-table-column>
      <el-table-column prop="originalScore" label="原始成绩"></el-table-column>
      <el-table-column prop="expectedScore" label="期望成绩"></el-table-column>
      <el-table-column prop="reason" label="申诉理由" show-overflow-tooltip></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewComment" label="审核意见" show-overflow-tooltip></el-table-column>
      <el-table-column prop="reviewTime" label="审核时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.reviewTime) }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 申诉表单对话框 -->
    <el-dialog 
      title="提交申诉" 
      :visible.sync="dialogVisible"
      width="600px"
      :destroy-on-close="true"
      :close-on-click-modal="false"
    >
      <el-form 
        :model="appealForm" 
        :rules="rules" 
        ref="appealForm" 
        label-width="100px"
      >
        <el-form-item label="测试记录" prop="testRecordId">
          <el-select 
            v-model="appealForm.testRecordId" 
            placeholder="请选择要申诉的测试记录"
            style="width: 100%"
            @change="handleRecordChange"
          >
            <el-option
              v-for="record in testRecords"
              :key="record.id"
              :label="`${record.sportsItemName} - ${record.score}分`"
              :value="record.id"
            >
              <span style="float: left">{{ record.sportsItemName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ record.score }}分</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="期望成绩" prop="expectedScore">
          <el-input-number 
            v-model="appealForm.expectedScore" 
            :min="0" 
            :max="100"
            :precision="1"
            :step="0.1"
            :controls-position="'right'"
            style="width: 200px"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="申诉理由" prop="reason">
          <el-input 
            type="textarea" 
            v-model="appealForm.reason"
            :rows="4"
            placeholder="请详细说明申诉理由，例如：成绩记录错误、测试过程中的特殊情况等..."
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitAppeal" :loading="submitting">
          提 交
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ScoreAppeal',
  data() {
    return {
      loading: false,
      submitting: false,
      dialogVisible: false,
      appealList: [],
      testRecords: [], // 存储已通过审核的测试记录
      currentRecord: null,
      appealForm: {
        testRecordId: '',
        expectedScore: undefined,
        reason: ''
      },
      rules: {
        testRecordId: [
          { required: true, message: '请选择要申诉的测试记录', trigger: 'change' }
        ],
        expectedScore: [
          { required: true, message: '请输入期望成绩', trigger: 'blur' }
        ],
        reason: [
          { required: true, message: '请输入申诉理由', trigger: 'blur' },
          { min: 10, max: 500, message: '申诉理由长度应在10-500字之间', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchAppealList()
    this.fetchAppealableRecords() // 获取可申诉的记录
  },
  methods: {
    // 获取可申诉的记录
    async fetchAppealableRecords() {
      try {
        this.loading = true
        const userId = localStorage.getItem('userId')
        console.log('正在获取可申诉记录，userId:', userId)
        
        const response = await this.$axios.get('/api/student/test-records/appealable')
        console.log('获取可申诉记录响应:', response.data)
        
        if (response.data.code === 200) {
          this.testRecords = response.data.data
          console.log('设置可申诉记录:', this.testRecords)
          
          if (this.testRecords.length === 0) {
            this.$message.info('暂无可申诉的测试记录')
          }
        } else {
          this.$message.error(response.data.message || '获取可申诉记录失败')
        }
      } catch (error) {
        console.error('获取可申诉记录失败:', error)
        this.$message.error('获取可申诉记录失败')
      } finally {
        this.loading = false
      }
    },

    // 获取申诉列表
    async fetchAppealList() {
      try {
        this.loading = true
        const response = await this.$axios.get('/api/student/appeals')
        console.log('获取到的申诉列表:', response.data)
        
        if (response.data.code === 200) {
          // 确保返回的数据是数组
          this.appealList = Array.isArray(response.data.data) ? 
            response.data.data : 
            (response.data.data.content || [])
        } else {
          this.$message.error(response.data.message || '获取申诉列表失败')
        }
      } catch (error) {
        console.error('获取申诉列表失败:', error)
        this.$message.error(error.response?.data?.message || '获取申诉列表失败')
      } finally {
        this.loading = false
      }
    },

    // 处理记录选择变化
    handleRecordChange(recordId) {
      this.currentRecord = this.testRecords.find(r => r.id === recordId)
      if (this.currentRecord) {
        // 设置原始成绩
        this.appealForm.expectedScore = this.currentRecord.score
      }
    },

    // 提交申诉
    submitAppeal() {
      this.$refs.appealForm.validate(async valid => {
        if (!valid) {
          return
        }

        try {
          this.submitting = true
          // 构造提交数据，确保 expectedScore 是数字类型
          const submitData = {
            testRecordId: this.appealForm.testRecordId,
            expectedScore: parseFloat(this.appealForm.expectedScore),
            reason: this.appealForm.reason
          }

          const response = await this.$axios.post('/api/student/appeals', submitData)
          
          if (response.data.code === 200) {
            this.$message.success('申诉提交成功')
            this.dialogVisible = false
            this.fetchAppealList()
            this.$refs.appealForm.resetFields()
          } else {
            this.$message.error(response.data.message || '提交申诉失败')
          }
        } catch (error) {
          console.error('提交申诉失败:', error)
          this.$message.error(error.response?.data?.message || '提交申诉失败')
        } finally {
          this.submitting = false
        }
      })
    },

    // 状态显示相关方法
    getStatusType(status) {
      const types = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return types[status] || 'info'
    },

    getStatusText(status) {
      const texts = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
      }
      return texts[status] || status
    },

    formatDateTime(datetime) {
      if (!datetime) return ''
      return new Date(datetime).toLocaleString()
    }
  }
}
</script>

<style lang="scss" scoped>
.score-appeal {
  padding: 20px;

  .info-card {
    margin-bottom: 20px;
    
    .info-content {
      display: flex;
      align-items: flex-start;

      .el-icon-info {
        font-size: 24px;
        color: #409EFF;
        margin-right: 15px;
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
    margin: 20px 0;
  }

  // 添加一些动画效果
  .el-dialog {
    .el-select {
      width: 100%;
    }
    
    .el-input-number {
      width: 180px;
    }
    
    .el-textarea {
      textarea {
        transition: border-color 0.3s;
        &:hover, &:focus {
          border-color: #409EFF;
        }
      }
    }
  }

  // 添加表格内容的样式
  .el-table {
    margin-top: 20px;
    
    .el-tag {
      margin: 0;
    }
    
    .cell {
      white-space: pre-line;
    }
  }
}
</style> 