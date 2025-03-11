<template>
  <div>
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态">
            <el-option label="全部" value=""></el-option>
            <el-option label="待审核" value="PENDING"></el-option>
            <el-option label="已通过" value="APPROVED"></el-option>
            <el-option label="已驳回" value="REJECTED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="体测项目">
          <el-select v-model="filterForm.sportsItemId" placeholder="选择项目">
            <el-option label="全部" value=""></el-option>
            <el-option v-for="item in sportsItems" 
                      :key="item.id" 
                      :label="item.name" 
                      :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchRecords">查询</el-button>
          <el-button type="success" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="records" border>
      <el-table-column prop="student.realName" label="学生姓名"></el-table-column>
      <el-table-column prop="student.studentNumber" label="学号"></el-table-column>
      <el-table-column prop="sportsItem.name" label="测试项目"></el-table-column>
      <el-table-column prop="score" label="成绩"></el-table-column>
      <el-table-column prop="testTime" label="测试时间">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.testTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template slot-scope="scope">
          {{ getStatusText(scope.row.status) }}
        </template>
      </el-table-column>
      <el-table-column prop="reviewComment" label="审核意见"></el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button size="mini" 
                     type="primary" 
                     @click="handleReview(scope.row)"
                     v-if="scope.row.status === 'PENDING'">
            审核
          </el-button>
          <el-button size="mini" 
                     type="warning" 
                     @click="handleModify(scope.row)"
                     v-if="scope.row.status !== 'PENDING'">
            修改
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      @current-change="handlePageChange"
      :current-page.sync="currentPage"
      :page-size="pageSize"
      layout="total, prev, pager, next"
      :total="total">
    </el-pagination>

    <!-- 审核对话框 -->
    <test-record-review
      :visible.sync="reviewDialogVisible"
      :record="currentRecord"
      @success="handleReviewSuccess">
    </test-record-review>

    <!-- 修改对话框 -->
    <el-dialog
      title="修改审核结果"
      :visible.sync="modifyDialogVisible"
      width="500px">
      <el-form :model="modifyForm" ref="modifyForm" label-width="100px">
        <el-form-item label="审核状态" prop="status">
          <el-radio-group v-model="modifyForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="comment">
          <el-input
            type="textarea"
            v-model="modifyForm.comment"
            :rows="3"
            placeholder="请输入审核意见">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="modifyDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitModify">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { formatDateTime } from '@/utils/datetime'
import TestRecordReview from './TestRecordReview.vue'

export default {
  components: {
    TestRecordReview
  },
  data() {
    return {
      records: [],
      sportsItems: [],
      filterForm: {
        status: '',
        sportsItemId: ''
      },
      currentPage: 1,
      pageSize: 10,
      total: 0,
      reviewDialogVisible: false,
      modifyDialogVisible: false,
      currentRecord: null,
      modifyForm: {
        status: '',
        comment: ''
      }
    }
  },
  methods: {
    formatDateTime,
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
      }
      return statusMap[status] || status
    },
    async fetchRecords() {
      try {
        const response = await this.$axios.get('/api/admin/test-record/review', {
          params: {
            status: this.filterForm.status,
            sportsItemId: this.filterForm.sportsItemId,
            pageNum: this.currentPage,
            pageSize: this.pageSize
          }
        })
        if (response.data.code === 200) {
          this.records = response.data.data.content
          this.total = response.data.data.totalElements
        }
      } catch (error) {
        this.$message.error('获取记录失败：' + error.message)
      }
    },
    async fetchSportsItems() {
      try {
        const response = await this.$axios.get('/api/admin/sports-items')
        if (response.data.code === 200) {
          this.sportsItems = response.data.data
        }
      } catch (error) {
        this.$message.error('获取体测项目失败：' + error.message)
      }
    },
    handlePageChange(page) {
      this.currentPage = page
      this.fetchRecords()
    },
    handleReview(record) {
      this.currentRecord = record
      this.reviewDialogVisible = true
    },
    handleReviewSuccess() {
      this.fetchRecords()
    },
    handleModify(record) {
      this.currentRecord = record
      this.modifyForm.status = record.status
      this.modifyForm.comment = record.reviewComment
      this.modifyDialogVisible = true
    },
    async submitModify() {
      try {
        const response = await this.$axios.put('/api/admin/test-record/modify', {
          id: this.currentRecord.id,
          status: this.modifyForm.status,
          comment: this.modifyForm.comment,
          reviewerId: this.$store.state.user.id
        })
        
        if (response.data.code === 200) {
          this.$message.success('修改成功')
          this.modifyDialogVisible = false
          this.fetchRecords()
        } else {
          this.$message.error(response.data.msg || '修改失败')
        }
      } catch (error) {
        this.$message.error('修改失败：' + error.message)
      }
    },
    async handleExport() {
      try {
        const response = await this.$axios.get('/api/admin/test-record/export', {
          params: {
            status: this.filterForm.status,
            sportsItemId: this.filterForm.sportsItemId
          },
          responseType: 'blob'
        })
        
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'test_records.xlsx')
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      } catch (error) {
        this.$message.error('导出失败：' + error.message)
      }
    }
  },
  created() {
    this.fetchSportsItems()
    this.fetchRecords()
  }
}
</script>

<style scoped>
.filter-section {
  margin-bottom: 20px;
}
</style> 