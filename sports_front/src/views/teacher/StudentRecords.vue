<template>
  <div class="student-records">
    <!-- 搜索筛选区 -->
    <div class="filter-section">
      <el-form :inline="true" :model="queryParams" ref="queryForm">
        <el-form-item label="班级">
          <el-select v-model="queryParams.className" placeholder="选择班级" clearable>
            <el-option
              v-for="className in classNames"
              :key="className"
              :label="className"
              :value="className"
            ></el-option>
          </el-select>
        </el-form-item>
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
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
            <el-option label="待审核" value="PENDING"></el-option>
            <el-option label="已通过" value="APPROVED"></el-option>
            <el-option label="已驳回" value="REJECTED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学号">
          <el-input 
            v-model="queryParams.studentNumber" 
            placeholder="请输入学号"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      style="width: 100%"
      border>
      <el-table-column type="index" label="序号" width="60"></el-table-column>
      <el-table-column prop="studentInfo.realName" label="学生姓名"></el-table-column>
      <el-table-column prop="studentInfo.studentNumber" label="学号"></el-table-column>
      <el-table-column prop="studentInfo.className" label="班级"></el-table-column>
      <el-table-column prop="sportsItem.name" label="测试项目"></el-table-column>
      <el-table-column prop="score" label="成绩"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
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
  </div>
</template>

<script>
export default {
  name: 'StudentRecords',
  data() {
    return {
      loading: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        className: '',
        sportsItemId: undefined,
        status: '',
        studentNumber: ''
      },
      classNames: [],
      sportsItems: [],
      tableData: [],
      total: 0
    }
  },
  created() {
    this.init()
  },
  methods: {
    async init() {
      await Promise.all([
        this.getClassNames(),
        this.getSportsItems(),
        this.getList()
      ])
    },
    async getList() {
      try {
        this.loading = true
        const response = await this.$axios.get('/api/teacher/student-records', {
          params: {
            page: this.queryParams.pageNum - 1,
            size: this.queryParams.pageSize,
            className: this.queryParams.className,
            sportsItemId: this.queryParams.sportsItemId,
            status: this.queryParams.status,
            keyword: this.queryParams.studentNumber
          }
        })
        
        if (response.data.code === 200) {
          this.tableData = response.data.data.content
          this.total = response.data.data.totalElements
        }
      } catch (error) {
        console.error('获取列表失败:', error)
        this.$message.error('获取列表失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    async getClassNames() {
      try {
        const res = await this.$axios.get('/api/teacher/classes')
        if (res.data.code === 200) {
          this.classNames = res.data.data
        }
      } catch (error) {
        console.error('获取班级列表失败:', error)
      }
    },
    async getSportsItems() {
      try {
        const res = await this.$axios.get('/api/teacher/sports-items')
        if (res.data.code === 200) {
          this.sportsItems = res.data.data
        }
      } catch (error) {
        console.error('获取体测项目列表失败:', error)
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.$refs.queryForm.resetFields()
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        className: '',
        sportsItemId: undefined,
        status: '',
        studentNumber: ''
      }
      this.getList()
    },
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.getList()
    },
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.getList()
    },
    getStatusType(status) {
      const statusMap = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return statusMap[status] || 'info'
    },
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
      }
      return statusMap[status] || status
    }
  }
}
</script>

<style scoped>
.student-records {
  padding: 20px;
}
.filter-section {
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 