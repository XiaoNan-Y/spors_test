<template>
  <div class="teacher-dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div slot="header">
            <span>待审核申请</span>
          </div>
          <div class="card-content">
            {{ stats.pendingReviews || 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div slot="header">
            <span>本月测试人数</span>
          </div>
          <div class="card-content">
            {{ stats.monthlyTests || 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div slot="header">
            <span>班级数量</span>
          </div>
          <div class="card-content">
            {{ stats.classCount || 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div slot="header">
            <span>学生总数</span>
          </div>
          <div class="card-content">
            {{ stats.studentCount || 0 }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近通知 -->
    <el-card class="notice-card">
      <div slot="header">
        <span>最近通知</span>
      </div>
      <el-table :data="notices" style="width: 100%">
        <el-table-column prop="title" label="标题"></el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180"></el-table-column>
        <el-table-column fixed="right" label="操作" width="120">
          <template slot-scope="scope">
            <el-button @click="viewNotice(scope.row)" type="text" size="small">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'TeacherDashboard',
  data() {
    return {
      stats: {
        pendingReviews: 0,
        monthlyTests: 0,
        classCount: 0,
        studentCount: 0
      },
      notices: []
    }
  },
  created() {
    this.getStats()
    this.getNotices()
  },
  methods: {
    async getStats() {
      try {
        // TODO: 调用获取统计数据的API
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    },
    async getNotices() {
      try {
        // TODO: 调用获取通知列表的API
      } catch (error) {
        console.error('获取通知列表失败:', error)
      }
    },
    viewNotice(notice) {
      // TODO: 实现查看通知详情的功能
    }
  }
}
</script>

<style scoped>
.teacher-dashboard {
  padding: 20px;
}

.el-row {
  margin-bottom: 20px;
}

.card-content {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
}

.notice-card {
  margin-top: 20px;
}
</style> 