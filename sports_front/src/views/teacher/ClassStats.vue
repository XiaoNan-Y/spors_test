<template>
  <div class="class-stats">
    <!-- 筛选区域 -->
    <div class="filter-section">
      <el-form :inline="true" :model="queryParams" ref="queryForm">
        <el-form-item label="测试项目">
          <el-select v-model="queryParams.sportsItemId" placeholder="选择测试项目" clearable>
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="queryParams.className" placeholder="选择班级" clearable>
            <el-option
              v-for="item in classList"
              :key="item"
              :label="item"
              :value="item"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div slot="header">
            <span>平均分</span>
          </div>
          <div class="card-content">
            {{ stats.averageScore || '0.00' }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div slot="header">
            <span>优秀率</span>
          </div>
          <div class="card-content">
            {{ stats.excellentRate || '0.00' }}%
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div slot="header">
            <span>合格率</span>
          </div>
          <div class="card-content">
            {{ stats.passRate || '0.00' }}%
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div slot="header">
            <span>参测人数</span>
          </div>
          <div class="card-content">
            {{ stats.totalStudents || '0' }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表展示 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header">
            <span>成绩分布</span>
          </div>
          <div class="chart-container">
            <div ref="scoreDistChart" style="height: 300px"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header">
            <span>班级对比</span>
          </div>
          <div class="chart-container">
            <div ref="classCompareChart" style="height: 300px"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card class="table-card">
      <div slot="header">
        <span>详细数据</span>
        <el-button
          style="float: right; padding: 3px 0"
          type="text"
          @click="handleExport"
        >导出Excel</el-button>
      </div>
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column label="班级" prop="className" align="center"></el-table-column>
        <el-table-column label="学号" prop="studentNumber" align="center"></el-table-column>
        <el-table-column label="姓名" prop="studentName" align="center"></el-table-column>
        <el-table-column label="成绩" prop="score" align="center"></el-table-column>
        <el-table-column label="等级" prop="grade" align="center">
          <template slot-scope="scope">
            <el-tag :type="getGradeType(scope.row.grade)">{{ scope.row.grade }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="测试时间" prop="testTime" align="center">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.testTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="queryParams.pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="queryParams.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getClassStats, getTestRecords } from '@/api/teacher'

export default {
  name: 'ClassStats',
  data() {
    return {
      // 查询参数
      queryParams: {
        sportsItemId: '',
        className: '',
        pageNum: 1,
        pageSize: 10
      },
      // 统计数据
      stats: {
        averageScore: 0,
        excellentRate: 0,
        passRate: 0,
        totalStudents: 0
      },
      // 表格数据
      tableData: [],
      total: 0,
      loading: false,
      // 下拉选项
      sportsItems: [],
      classList: [],
      // 图表实例
      scoreDistChart: null,
      classCompareChart: null
    }
  },
  created() {
    this.getList()
    this.getSportsItems()
    this.getClassList()
  },
  mounted() {
    this.initCharts()
    // 监听窗口大小变化，重绘图表
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
  },
  methods: {
    // 获取数据列表
    async getList() {
      this.loading = true
      try {
        const res = await getClassStats(this.queryParams)
        if (res.data.code === 200) {
          const data = res.data.data
          this.stats = data.stats
          this.tableData = data.records
          this.total = data.total
          this.updateCharts(data)
        }
      } catch (error) {
        console.error('获取数据失败:', error)
        this.$message.error('获取数据失败')
      }
      this.loading = false
    },

    // 初始化图表
    initCharts() {
      this.scoreDistChart = echarts.init(this.$refs.scoreDistChart)
      this.classCompareChart = echarts.init(this.$refs.classCompareChart)
      
      // 设置初始配置
      this.updateCharts({
        scoreDist: [],
        classCompare: []
      })
    },

    // 更新图表数据
    updateCharts(data) {
      // 成绩分布图配置
      const scoreDistOption = {
        title: {
          text: '成绩分布'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['不及格', '及格', '良好', '优秀']
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: data.scoreDist || [0, 0, 0, 0],
          type: 'bar'
        }]
      }
      
      // 班级对比图配置
      const classCompareOption = {
        title: {
          text: '班级平均分对比'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: data.classCompare?.map(item => item.className) || []
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: data.classCompare?.map(item => item.averageScore) || [],
          type: 'bar'
        }]
      }
      
      this.scoreDistChart.setOption(scoreDistOption)
      this.classCompareChart.setOption(classCompareOption)
    },

    // 重绘图表
    resizeCharts() {
      this.scoreDistChart?.resize()
      this.classCompareChart?.resize()
    },

    // 获取体育项目列表
    async getSportsItems() {
      try {
        // TODO: 调用获取体育项目列表的API
        this.sportsItems = []
      } catch (error) {
        console.error('获取体育项目列表失败:', error)
      }
    },

    // 获取班级列表
    async getClassList() {
      try {
        // TODO: 调用获取班级列表的API
        this.classList = []
      } catch (error) {
        console.error('获取班级列表失败:', error)
      }
    },

    // 查询按钮点击
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    // 重置按钮点击
    resetQuery() {
      this.queryParams = {
        sportsItemId: '',
        className: '',
        pageNum: 1,
        pageSize: 10
      }
      this.getList()
    },

    // 导出数据
    handleExport() {
      // TODO: 实现导出功能
    },

    // 分页大小改变
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.getList()
    },

    // 页码改变
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.getList()
    },

    // 获取成绩等级样式
    getGradeType(grade) {
      const gradeMap = {
        '优秀': 'success',
        '良好': '',
        '及格': 'warning',
        '不及格': 'danger'
      }
      return gradeMap[grade] || ''
    },

    // 格式化日期时间
    formatDateTime(datetime) {
      if (!datetime) return '-'
      const date = new Date(datetime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      })
    }
  }
}
</script>

<style scoped>
.class-stats {
  padding: 20px;
}

.filter-section {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.card-content {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-container {
  padding: 10px;
}

.table-card {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 