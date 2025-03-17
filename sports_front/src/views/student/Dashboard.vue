<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="info-card">
          <div slot="header">
            <span>个人信息</span>
          </div>
          <div class="info-content">
            <div class="info-item">
              <span class="label">姓名：</span>
              <span>{{ studentInfo.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">学号：</span>
              <span>{{ studentInfo.studentId }}</span>
            </div>
            <div class="info-item">
              <span class="label">班级：</span>
              <span>{{ studentInfo.className }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="test-status-card">
          <div slot="header">
            <span>体测状态</span>
          </div>
          <div class="test-status-content">
            <el-progress type="circle"
                         :percentage="testProgress"
                         :status="testProgressStatus">
            </el-progress>
            <div class="status-text">已完成 {{ completedItems }}/{{ totalItems }} 个项目</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="notice-card">
          <div slot="header">
            <span>最新通知</span>
            <el-button style="float: right; padding: 3px 0" type="text"
                       @click="$router.push('/student/notices')">查看更多</el-button>
          </div>
          <div class="notice-list">
            <div v-for="notice in recentNotices" :key="notice.id" class="notice-item">
              <span class="notice-title">{{ notice.title }}</span>
              <span class="notice-date">{{ notice.date }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="upcoming-tests-card">
          <div slot="header">
            <span>待测项目</span>
          </div>
          <el-table :data="upcomingTests" style="width: 100%">
            <el-table-column prop="name" label="项目名称"></el-table-column>
            <el-table-column prop="standard" label="达标要求"></el-table-column>
            <el-table-column prop="deadline" label="测试截止日期"></el-table-column>
            <el-table-column label="操作" width="150">
              <template slot-scope="scope">
                <el-button size="mini" type="primary"
                          @click="viewTestDetail(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'StudentDashboard',
  data() {
    return {
      studentInfo: {
        name: '',
        studentId: '',
        className: ''
      },
      completedItems: 0,
      totalItems: 0,
      recentNotices: [],
      upcomingTests: []
    }
  },
  computed: {
    testProgress() {
      return this.totalItems ? Math.round((this.completedItems / this.totalItems) * 100) : 0
    },
    testProgressStatus() {
      return this.testProgress === 100 ? 'success' : ''
    }
  },
  created() {
    this.fetchStudentInfo()
    this.fetchTestProgress()
    this.fetchRecentNotices()
    this.fetchUpcomingTests()
  },
  methods: {
    async fetchStudentInfo() {
      try {
        const response = await this.$http.get('/api/student/info')
        this.studentInfo = response.data
      } catch (error) {
        this.$message.error('获取个人信息失败')
      }
    },
    async fetchTestProgress() {
      try {
        const response = await this.$http.get('/api/student/test-progress')
        this.completedItems = response.data.completed
        this.totalItems = response.data.total
      } catch (error) {
        this.$message.error('获取体测进度失败')
      }
    },
    async fetchRecentNotices() {
      try {
        const response = await this.$http.get('/api/notices/recent')
        this.recentNotices = response.data
      } catch (error) {
        this.$message.error('获取通知失败')
      }
    },
    async fetchUpcomingTests() {
      try {
        const response = await this.$http.get('/api/student/upcoming-tests')
        this.upcomingTests = response.data
      } catch (error) {
        this.$message.error('获取待测项目失败')
      }
    },
    viewTestDetail(test) {
      this.$router.push(`/student/test-items/${test.id}`)
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.info-card, .test-status-card, .notice-card {
  height: 280px;
}

.info-content {
  padding: 20px 0;
}

.info-item {
  margin-bottom: 15px;
}

.info-item .label {
  font-weight: bold;
  margin-right: 10px;
}

.test-status-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.status-text {
  margin-top: 20px;
  color: #606266;
}

.notice-list {
  height: 200px;
  overflow-y: auto;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
}

.notice-title {
  flex: 1;
  margin-right: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-date {
  color: #909399;
  font-size: 13px;
}

.upcoming-tests-card {
  margin-bottom: 20px;
}
</style>