<template>
  <div class="class-statistics">
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="班级">
          <el-select v-model="filterForm.className" placeholder="选择班级" clearable>
            <el-option
              v-for="className in classNames"
              :key="className"
              :label="className"
              :value="className"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="测试项目">
          <el-select v-model="filterForm.sportsItemId" placeholder="选择测试项目" clearable>
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStatistics">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="statistics" v-loading="loading" border>
      <el-table-column prop="className" label="班级" />
      <el-table-column prop="totalCount" label="总人数" />
      <el-table-column prop="averageScore" label="平均分">
        <template slot-scope="scope">
          {{ scope.row.averageScore.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="excellentRate" label="优秀率">
        <template slot-scope="scope">
          {{ scope.row.excellentRate.toFixed(2) }}%
        </template>
      </el-table-column>
      <el-table-column prop="passRate" label="合格率">
        <template slot-scope="scope">
          {{ scope.row.passRate.toFixed(2) }}%
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'ClassStatistics',
  data() {
    return {
      loading: false,
      statistics: [],
      classNames: [],
      sportsItems: [],
      filterForm: {
        className: '',
        sportsItemId: ''
      }
    }
  },
  created() {
    this.loadClassNames()
    this.loadSportsItems()
    this.loadStatistics()
  },
  methods: {
    async loadClassNames() {
      try {
        const response = await this.$axios.get('/api/teacher/classes')
        if (response.data.code === 200) {
          this.classNames = response.data.data
        }
      } catch (error) {
        this.$message.error('获取班级列表失败')
      }
    },
    async loadSportsItems() {
      try {
        const response = await this.$axios.get('/api/teacher/sports-items')
        if (response.data.code === 200) {
          this.sportsItems = response.data.data
        }
      } catch (error) {
        this.$message.error('获取体育项目列表失败')
      }
    },
    async loadStatistics() {
      this.loading = true
      try {
        const response = await this.$axios.get('/api/teacher/statistics/class', {
          params: {
            className: this.filterForm.className || '',
            sportsItemId: this.filterForm.sportsItemId || ''
          }
        })
        if (response.data.code === 200) {
          this.statistics = response.data.data
        } else {
          this.$message.error(response.data.message || '获取统计数据失败')
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
        this.$message.error('获取统计数据失败')
      } finally {
        this.loading = false
      }
    },
    handleReset() {
      this.filterForm = {
        className: '',
        sportsItemId: ''
      }
      this.loadStatistics()
    }
  }
}
</script>

<style scoped>
.class-statistics {
  padding: 20px;
}
.filter-section {
  margin-bottom: 20px;
}
</style> 