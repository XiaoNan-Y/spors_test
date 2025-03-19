<template>
  <div class="test-records">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>测试记录</span>
      </div>
      
      <el-form :inline="true" class="search-form">
        <el-form-item label="测试项目">
          <el-select v-model="query.sportsItemId" placeholder="全部" clearable>
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchRecords">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="records" style="width: 100%" v-loading="loading">
        <el-table-column prop="sportsItem.name" label="测试项目" />
        <el-table-column prop="score" label="成绩" />
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

      <div class="pagination-container">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="query.page"
          :page-sizes="[10, 20, 50]"
          :page-size="query.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { formatDate } from '@/utils/date'

export default {
  name: 'StudentTestRecords',
  data() {
    return {
      records: [],
      sportsItems: [],
      loading: false,
      total: 0,
      query: {
        page: 1,
        size: 10,
        sportsItemId: null
      }
    }
  },
  created() {
    this.fetchSportsItems()
    this.fetchRecords()
  },
  methods: {
    async fetchSportsItems() {
      try {
        const response = await this.$http.get('/api/sports-items')
        if (response.data.code === 200) {
          this.sportsItems = response.data.data
        }
      } catch (error) {
        console.error('获取体育项目失败:', error)
        this.$message.error('获取体育项目失败')
      }
    },
    async fetchRecords() {
      this.loading = true
      try {
        const response = await this.$http.get('/api/student/test-records', {
          params: {
            page: this.query.page - 1,
            size: this.query.size,
            sportsItemId: this.query.sportsItemId
          }
        })
        if (response.data.code === 200) {
          this.records = response.data.data.content
          this.total = response.data.data.totalElements
        }
      } catch (error) {
        console.error('获取测试记录失败:', error)
        this.$message.error('获取测试记录失败')
      } finally {
        this.loading = false
      }
    },
    handleSizeChange(val) {
      this.query.size = val
      this.fetchRecords()
    },
    handleCurrentChange(val) {
      this.query.page = val
      this.fetchRecords()
    },
    formatDate,
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
.test-records {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 