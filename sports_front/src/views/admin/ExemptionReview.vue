<template>
  <div class="exemption-review">
    <div class="header">
      <h2>免测申请审核</h2>
    </div>

    <div class="filters">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
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
          <el-button type="success" @click="handleExport">导出数据</el-button>
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
      <el-table-column prop="reason" label="申请原因" show-overflow-tooltip />
      <el-table-column label="证明材料" width="120">
        <template slot-scope="scope">
          <el-button 
            v-if="scope.row.attachmentUrl"
            type="text" 
            @click="previewAttachment(scope.row.attachmentUrl)"
          >
            查看附件
          </el-button>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="adminReviewComment" label="审核意见" show-overflow-tooltip />
      <el-table-column prop="applyTime" label="申请时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.applyTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.status === 'PENDING'"
            size="mini"
            type="primary"
            @click="handleReview(scope.row)"
          >审核</el-button>
          <el-button
            v-else
            size="mini"
            type="info"
            @click="handleDetail(scope.row)"
          >查看</el-button>
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
      <div class="review-info">
        <p><strong>学生信息：</strong>{{ currentRecord?.studentName }} ({{ currentRecord?.studentNumber }})</p>
        <p><strong>班级：</strong>{{ currentRecord?.className }}</p>
        <p><strong>申请原因：</strong>{{ currentRecord?.reason }}</p>
        <p v-if="currentRecord?.attachmentUrl">
          <strong>证明材料：</strong>
          <el-button type="text" @click="previewAttachment(currentRecord.attachmentUrl)">
            查看附件
          </el-button>
        </p>
      </div>
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

    <!-- 详情对话框 -->
    <el-dialog
      title="申请详情"
      :visible.sync="detailDialog.visible"
      width="500px"
    >
      <div class="detail-info">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="学生姓名">{{ currentRecord?.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentRecord?.studentNumber }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentRecord?.className }}</el-descriptions-item>
          <el-descriptions-item label="申请原因">{{ currentRecord?.reason }}</el-descriptions-item>
          <el-descriptions-item label="证明材料">
            <el-button 
              v-if="currentRecord?.attachmentUrl"
              type="text" 
              @click="previewAttachment(currentRecord.attachmentUrl)"
            >
              查看附件
            </el-button>
            <span v-else>无</span>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDateTime(currentRecord?.applyTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentRecord?.status)">
              {{ getStatusText(currentRecord?.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核意见">{{ currentRecord?.adminReviewComment || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ formatDateTime(currentRecord?.adminReviewTime) || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialog.visible = false">关闭</el-button>
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
        status: '',
        keyword: ''
      },
      reviewDialog: {
        visible: false
      },
      detailDialog: {
        visible: false
      },
      reviewForm: {
        status: '',
        adminReviewComment: ''
      },
      currentRecord: null
    }
  },
  created() {
    this.fetchRecords()
  },
  methods: {
    async fetchRecords() {
      try {
        this.loading = true
        const response = await this.$axios.get('/api/exemptions/admin/list', {
          params: {
            status: this.filters.status,
            keyword: this.filters.keyword,
            page: this.currentPage - 1,
            size: this.pageSize
          }
        })
        
        if (response.data.code === 200) {
          this.records = response.data.data.content
          this.total = response.data.data.totalElements
        } else {
          this.$message.error(response.data.message)
        }
      } catch (error) {
        console.error('获取记录失败:', error)
        this.$message.error('获取记录失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchRecords()
    },
    resetFilters() {
      this.filters = {
        status: '',
        keyword: ''
      }
      this.handleSearch()
    },
    getStatusType(status) {
      const types = {
        PENDING: 'warning',
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
    },
    formatDateTime(datetime) {
      if (!datetime) return '-'
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
    handleDetail(row) {
      this.currentRecord = row
      this.detailDialog.visible = true
    },
    async submitReview() {
      if (!this.reviewForm.status) {
        this.$message.warning('请选择审核结果')
        return
      }
      
      try {
        const response = await this.$axios.post(
          `/api/exemptions/admin/review/${this.currentRecord.id}`,
          {
            status: this.reviewForm.status,
            comment: this.reviewForm.adminReviewComment,
            reviewerId: localStorage.getItem('userId')
          }
        )
        
        if (response.data.code === 200) {
          this.$message.success('审核成功')
          this.reviewDialog.visible = false
          this.fetchRecords()
        } else {
          this.$message.error(response.data.message || '审核失败')
        }
      } catch (error) {
        console.error('审核失败:', error)
        this.$message.error('审核失败: ' + error.message)
      }
    },
    previewAttachment(url) {
      if (!url) return
      window.open(url, '_blank')
    },
    async handleExport() {
      try {
        // 使用原生 fetch 处理文件下载
        const response = await fetch('/api/exemptions/admin/export', {
          headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
          }
        });
        
        if (!response.ok) {
          throw new Error('导出失败');
        }
        
        // 获取文件名
        const contentDisposition = response.headers.get('content-disposition');
        let filename = '免测申请数据.xlsx';
        if (contentDisposition) {
          const filenameMatch = contentDisposition.match(/filename="(.+)"/);
          if (filenameMatch) {
            filename = decodeURIComponent(filenameMatch[1]);
          }
        }
        
        // 下载文件
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        
        this.$message.success('导出成功');
      } catch (error) {
        console.error('导出失败:', error);
        this.$message.error('导出失败: ' + error.message);
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
  margin-bottom: 20px;
}

.filters {
  margin-bottom: 20px;
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.review-info {
  background-color: #f5f7fa;
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 4px;
}

.review-info p {
  margin: 8px 0;
  line-height: 1.5;
}

.detail-info {
  padding: 10px;
}
</style> 