<template>
  <div class="test-records">
    <div class="filter-container">
      <el-select v-model="status" placeholder="状态" clearable>
        <el-option label="全部" value=""></el-option>
        <el-option label="待审核" value="PENDING"></el-option>
        <el-option label="已通过" value="APPROVED"></el-option>
        <el-option label="已拒绝" value="REJECTED"></el-option>
      </el-select>
      <el-button type="primary" @click="getTestRecords">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <el-table :data="records" border style="width: 100%">
      <el-table-column prop="createdAt" label="测试时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column prop="sportsItemName" label="体育项目"></el-table-column>
      <el-table-column prop="score" label="成绩"></el-table-column>
      <el-table-column prop="grade" label="等级"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewComment" label="审核意见"></el-table-column>
    </el-table>

    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="[10, 20, 50]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total">
    </el-pagination>
  </div>
</template>

<script>
export default {
  name: 'TestRecords',
  data() {
    return {
      status: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      records: [],
      loading: false
    }
  },
  created() {
    // 组件创建时获取数据
    this.getTestRecords()
  },
  methods: {
    async getTestRecords() {
      try {
        this.loading = true
        const userId = localStorage.getItem('userId')
        
        // 添加userId检查
        if (!userId) {
          this.$message.error('未登录或用户信息缺失')
          this.$router.push('/login')
          return
        }
        
        // 打印请求参数
        console.log('请求参数:', {
          status: this.status,
          page: this.currentPage,
          size: this.pageSize,
          userId: userId
        })

        const response = await this.$axios.get('/api/student/test-records', {
          params: {
            status: this.status,
            page: this.currentPage,
            size: this.pageSize
          },
          headers: {
            userId: userId
          }
        })

        console.log('获取到的记录数据:', response.data.data.content)

        if (response.data.code === 200) {
          this.records = response.data.data.content
          this.total = response.data.data.totalElements
        } else {
          this.$message.error(response.data.message || '获取数据失败')
        }
      } catch (error) {
        console.error('获取测试记录失败:', error)
        if (typeof error === 'object' && error.message) {
          this.$message.error(`获取测试记录失败: ${error.message}`)
        } else {
          this.$message.error(`获取测试记录失败: ${error}`)
        }
      } finally {
        this.loading = false
      }
    },
    handleSizeChange(val) {
      console.log('改变每页数量:', val)
      this.pageSize = val
      this.getTestRecords()
    },
    handleCurrentChange(val) {
      console.log('改变页码:', val)
      this.currentPage = val
      this.getTestRecords()
    },
    resetQuery() {
      this.status = ''
      this.currentPage = 1
      this.getTestRecords()
    },
    getStatusType(status) {
      const types = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已拒绝'
      }
      return texts[status] || status
    },
    formatDateTime(datetime) {
      if (!datetime) return '';
      const date = new Date(datetime);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    }
  }
}
</script>

<style scoped>
.test-records {
  padding: 20px;
}
.filter-container {
  margin-bottom: 20px;
}
.filter-container > * {
  margin-right: 10px;
}
</style> 