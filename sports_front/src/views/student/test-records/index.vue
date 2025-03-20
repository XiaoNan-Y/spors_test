<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-container">
      <el-table
        v-loading="loading"
        :data="recordList"
        border
        style="width: 100%"
      >
        <el-table-column
          prop="createTime"
          label="测试时间"
          width="180"
        >
          <template slot-scope="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="sportsItemName"
          label="体育项目"
          width="150"
        />
        <el-table-column
          prop="score"
          label="成绩"
          width="100"
        />
        <el-table-column
          prop="grade"
          label="等级"
          width="100"
        >
          <template slot-scope="scope">
            <el-tag :type="getGradeType(scope.row.grade)">
              {{ scope.row.grade }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          width="100"
        >
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="reviewComment"
          label="审核意见"
        />
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="queryParams.page"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="queryParams.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getTestRecords } from '@/api/student'

export default {
  name: 'StudentTestRecords',
  data() {
    return {
      loading: false,
      total: 0,
      recordList: [],
      queryParams: {
        page: 1,
        size: 10,
        status: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      try {
        this.loading = true
        const params = {
          ...this.queryParams,
          page: this.queryParams.page - 1
        }
        const response = await getTestRecords(params)
        this.recordList = response.data.content
        this.total = response.data.totalElements
      } catch (error) {
        console.error('获取体测记录失败:', error)
        this.$message.error('获取体测记录失败')
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.queryParams.page = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = {
        page: 1,
        size: 10,
        status: undefined
      }
      this.getList()
    },
    handleSizeChange(val) {
      this.queryParams.size = val
      this.getList()
    },
    handleCurrentChange(val) {
      this.queryParams.page = val
      this.getList()
    },
    getStatusType(status) {
      const statusMap = {
        PENDING: 'warning',
        APPROVED: 'success',
        REJECTED: 'danger'
      }
      return statusMap[status] || 'info'
    },
    getStatusText(status) {
      const statusMap = {
        PENDING: '待审核',
        APPROVED: '已通过',
        REJECTED: '已驳回'
      }
      return statusMap[status] || status
    },
    getGradeType(grade) {
      const gradeMap = {
        'A': 'success',
        'B': 'primary',
        'C': 'warning',
        'D': 'danger'
      }
      return gradeMap[grade] || 'info'
    },
    formatDate(date) {
      if (!date) return '';
      const dateObj = new Date(date);
      const year = dateObj.getFullYear();
      const month = String(dateObj.getMonth() + 1).padStart(2, '0');
      const day = String(dateObj.getDate()).padStart(2, '0');
      const hour = String(dateObj.getHours()).padStart(2, '0');
      const minute = String(dateObj.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day} ${hour}:${minute}`;
    }
  }
}
</script>

<style scoped>
.filter-container {
  margin-bottom: 20px;
}
.table-container {
  margin-top: 20px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 