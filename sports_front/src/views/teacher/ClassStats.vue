<template>
  <div class="class-stats">
    <!-- 搜索筛选区 -->
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="班级">
          <el-select v-model="filterForm.className" placeholder="选择班级" clearable>
            <el-option
              v-for="className in classNames"
              :key="className"
              :label="className"
              :value="className">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试项目">
          <el-select v-model="filterForm.sportsItemId" placeholder="选择测试项目" clearable>
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
            <el-option label="待审核" value="PENDING"></el-option>
            <el-option label="已通过" value="APPROVED"></el-option>
            <el-option label="已驳回" value="REJECTED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="statistics"
      border
      stripe
      style="width: 100%">
      <el-table-column
        prop="className"
        label="班级"
        align="center">
      </el-table-column>
      <el-table-column
        prop="totalCount"
        label="总人数"
        align="center">
      </el-table-column>
      <el-table-column
        prop="averageScore"
        label="平均分"
        align="center">
        <template slot-scope="scope">
          {{ scope.row.averageScore.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column
        label="优秀率"
        align="center">
        <template slot-scope="scope">
          {{ calculateRate(scope.row.excellentCount, scope.row.totalCount) }}%
        </template>
      </el-table-column>
      <el-table-column
        label="及格率"
        align="center">
        <template slot-scope="scope">
          {{ calculateRate(scope.row.passCount, scope.row.totalCount) }}%
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'ClassStats',
  data() {
    return {
      loading: false,
      statistics: [],
      classNames: [],
      sportsItems: [],
      filterForm: {
        className: '',
        sportsItemId: '',
        status: 'APPROVED'  // 默认统计已通过的记录
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
        console.error('获取班级列表失败:', error)
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
        console.error('获取体育项目列表失败:', error)
        this.$message.error('获取体育项目列表失败')
      }
    },
    async loadStatistics() {
      this.loading = true;
      try {
        const params = {
          className: this.filterForm.className || '',
          sportsItemId: this.filterForm.sportsItemId || '',
          status: this.filterForm.status || ''
        };
        console.log('发送统计查询参数:', params);
        
        const response = await this.$axios.get('/api/teacher/statistics/class', { params });
        console.log('统计数据原始响应:', response);
        
        if (response.data.code === 200) {
          this.statistics = response.data.data;
          console.log('处理后的统计数据:', this.statistics);
          
          if (this.statistics.length === 0) {
            this.$message.warning('没有找到符合条件的统计数据');
          }
        } else {
          throw new Error(response.data.message || '获取统计数据失败');
        }
      } catch (error) {
        console.error('获取统计数据失败:', error);
        this.$message.error('获取统计数据失败: ' + (error.response?.data?.message || error.message));
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.loadStatistics()
    },
    handleReset() {
      this.filterForm = {
        className: '',
        sportsItemId: '',
        status: 'APPROVED'  // 重置时恢复默认值
      };
      this.loadStatistics()
    },
    calculateRate(count, total) {
      if (!total) return '0.00'
      return ((count / total) * 100).toFixed(2)
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
</style> 