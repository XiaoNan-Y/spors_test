<template>
  <div class="dashboard">
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon student-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="stats-info">
            <div class="stats-title">学生总数</div>
            <div class="stats-number">{{ stats.studentCount }}</div>
            <div class="stats-desc">在校学生总人数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon teacher-icon">
            <i class="el-icon-s-custom"></i>
          </div>
          <div class="stats-info">
            <div class="stats-title">教师总数</div>
            <div class="stats-number">{{ stats.teacherCount }}</div>
            <div class="stats-desc">任职教师总人数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon item-icon">
            <i class="el-icon-trophy"></i>
          </div>
          <div class="stats-info">
            <div class="stats-title">体育项目</div>
            <div class="stats-number">
              <count-to
                :start-val="0"
                :end-val="stats.sportsItemCount"
                :duration="2000"
              />
            </div>
            <div class="stats-desc">当前测试项目数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon record-icon">
            <i class="el-icon-data-line"></i>
          </div>
          <div class="stats-info">
            <div class="stats-title">测试记录</div>
            <div class="stats-number">
              <count-to
                :start-val="0"
                :end-val="stats.testRecordCount"
                :duration="2000"
              />
            </div>
            <div class="stats-desc">累计测试记录数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作区域 -->
    <el-row :gutter="20" class="quick-access">
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>快捷操作</span>
          </div>
          <div class="quick-buttons">
            <el-button type="primary" @click="$router.push('/admin/users/student')">
              <i class="el-icon-plus"></i> 添加学生
            </el-button>
            <el-button type="success" @click="$router.push('/admin/users/teacher')">
              <i class="el-icon-plus"></i> 添加教师
            </el-button>
            <el-button type="warning" @click="$router.push('/admin/sports-items')">
              <i class="el-icon-plus"></i> 添加项目
            </el-button>
            <el-button type="info" @click="$router.push('/admin/notices')">
              <i class="el-icon-plus"></i> 发布公告
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>最新公告</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="$router.push('/admin/notices')">
              查看更多
            </el-button>
          </div>
          <div v-if="notices.length > 0">
            <div v-for="notice in notices" :key="notice.id" class="notice-item">
              <span class="notice-title">{{ notice.title }}</span>
              <span class="notice-time">{{ notice.createTime }}</span>
            </div>
          </div>
          <div v-else class="empty-text">
            暂无公告
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>各年级学生分布</span>
          </div>
          <div class="chart-container">
            <!-- 这里可以使用 ECharts 等图表库 -->
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>测试成绩分布</span>
          </div>
          <div class="chart-container">
            <!-- 这里可以使用 ECharts 等图表库 -->
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'  // 需要安装 vue-count-to 包

export default {
  name: 'AdminDashboard',
  components: {
    CountTo
  },
  data() {
    return {
      stats: {
        studentCount: 0,
        teacherCount: 0,
        sportsItemCount: 0,
        testRecordCount: 0
      },
      notices: []
    }
  },
  created() {
    this.fetchStats()
    this.fetchNotices()
  },
  methods: {
    async fetchStats() {
      try {
        const res = await this.$http.get('/admin/dashboard/stats')
        if (res.data.code === 200) {
          this.stats = res.data.data
        } else {
          this.$message.error(res.data.msg || '获取统计数据失败')
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
        this.$message.error('获取统计数据失败')
      }
    },
    async fetchNotices() {
      try {
        const res = await this.$http.get('/admin/notices/latest')
        if (res.data.code === 200) {
          this.notices = res.data.data
        }
      } catch (error) {
        console.error('获取公告失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  display: flex;
  align-items: center;
  padding: 20px;
  transition: all 0.3s;
  cursor: pointer;
}

.stats-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.stats-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin-right: 20px;
}

.student-icon {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  color: white;
}

.teacher-icon {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}

.item-icon {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
  color: white;
}

.record-icon {
  background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%);
  color: white;
}

.stats-info {
  flex-grow: 1;
}

.stats-title {
  font-size: 16px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.stats-number {
  font-size: 28px;
  font-weight: bold;
  color: #262626;
  line-height: 1.4;
}

.stats-desc {
  font-size: 14px;
  color: #8c8c8c;
  margin-top: 4px;
}

.quick-access {
  margin-bottom: 20px;
}

.quick-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  color: #303133;
}

.notice-time {
  color: #909399;
  font-size: 12px;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}
</style> 