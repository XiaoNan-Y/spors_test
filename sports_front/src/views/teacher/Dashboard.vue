<template>
  <div class="dashboard-container">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <h2>欢迎回来，老师</h2>
      <p>{{ currentDate }}</p>
    </div>

    <!-- 功能模块卡片 -->
    <el-row :gutter="20">
      <!-- 班级成绩统计 -->
      <el-col :span="8">
        <el-card shadow="hover" class="module-card" @click="navigateTo('/teacher/class-stats')">
          <div class="module-content">
            <i class="el-icon-s-data module-icon"></i>
            <div class="module-info">
              <h3>班级成绩统计</h3>
              <div class="stats-info">
                <div class="stat-item">
                  <span class="stat-label">班级数量</span>
                  <span class="stat-value">{{ stats.classCount || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">测试完成率</span>
                  <span class="stat-value">{{ stats.testCompletionRate || 0 }}%</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">及格率</span>
                  <span class="stat-value">{{ stats.passRate || 0 }}%</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 学生成绩管理 -->
      <el-col :span="8">
        <el-card shadow="hover" class="module-card" @click="navigateTo('/teacher/student-records')">
          <div class="module-content">
            <i class="el-icon-document module-icon"></i>
            <div class="module-info">
              <h3>学生成绩管理</h3>
              <div class="stats-info">
                <div class="stat-item">
                  <span class="stat-label">待录入成绩</span>
                  <span class="stat-value warning">{{ stats.pendingRecords || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">本周已录入</span>
                  <span class="stat-value">{{ stats.weeklyRecords || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 免测/重测审核 -->
      <el-col :span="8">
        <el-card shadow="hover" class="module-card" @click="navigateTo('/teacher/exemption-review')">
          <div class="module-content">
            <i class="el-icon-document-checked module-icon"></i>
            <div class="module-info">
              <h3>免测/重测审核</h3>
              <div class="stats-info">
                <div class="stat-item">
                  <span class="stat-label">待审核申请</span>
                  <span class="stat-value warning">{{ stats.pendingReviews || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">本周已审核</span>
                  <span class="stat-value">{{ stats.weeklyReviewed || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      
      <!-- 通知管理 -->
      <el-col :span="8">
        <el-card shadow="hover" class="module-card" @click="navigateTo('/teacher/notice-management')">
          <div class="module-content">
            <i class="el-icon-bell module-icon"></i>
            <div class="module-info">
              <h3>通知管理</h3>
              <div class="stats-info">
                <div class="stat-item">
                  <span class="stat-label">最新通知</span>
                  <span class="stat-value">{{ stats.latestNotices || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">本周发布</span>
                  <span class="stat-value">{{ stats.weeklyNotices || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 近期活动 -->
      <el-col :span="8">
        <el-card shadow="hover" class="module-card">
          <div class="module-content">
            <i class="el-icon-date module-icon"></i>
            <div class="module-info">
              <h3>近期活动</h3>
              <div class="activity-list" v-if="stats.recentActivities && stats.recentActivities.length">
                <div v-for="(activity, index) in stats.recentActivities" :key="index" class="activity-item">
                  {{ activity.name }} - {{ activity.date }}
                </div>
              </div>
              <div v-else class="no-activity">
                暂无近期活动
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'TeacherDashboard',
  data() {
    return {
      teacherName: '',
      currentDate: '',
      stats: {
        classCount: 0,
        testCompletionRate: 0,
        passRate: 0,
        pendingRecords: 0,
        weeklyRecords: 0,
        pendingReviews: 0,
        weeklyReviewed: 0,
        totalStudents: 0,
        latestNotices: 0,
        weeklyNotices: 0,
        recentActivities: []
      }
    }
  },
  created() {
    this.initData()
    this.fetchDashboardStats()
  },
  methods: {
    initData() {
      const user = JSON.parse(localStorage.getItem('user'))
      this.teacherName = user ? user.realName || user.username : '老师'
      this.currentDate = new Date().toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      })
    },
    async fetchDashboardStats() {
      try {
        const response = await this.$http.get('/api/teacher/dashboard/stats')
        if (response.data.code === 200) {
          this.stats = { ...this.stats, ...response.data.data }
        } else {
          this.$message.error('获取统计数据失败')
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
        this.$message.error('获取统计数据失败')
      }
    },
    navigateTo(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.welcome-section {
  margin-bottom: 30px;
}

.welcome-section h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.welcome-section p {
  margin: 8px 0 0;
  color: #909399;
}

.module-card {
  height: 180px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.module-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.module-content {
  height: 100%;
  display: flex;
  align-items: flex-start;
  padding: 20px;
}

.module-icon {
  font-size: 48px;
  margin-right: 20px;
  color: #409EFF;
}

.module-info {
  flex: 1;
}

.module-info h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #303133;
}

.stats-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.stat-value {
  font-size: 16px;
  font-weight: bold;
  color: #409EFF;
}

.stat-value.warning {
  color: #E6A23C;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.activity-item {
  font-size: 14px;
  color: #606266;
}

.no-activity {
  color: #909399;
  font-size: 14px;
}
</style>
