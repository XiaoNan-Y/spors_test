<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- 成绩概览 -->
      <el-col :span="8">
        <el-card class="box-card">
          <div slot="header">
            <span>成绩概览</span>
          </div>
          <div class="data-overview">
            <div class="data-item">
              <div class="label">已测项目</div>
              <div class="value">{{ stats.testedCount || 0 }}</div>
            </div>
            <div class="data-item">
              <div class="label">待测项目</div>
              <div class="value">{{ stats.pendingCount || 0 }}</div>
            </div>
            <div class="data-item">
              <div class="label">合格率</div>
              <div class="value">{{ stats.passRate || 0 }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 免测/重测申请 -->
      <el-col :span="8">
        <el-card class="box-card">
          <div slot="header">
            <span>申请状态</span>
          </div>
          <div class="application-status">
            <div class="data-item">
              <div class="label">待审核</div>
              <div class="value">{{ stats.pendingApplications || 0 }}</div>
            </div>
            <div class="data-item">
              <div class="label">已通过</div>
              <div class="value">{{ stats.approvedApplications || 0 }}</div>
            </div>
            <div class="data-item">
              <div class="label">已驳回</div>
              <div class="value">{{ stats.rejectedApplications || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 最新通知 -->
      <el-col :span="8">
        <el-card class="box-card">
          <div slot="header">
            <span>最新通知</span>
          </div>
          <div class="notice-list">
            <div v-if="!stats.latestNotices || stats.latestNotices.length === 0" class="empty-text">
              暂无通知
            </div>
            <div v-else v-for="notice in stats.latestNotices" :key="notice.id" class="notice-item">
              <span class="notice-title" @click="viewNotice(notice)">{{ notice.title }}</span>
              <span class="notice-time">{{ formatDate(notice.createTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近成绩 -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="box-card">
          <div slot="header">
            <span>最近成绩</span>
          </div>
          <el-table :data="stats.recentRecords || []" style="width: 100%">
            <el-table-column prop="sportsItem.name" label="测试项目" width="180"/>
            <el-table-column prop="score" label="成绩" width="180"/>
            <el-table-column prop="status" label="状态">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="测试时间">
              <template slot-scope="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { formatDate } from '@/utils/date'

export default {
  name: 'StudentDashboard',
  data() {
    return {
      stats: {}
    }
  },
  created() {
    this.fetchDashboardStats()
  },
  methods: {
    async fetchDashboardStats() {
      try {
        const response = await this.$http.get('/api/student/dashboard/stats')
        if (response.data.code === 200) {
          this.stats = response.data.data
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
        this.$message.error('获取统计数据失败')
      }
    },
    formatDate,
    viewNotice(notice) {
      this.$router.push(`/student/notices/${notice.id}`)
    },
    getStatusType(status) {
      const types = {
        PENDING: 'info',
        APPROVED: 'success',
        REJECTED: 'danger'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        PENDING: '待审核',
        APPROVED: '已通过',
        REJECTED: '已驳回'
      }
      return texts[status] || status
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.data-overview, .application-status {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.data-item {
  text-align: center;
}

.data-item .label {
  color: #909399;
  font-size: 14px;
}

.data-item .value {
  color: #303133;
  font-size: 24px;
  font-weight: bold;
  margin-top: 5px;
}

.notice-list {
  max-height: 250px;
  overflow-y: auto;
}

.notice-item {
  padding: 8px 0;
  border-bottom: 1px solid #EBEEF5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  flex: 1;
  margin-right: 10px;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-title:hover {
  color: #409EFF;
}

.notice-time {
  color: #909399;
  font-size: 12px;
}

.empty-text {
  color: #909399;
  text-align: center;
  padding: 20px 0;
}
</style> 