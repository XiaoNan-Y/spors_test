<template>
  <div class="teacher-dashboard">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <h2>欢迎回来，{{ teacherName }}</h2>
      <p>{{ currentDate }}</p>
    </div>

    <!-- 数据概览 -->
    <el-row :gutter="20" class="data-overview">
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-document-checked"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">免测审核</div>
              <div class="stat-value">{{ stats.pendingReviews || 0 }}</div>
              <div class="stat-detail">待处理申请</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="stat-card">
            <div class="stat-icon blue">
              <i class="el-icon-s-data"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">班级统计</div>
              <div class="stat-value">{{ stats.classCount || 0 }}</div>
              <div class="stat-detail">测试完成率: {{ stats.testCompletion || 0 }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="stat-card">
            <div class="stat-icon green">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">成绩管理</div>
              <div class="stat-value">{{ stats.classCount || 0 }}</div>
              <div class="stat-detail">班级数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="stat-card">
            <div class="stat-icon purple">
              <i class="el-icon-bell"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">通知管理</div>
              <div class="stat-value">{{ stats.unreadNotices || 0 }}</div>
              <div class="stat-detail">未读通知</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :span="16">
        <el-card class="chart-card">
          <div slot="header">
            <span>班级测试完成情况</span>
            <el-radio-group v-model="chartTimeRange" size="small" style="float: right">
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-container">
            <v-chart :option="classTestChartOption" autoresize />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <div slot="header">
            <span>学生成绩分布</span>
          </div>
          <div class="chart-container">
            <v-chart :option="gradeDistributionOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 通知和待办 -->
    <el-row :gutter="20" class="bottom-section">
      <el-col :span="12">
        <el-card class="notice-card">
          <div slot="header">
            <span>最近通知</span>
            <el-button style="float: right" type="text">查看全部</el-button>
          </div>
          <el-table :data="notices" style="width: 100%">
            <el-table-column prop="title" label="标题">
              <template slot-scope="scope">
                <el-tag size="mini" :type="scope.row.type === 'important' ? 'danger' : ''" v-if="scope.row.type">{{ scope.row.type === 'important' ? '重要' : '普通' }}</el-tag>
                {{ scope.row.title }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="180"></el-table-column>
            <el-table-column fixed="right" label="操作" width="120">
              <template slot-scope="scope">
                <el-button @click="viewNotice(scope.row)" type="text" size="small">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="todo-card">
          <div slot="header">
            <span>待办事项</span>
            <el-button style="float: right" type="text" @click="refreshTodos">刷新</el-button>
          </div>
          <el-timeline>
            <el-timeline-item
              v-for="(todo, index) in todos"
              :key="index"
              :type="todo.type"
              :timestamp="todo.time">
              {{ todo.content }}
              <el-button type="text" size="mini" @click="handleTodo(todo)">处理</el-button>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import ECharts from 'vue-echarts'
import 'echarts/lib/chart/line'
import 'echarts/lib/chart/pie'
import 'echarts/lib/component/tooltip'
import 'echarts/lib/component/legend'
import 'echarts/lib/component/grid'

export default {
  name: 'TeacherDashboard',
  components: {
    'v-chart': ECharts
  },
  data() {
    return {
      teacherName: '',
      currentDate: '',
      chartTimeRange: 'week',
      stats: {
        pendingReviews: 0,        // 待审核免测申请数
        classCount: 0,            // 管理的班级数
        testCompletion: 0,        // 本月测试完成率
        unreadNotices: 0,         // 未读通知数
        monthlyTests: 0,          // 本月测试人数
        activeClasses: 0,         // 活跃班级数
        studentCount: 0,          // 学生总数
        reviewTrend: 0,           // 审核趋势
        testParticipationRate: 0  // 参测率
      },
      notices: [],
      todos: [],
      classTestChartOption: {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['测试完成率', '及格率']
        },
        xAxis: {
          type: 'category',
          data: ['班级A', '班级B', '班级C', '班级D', '班级E']
        },
        yAxis: {
          type: 'value',
          name: '百分比',
          axisLabel: {
            formatter: '{value}%'
          }
        },
        series: [
          {
            name: '测试完成率',
            data: [95, 88, 76, 85, 92],
            type: 'bar',
            barWidth: '20%',
            itemStyle: {
              color: '#409EFF'
            }
          },
          {
            name: '及格率',
            data: [90, 85, 70, 80, 88],
            type: 'bar',
            barWidth: '20%',
            itemStyle: {
              color: '#67C23A'
            }
          }
        ]
      },
      gradeDistributionOption: {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}人 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: ['优秀(90-100分)', '良好(80-89分)', '及格(60-79分)', '不及格(0-59分)']
        },
        series: [{
          name: '成绩分布',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'outside',
            formatter: '{b}: {c}人'
          },
          data: [
            { 
              value: 30, 
              name: '优秀(90-100分)',
              itemStyle: { color: '#67C23A' }
            },
            { 
              value: 45, 
              name: '良好(80-89分)',
              itemStyle: { color: '#409EFF' }
            },
            { 
              value: 20, 
              name: '及格(60-79分)',
              itemStyle: { color: '#E6A23C' }
            },
            { 
              value: 5, 
              name: '不及格(0-59分)',
              itemStyle: { color: '#F56C6C' }
            }
          ]
        }]
      }
    }
  },
  created() {
    this.initData()
  },
  methods: {
    initData() {
      const user = JSON.parse(localStorage.getItem('user'))
      this.teacherName = user ? user.name : '老师'
      this.currentDate = new Date().toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      })
      this.getStats()
      this.getNotices()
      this.getTodos()
    },
    percentageFormat(val) {
      return val + '%'
    },
    async getStats() {
      try {
        const { data } = await this.$http.get('/api/teacher/dashboard-stats')
        if (data.code === 200) {
          this.stats = {
            ...this.stats,
            ...data.data
          }
          // 更新图表数据
          this.updateChartData()
        } else {
          this.$message.error(data.message || '获取统计数据失败')
        }
      } catch (error) {
        this.$message.error('获取统计数据失败')
        console.error('获取统计数据失败:', error)
      }
    },

    updateChartData() {
      // 更新班级测试完成情况图表
      this.classTestChartOption.series[0].data = [
        this.stats.testCompletion || 0,
        this.stats.testCompletion || 0,
        this.stats.testCompletion || 0,
        this.stats.testCompletion || 0,
        this.stats.testCompletion || 0
      ]

      // 更新成绩分布图表
      const total = this.stats.studentCount || 100
      this.gradeDistributionOption.series[0].data = [
        {
          value: Math.round(total * 0.3),
          name: '优秀(90-100分)',
          itemStyle: { color: '#67C23A' }
        },
        {
          value: Math.round(total * 0.45),
          name: '良好(80-89分)',
          itemStyle: { color: '#409EFF' }
        },
        {
          value: Math.round(total * 0.2),
          name: '及格(60-79分)',
          itemStyle: { color: '#E6A23C' }
        },
        {
          value: Math.round(total * 0.05),
          name: '不及格(0-59分)',
          itemStyle: { color: '#F56C6C' }
        }
      ]
    },
    async getNotices() {
      try {
        // TODO: 调用获取通知列表的API
        // const { data } = await this.$http.get('/api/notices')
        // this.notices = data
      } catch (error) {
        this.$message.error('获取通知列表失败')
        console.error('获取通知列表失败:', error)
      }
    },
    async getTodos() {
      try {
        // TODO: 调用获取待办事项的API
        // const { data } = await this.$http.get('/api/teacher/todos')
        // this.todos = data
      } catch (error) {
        this.$message.error('获取待办事项失败')
        console.error('获取待办事项失败:', error)
      }
    },
    viewNotice(notice) {
      // TODO: 实现查看通知详情的功能
      this.$message.info('查看通知详情：' + notice.title)
    },
    handleTodo(todo) {
      // TODO: 实现处理待办事项的功能
      this.$message.info('处理待办事项：' + todo.content)
    },
    refreshTodos() {
      this.getTodos()
    }
  }
}
</script>

<style scoped>
.teacher-dashboard {
  padding: 20px;
}

.welcome-section {
  margin-bottom: 24px;
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

.data-overview {
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background-color: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon i {
  font-size: 24px;
  color: #409EFF;
}

.stat-icon.blue {
  background-color: #ecf5ff;
}

.stat-icon.blue i {
  color: #409EFF;
}

.stat-icon.green {
  background-color: #f0f9eb;
}

.stat-icon.green i {
  color: #67C23A;
}

.stat-icon.purple {
  background-color: #f5f0ff;
}

.stat-icon.purple i {
  color: #8B5CF6;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  color: #67C23A;
}

.stat-detail {
  font-size: 12px;
  color: #909399;
}

.chart-section {
  margin-bottom: 24px;
}

.chart-card {
  margin-bottom: 24px;
}

.chart-container {
  height: 300px;
}

.bottom-section {
  margin-bottom: 24px;
}

.notice-card,
.todo-card {
  height: 400px;
}

.el-timeline {
  padding: 16px;
  max-height: 320px;
  overflow-y: auto;
}
</style>