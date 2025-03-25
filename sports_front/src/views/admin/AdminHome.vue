<template>
  <div class="admin-home">
    <!-- 标题和欢迎区域 -->
    <div class="welcome-section">
      <h2>欢迎回来，管理员</h2>
      <p>{{ currentDate }}</p>
    </div>

    <!-- 数据概览卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="box-card" shadow="hover">
          <div class="card-content">
            <div class="icon-wrapper student-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="info">
              <div class="number">5</div>
              <div class="label">学生总数</div>
              <div class="desc">在校学生总人数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card" shadow="hover">
          <div class="card-content">
            <div class="icon-wrapper teacher-icon">
              <i class="el-icon-s-custom"></i>
            </div>
            <div class="info">
              <div class="number">3</div>
              <div class="label">教师总数</div>
              <div class="desc">任课教师总人数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card" shadow="hover">
          <div class="card-content">
            <div class="icon-wrapper sports-icon">
              <i class="el-icon-trophy"></i>
            </div>
            <div class="info">
              <div class="number">6</div>
              <div class="label">体育项目</div>
              <div class="desc">当前测试项目数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card" shadow="hover">
          <div class="card-content">
            <div class="icon-wrapper record-icon">
              <i class="el-icon-data-line"></i>
            </div>
            <div class="info">
              <div class="number">23</div>
              <div class="label">测试记录</div>
              <div class="desc">累计测试记录数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作区域 -->
    <el-card class="operation-card">
      <div slot="header" class="clearfix">
        <span>快捷操作</span>
      </div>
      <div class="quick-actions">
        <div class="action-card" @click="handleQuickAction('add-record')">
          <i class="el-icon-plus"></i>
          <span>录入成绩</span>
        </div>
        <div class="action-card" @click="handleQuickAction('review-data')">
          <i class="el-icon-check"></i>
          <span>审核数据</span>
        </div>
        <div class="action-card" @click="handleQuickAction('review-exemption')">
          <i class="el-icon-document-checked"></i>
          <span>免测审核</span>
        </div>
        <div class="action-card" @click="handleQuickAction('manage-students')">
          <i class="el-icon-user"></i>
          <span>学生管理</span>
        </div>
      </div>
    </el-card>

    <!-- 最新公告区域 -->
    <el-card class="notice-card">
      <div slot="header" class="clearfix">
        <span>最新公告</span>
        <el-button style="float: right; padding: 3px 0" type="text">查看更多</el-button>
      </div>
      <div class="notice-list">
        <div v-for="notice in notices" :key="notice.id" class="notice-item">
          <span class="notice-title">{{ notice.title }}</span>
          <span class="notice-time">{{ formatDateTime(notice.time) }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'AdminHome',
  data() {
    return {
      currentDate: this.formatDate(new Date()),
      notices: [
        { id: 1, title: '体育馆开馆时间变更', time: '2025-03-18T19:32:34' },
        { id: 2, title: '2024年体育测试开始', time: '2025-03-17T19:45:25' },
        { id: 3, title: '体育馆维修通知', time: '2025-03-17T19:45:25' }
      ]
    }
  },
  methods: {
    formatDate(date) {
      const year = date.getFullYear()
      const month = date.getMonth() + 1
      const day = date.getDate()
      const weekDay = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][date.getDay()]
      return `${year}年${month}月${day}日 ${weekDay}`
    },
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      })
    },
    handleQuickAction(action) {
      switch (action) {
        case 'add-record':
          this.$router.push('/admin/data-review');
          break;
        case 'review-data':
          this.$router.push('/admin/data-review');
          break;
        case 'review-exemption':
          this.$router.push('/admin/exemption-review');
          break;
        case 'manage-students':
          this.$router.push('/admin/users/student');
          break;
        default:
          console.warn('未定义的操作:', action);
      }
    }
  }
}
</script>

<style scoped>
.admin-home {
  min-height: 100%;
  background-color: #f0f2f5;
  padding: 20px;
}

.welcome-section {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.welcome-section h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
  margin-bottom: 10px;
}

.welcome-section p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.box-card {
  margin-bottom: 20px;
}

.card-content {
  display: flex;
  align-items: center;
}

.icon-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.icon-wrapper i {
  font-size: 30px;
  color: #fff;
}

.student-icon {
  background-color: #409EFF;
}

.teacher-icon {
  background-color: #67C23A;
}

.sports-icon {
  background-color: #E6A23C;
}

.record-icon {
  background-color: #909399;
}

.info {
  flex: 1;
}

.info .number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.info .label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.info .desc {
  font-size: 12px;
  color: #909399;
}

.operation-card {
  margin-bottom: 20px;
}

.operation-card .el-button {
  width: 100%;
  margin-bottom: 10px;
}

.notice-card {
  margin-bottom: 20px;
}

.notice-list .notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
}

.notice-list .notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  color: #303133;
  font-size: 14px;
}

.notice-time {
  color: #909399;
  font-size: 12px;
}

.quick-actions {
  display: flex;
  justify-content: space-between;
  margin: 10px 0;
}

.action-card {
  flex: 1;
  margin: 0 10px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #409EFF;
  color: white;
  border-radius: 4px;
}

.action-card:nth-child(1) {
  background-color: #409EFF;
}

.action-card:nth-child(2) {
  background-color: #67C23A;
}

.action-card:nth-child(3) {
  background-color: #E6A23C;
}

.action-card:nth-child(4) {
  background-color: #909399;
}

.action-card:hover {
  opacity: 0.8;
}

.action-card i {
  font-size: 18px;
  margin-right: 5px;
  color: white;
}

.action-card span {
  font-size: 14px;
  color: white;
}
</style> 