<template>
  <div class="exemption-review">
    <div class="header">
      <h2>免测/重测申请审核</h2>
      <div class="actions">
        <el-button type="warning" @click="fixStudentNames">
          <i class="el-icon-refresh"></i> 修复学生信息
        </el-button>
      </div>
    </div>

    <div class="filters">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="申请类型">
          <el-select v-model="filters.type" placeholder="选择申请类型" clearable>
            <el-option label="免测" value="EXEMPTION" />
            <el-option label="重测" value="RETEST" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="选择状态" clearable>
            <el-option label="待教师审核" value="PENDING_TEACHER" />
            <el-option label="待管理员审核" value="PENDING_ADMIN" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="教师已驳回" value="REJECTED_TEACHER" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model="filters.keyword"
            placeholder="学号/姓名"
            clearable
            @keyup.enter.native="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table
      v-loading="loading"
      :data="records"
      border
      style="width: 100%"
    >
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column prop="studentNumber" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column prop="className" label="班级" width="120" />
      <el-table-column prop="type" label="申请类型" width="100">
        <template slot-scope="scope">
          {{ scope.row.type === 'EXEMPTION' ? '免测' : '重测' }}
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="申请原因" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="120">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="teacherReviewComment" label="教师审核意见" show-overflow-tooltip />
      <el-table-column prop="adminReviewComment" label="管理员审核意见" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="申请时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.applyTime || scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="handleReview(scope.row)"
            v-if="scope.row.status === 'PENDING_ADMIN'"
          >审核</el-button>
          <el-button
            size="mini"
            type="warning"
            @click="handleModify(scope.row)"
          >修改</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page.sync="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
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
      <el-form :model="reviewForm" ref="reviewForm" label-width="100px">
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="reviewForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="adminReviewComment">
          <el-input
            type="textarea"
            v-model="reviewForm.adminReviewComment"
            :rows="4"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="reviewDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确定</el-button>
      </div>
    </el-dialog>

    <!-- 修改对话框 -->
    <el-dialog
      title="修改审核状态"
      :visible.sync="modifyDialog.visible"
      width="500px"
    >
      <el-form :model="modifyForm" ref="modifyForm" label-width="100px">
        <el-form-item label="审核状态" prop="status">
          <el-select v-model="modifyForm.status" placeholder="选择状态">
            <el-option label="待教师审核" value="PENDING_TEACHER" />
            <el-option label="待管理员审核" value="PENDING_ADMIN" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="教师已驳回" value="REJECTED_TEACHER" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核意见" prop="adminReviewComment">
          <el-input
            type="textarea"
            v-model="modifyForm.adminReviewComment"
            :rows="4"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="modifyDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitModify">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ExemptionReview',
  data() {
    return {
      loading: false,
      records: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      filters: {
        type: undefined,
        status: undefined,
        keyword: ''
      },
      reviewDialog: {
        visible: false
      },
      reviewForm: {
        status: '',
        adminReviewComment: ''
      },
      currentRecord: null,
      modifyDialog: {
        visible: false
      },
      modifyForm: {
        status: '',
        adminReviewComment: ''
      }
    }
  },
  created() {
    this.fetchRecords()
  },
  methods: {
    async fetchRecords() {
      try {
        const response = await this.$axios.get('/api/admin/exemptions', {
          params: {
            keyword: this.filters.keyword,
            page: this.currentPage - 1,
            size: this.pageSize
          }
        });
        
        if (response.data.code === 200) {
          this.records = response.data.data.content;
          this.total = response.data.data.totalElements;
        } else {
          this.$message.error(response.data.message);
        }
      } catch (error) {
        console.error('获取记录失败:', error);
        this.$message.error('获取记录失败: ' + error.message);
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchRecords()
    },
    resetFilters() {
      this.filters = {
        type: undefined,
        status: undefined,
        keyword: ''
      }
      this.handleSearch()
    },
    getStatusType(status) {
      const types = {
        PENDING_TEACHER: 'warning',
        PENDING_ADMIN: 'warning',
        APPROVED: 'success',
        REJECTED: 'danger',
        REJECTED_TEACHER: 'danger'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        PENDING_TEACHER: '待教师审核',
        PENDING_ADMIN: '待管理员审核',
        APPROVED: '已通过',
        REJECTED: '已驳回',
        REJECTED_TEACHER: '教师已驳回'
      }
      return texts[status] || status
    },
    formatDateTime(datetime) {
      if (!datetime) return ''
      return new Date(datetime).toLocaleString()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchRecords()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchRecords()
    },
    handleReview(row) {
      this.currentRecord = row
      this.reviewForm = {
        status: '',
        adminReviewComment: ''
      }
      this.reviewDialog.visible = true
    },
    async submitReview() {
      if (!this.reviewForm.status) {
        this.$message.warning('请选择审核结果')
        return
      }
      
      try {
        const response = await this.$http.put(
          `/api/exemptions/${this.currentRecord.id}/review`,
          {
            status: this.reviewForm.status,
            adminReviewComment: this.reviewForm.adminReviewComment,
            adminReviewTime: new Date().toISOString()
          }
        )
        
        if (response.data.code === 200) {
          this.$message.success('审核成功')
          this.reviewDialog.visible = false
          this.fetchRecords()
        } else {
          this.$message.error(response.data.msg || '审核失败')
        }
      } catch (error) {
        console.error('审核失败:', error)
        this.$message.error('审核失败')
      }
    },
    async fixStudentNames() {
      try {
        const response = await this.$http.post('/api/data-fix/fix-exemption-names')
        if (response.data.code === 200) {
          this.$message.success(response.data.data || '修复成功')
          // 重新加载数据
          this.fetchRecords()
        } else {
          this.$message.error(response.data.msg || '修复失败')
        }
      } catch (error) {
        console.error('修复失败:', error)
        this.$message.error('修复失败')
      }
    },
    handleModify(row) {
      this.currentRecord = row
      this.modifyForm = {
        status: row.status,
        adminReviewComment: row.adminReviewComment || ''
      }
      this.modifyDialog.visible = true
    },
    async submitModify() {
      try {
        const response = await this.$http.put(
          `/api/exemptions/${this.currentRecord.id}/modify`,
          {
            status: this.modifyForm.status,
            adminReviewComment: this.modifyForm.adminReviewComment
          }
        )
        
        if (response.data.code === 200) {
          this.$message.success('修改成功')
          this.modifyDialog.visible = false
          this.fetchRecords()
        } else {
          this.$message.error(response.data.msg || '修改失败')
        }
      } catch (error) {
        console.error('修改失败:', error)
        this.$message.error('修改失败')
      }
    }
  }
}
</script>

<style scoped>
.exemption-review {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.actions {
  display: flex;
  gap: 10px;
}

.filters {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 