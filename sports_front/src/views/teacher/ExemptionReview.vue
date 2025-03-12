<template>
  <div class="exemption-review">
    <!-- 搜索筛选区 -->
    <div class="filter-section">
      <el-form :inline="true" :model="queryParams" ref="queryForm">
        <el-form-item label="申请类型">
          <el-select v-model="queryParams.type" placeholder="选择申请类型" clearable>
            <el-option label="免测申请" value="EXEMPTION"></el-option>
            <el-option label="重测申请" value="RETEST"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
            <el-option label="待审核" value="PENDING_TEACHER"></el-option>
            <el-option label="已通过" value="APPROVED"></el-option>
            <el-option label="已驳回" value="REJECTED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="queryParams.studentNumber" placeholder="请输入学号" clearable></el-input>
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
      border
      stripe
      style="width: 100%"
    >
      <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
      <el-table-column label="申请类型" prop="type" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.type === 'EXEMPTION' ? 'warning' : 'info'">
            {{ scope.row.type === 'EXEMPTION' ? '免测' : '重测' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="学生姓名" prop="studentName" align="center"></el-table-column>
      <el-table-column label="学号" prop="studentNumber" align="center"></el-table-column>
      <el-table-column label="申请原因" prop="reason" align="center"></el-table-column>
      <el-table-column label="申请时间" prop="applyTime" align="center">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.applyTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" align="center">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.status === 'PENDING_TEACHER'"
            size="mini"
            type="primary"
            @click="handleReview(scope.row)"
          >审核</el-button>
          <el-button
            size="mini"
            type="info"
            @click="handleDetail(scope.row)"
          >详情</el-button>
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

    <!-- 审核对话框 -->
    <el-dialog
      title="审核申请"
      :visible.sync="reviewDialog.visible"
      width="500px"
    >
      <el-form :model="reviewForm" ref="reviewForm" :rules="reviewRules" label-width="100px">
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="reviewForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="comment">
          <el-input
            type="textarea"
            v-model="reviewForm.comment"
            :rows="4"
            placeholder="请输入审核意见"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="reviewDialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="submitReview">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getExemptionList, teacherReview } from '@/api/exemption'

export default {
  name: 'TeacherExemptionReview',
  data() {
    return {
      loading: false,
      total: 0,
      queryParams: {
        type: '',
        status: '',
        studentNumber: '',
        pageNum: 1,
        pageSize: 10
      },
      tableData: [],
      reviewDialog: {
        visible: false
      },
      reviewForm: {
        id: null,
        status: '',
        comment: ''
      },
      reviewRules: {
        status: [
          { required: true, message: '请选择审核结果', trigger: 'change' }
        ],
        comment: [
          { required: true, message: '请输入审核意见', trigger: 'blur' }
        ]
      },
      currentRecord: null
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      try {
        this.loading = true
        const res = await getExemptionList(this.queryParams)
        this.tableData = res.data.content
        this.total = res.data.totalElements
      } catch (error) {
        console.error('获取列表失败:', error)
        this.$message.error('获取列表失败')
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = {
        type: '',
        status: '',
        studentNumber: '',
        pageNum: 1,
        pageSize: 10
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
    handleReview(row) {
      this.currentRecord = row
      this.reviewForm = {
        id: row.id,
        status: '',
        comment: ''
      }
      this.reviewDialog.visible = true
    },
    async submitReview() {
      this.$refs.reviewForm.validate(async (valid) => {
        if (valid) {
          try {
            await teacherReview({
              id: this.currentRecord.id,
              status: this.reviewForm.status,
              teacherReviewComment: this.reviewForm.comment
            })
            this.$message.success('审核成功')
            this.reviewDialog.visible = false
            this.getList()
          } catch (error) {
            console.error('审核失败:', error)
            this.$message.error('审核失败')
          }
        }
      })
    },
    getStatusType(status) {
      const statusMap = {
        'PENDING_TEACHER': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return statusMap[status] || ''
    },
    getStatusText(status) {
      const statusMap = {
        'PENDING_TEACHER': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
      }
      return statusMap[status] || status
    },
    formatDateTime(datetime) {
      if (!datetime) return '-'
      const date = new Date(datetime)
      return date.toLocaleString()
    }
  }
}
</script>

<style scoped>
.exemption-review {
  padding: 20px;
}

.filter-section {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 