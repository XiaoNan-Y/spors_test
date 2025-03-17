<template>
  <div class="test-items-container">
    <el-card v-for="item in testItems" :key="item.id" class="test-item-card">
      <div slot="header" class="card-header">
        <span class="item-name">{{ item.name }}</span>
        <div class="header-actions">
          <el-tag :type="getStatusType(item.status)">{{ getStatusText(item.status) }}</el-tag>
          <el-button type="primary" size="small" @click="viewDetail(item)" style="margin-left: 10px">
            查看详情
          </el-button>
        </div>
      </div>
      
      <div class="item-content">
        <div class="item-info">
          <div class="info-row">
            <span class="label">测试时间：</span>
            <span>{{ item.testTime }}</span>
          </div>
          <div class="info-row">
            <span class="label">达标要求：</span>
            <span>{{ item.standard }}</span>
          </div>
          <div class="info-row">
            <span class="label">测试地点：</span>
            <span>{{ item.location }}</span>
          </div>
        </div>
        
        <div class="item-description">
          <div class="description-title">测试说明：</div>
          <div class="description-content">{{ item.description }}</div>
        </div>
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog :title="currentItem.name + ' - 详细信息'" :visible.sync="dialogVisible" width="50%">
      <div class="detail-content">
        <div class="detail-section">
          <h4>测试方法</h4>
          <p>{{ currentItem.testMethod }}</p>
        </div>
        
        <div class="detail-section">
          <h4>评分标准</h4>
          <el-table :data="currentItem.scoreStandards" style="width: 100%">
            <el-table-column prop="score" label="分数" width="100"></el-table-column>
            <el-table-column prop="requirement" label="要求"></el-table-column>
          </el-table>
        </div>

        <div class="detail-section" v-if="currentItem.status === 'completed'">
          <h4>我的成绩</h4>
          <div class="my-score">
            <span class="score-value">{{ currentItem.myScore }}</span>
            <span class="score-text">分</span>
          </div>
        </div>

        <div class="detail-actions" v-if="currentItem.status !== 'completed'">
          <el-button type="primary" @click="applyForTest">申请测试</el-button>
        </div>
        <div class="detail-actions" v-else>
          <el-button type="warning" @click="applyForRetest">申请重测</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TestItems',
  data() {
    return {
      testItems: [],
      dialogVisible: false,
      currentItem: {}
    }
  },
  created() {
    this.fetchTestItems()
  },
  methods: {
    async fetchTestItems() {
      try {
        const response = await this.$http.get('/api/student/test-items')
        this.testItems = response.data
      } catch (error) {
        this.$message.error('获取体测项目失败')
      }
    },
    getStatusType(status) {
      const types = {
        'not_started': 'info',
        'in_progress': 'warning',
        'completed': 'success',
        'missed': 'danger'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        'not_started': '未开始',
        'in_progress': '进行中',
        'completed': '已完成',
        'missed': '已错过'
      }
      return texts[status] || '未知状态'
    },
    viewDetail(item) {
      this.currentItem = { ...item }
      this.dialogVisible = true
    },
    async applyForTest() {
      try {
        await this.$http.post(`/api/student/test-items/${this.currentItem.id}/apply`)
        this.$message.success('申请成功')
        this.dialogVisible = false
        this.fetchTestItems()
      } catch (error) {
        this.$message.error('申请失败')
      }
    },
    async applyForRetest() {
      try {
        await this.$http.post(`/api/student/test-items/${this.currentItem.id}/retest`)
        this.$message.success('重测申请已提交')
        this.dialogVisible = false
        this.fetchTestItems()
      } catch (error) {
        this.$message.error('申请失败')
      }
    }
  }
}
</script>

<style scoped>
.test-items-container {
  padding: 20px;
}

.test-item-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-name {
  font-size: 16px;
  font-weight: bold;
}

.item-content {
  padding: 10px 0;
}

.item-info {
  margin-bottom: 20px;
}

.info-row {
  margin-bottom: 10px;
}

.label {
  font-weight: bold;
  margin-right: 10px;
  color: #606266;
}

.description-title {
  font-weight: bold;
  margin-bottom: 10px;
  color: #606266;
}

.description-content {
  line-height: 1.6;
  color: #303133;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin-bottom: 10px;
  color: #303133;
}

.my-score {
  text-align: center;
  padding: 20px 0;
}

.score-value {
  font-size: 36px;
  color: #409EFF;
  font-weight: bold;
}

.score-text {
  font-size: 16px;
  color: #606266;
  margin-left: 5px;
}

.detail-actions {
  text-align: center;
  margin-top: 20px;
}
</style>