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
            <el-option label="待审核" value="PENDING"></el-option>
            <el-option label="已通过" value="APPROVED"></el-option>
            <el-option label="已驳回" value="REJECTED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="queryParams.className" placeholder="选择班级" clearable>
            <el-option
              v-for="className in classList"
              :key="className"
              :label="className"
              :value="className"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="学生姓名/学号"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="handleExport">导出</el-button>
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
      <el-table-column label="学生姓名" prop="studentName" min-width="100" align="center"></el-table-column>
      <el-table-column label="学号" prop="studentNumber" min-width="120" align="center"></el-table-column>
      <el-table-column label="班级" prop="className" min-width="100" align="center"></el-table-column>
      <el-table-column label="测试项目" min-width="120" align="center">
        <template slot-scope="scope">
          {{ scope.row.sportsItem ? scope.row.sportsItem.name : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="申请类型" min-width="100" align="center">
        <template slot-scope="scope">
          {{ scope.row.type === 'EXEMPTION' ? '免测' : '重测' }}
        </template>
      </el-table-column>
      <el-table-column label="申请原因" prop="reason" min-width="200" show-overflow-tooltip></el-table-column>
      <el-table-column label="状态" min-width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="250">
        <template slot-scope="scope">
          <el-button
            v-if="canReview(scope.row)"
            size="mini"
            type="primary"
            @click="handleReview(scope.row)"
          >审核</el-button>
          <el-button
            v-if="canModify(scope.row)"
            size="mini"
            type="warning"
            @click="handleModify(scope.row)"
          >修改</el-button>
          <el-button
            size="mini"
            type="info"
            @click="handleDetail(scope.row)"
          >查看</el-button>
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
        :total="total"
      ></el-pagination>
    </div>

    <!-- 审核对话框 -->
    <el-dialog title="申请审核" :visible.sync="reviewDialog.visible" width="500px">
      <div class="review-info">
        <p>学生：{{ currentRecord?.student?.realName }} ({{ currentRecord?.studentNumber }})</p>
        <p>班级：{{ currentRecord?.className }}</p>
        <p>项目：{{ currentRecord?.sportsItem?.name }}</p>
        <p>类型：{{ currentRecord?.type === 'EXEMPTION' ? '免测' : '重测' }}</p>
        <p>原因：{{ currentRecord?.reason }}</p>
      </div>
      <el-form :model="reviewForm" :rules="reviewRules" ref="reviewForm" label-width="80px">
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
            :rows="3"
            placeholder="请输入审核意见"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="reviewDialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="submitReview">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="申请详情" :visible.sync="detailDialog.visible" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="学生姓名">{{ currentRecord?.student?.realName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRecord?.studentNumber }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ currentRecord?.className }}</el-descriptions-item>
        <el-descriptions-item label="测试项目">{{ currentRecord?.sportsItem?.name }}</el-descriptions-item>
        <el-descriptions-item label="申请类型">{{ currentRecord?.type === 'EXEMPTION' ? '免测' : '重测' }}</el-descriptions-item>
        <el-descriptions-item label="申请原因">{{ currentRecord?.reason }}</el-descriptions-item>
        <el-descriptions-item label="审核结果">
          <el-tag :type="getStatusType(currentRecord?.status)">
            {{ getStatusText(currentRecord?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见">{{ currentRecord?.reviewComment }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ formatDateTime(currentRecord?.reviewTime) }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialog.visible = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 修改对话框 -->
    <el-dialog
      title="修改审核"
      :visible.sync="modifyDialog.visible"
      width="500px"
    >
      <div class="review-info">
        <p>学生：{{ currentRecord?.studentName }} ({{ currentRecord?.studentNumber }})</p>
        <p>班级：{{ currentRecord?.className }}</p>
        <p>申请类型：{{ currentRecord?.type === 'EXEMPTION' ? '免测申请' : '重测申请' }}</p>
        <p>申请原因：{{ currentRecord?.reason }}</p>
        <p>原审核意见：{{ currentRecord?.teacherReviewComment }}</p>
        <p>原审核时间：{{ formatDateTime(currentRecord?.teacherReviewTime) }}</p>
      </div>
      <el-form :model="modifyForm" :rules="reviewRules" ref="modifyForm" label-width="100px">
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="modifyForm.status">
            <el-radio label="PENDING">待审核</el-radio>
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="comment">
          <el-input
            type="textarea"
            v-model="modifyForm.comment"
            :rows="3"
            placeholder="请输入新的审核意见"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="modifyDialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="submitModify">确 定</el-button>
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
      tableData: [],
      total: 0,
      queryParams: {
        type: '',
        status: '',
        className: '',
        keyword: '',
        pageNum: 1,
        pageSize: 10
      },
      classList: [],
      reviewDialog: {
        visible: false
      },
      detailDialog: {
        visible: false
      },
      currentRecord: null,
      reviewForm: {
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
      modifyDialog: {
        visible: false
      },
      modifyForm: {
        id: null,
        status: '',
        comment: ''
      }
    }
  },
  created() {
    this.init()
  },
  methods: {
    async init() {
      await Promise.all([
        this.getClassList(),
        this.getList()
      ])
    },
    async getList() {
      this.loading = true
      try {
        const params = {
          className: this.queryParams.className,
          type: this.queryParams.type,
          status: this.queryParams.status,
          keyword: this.queryParams.keyword,
          page: this.queryParams.pageNum - 1,
          size: this.queryParams.pageSize
        }
        console.log('Request params:', params)

        const response = await this.$http.get('/api/teacher/exemption-applications', { params })
        
        if (response.data.code === 200) {
          const { content, totalElements } = response.data.data || {}
          this.tableData = content || []
          this.total = totalElements || 0
          
          console.log('Loaded records:', this.tableData)
          console.log('Total elements:', this.total)
        } else {
          this.$message.error(response.data.msg || '获取数据失败')
        }
      } catch (error) {
        console.error('获取记录失败:', error)
        this.$message.error('获取记录失败')
      } finally {
        this.loading = false
      }
    },
    async getClassList() {
      try {
        const res = await this.$http.get('/api/teacher/classes')
        if (res.data.code === 200) {
          this.classList = res.data.data || []
        }
      } catch (error) {
        console.error('获取班级列表失败:', error)
        this.$message.error('获取班级列表失败')
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
        type: '',
        status: '',
        className: '',
        keyword: ''
      }
      this.getList()
    },
    handleReview(row) {
      this.currentRecord = row
      this.reviewForm = {
        status: '',
        comment: ''
      }
      this.reviewDialog.visible = true
    },
    handleDetail(row) {
      this.currentRecord = row
      this.detailDialog.visible = true
    },
    async submitReview() {
      this.$refs.reviewForm.validate(async valid => {
        if (valid) {
          try {
            const res = await this.$http.put(`/api/teacher/exemptions/${this.currentRecord.id}/review`, {
              status: this.reviewForm.status,
              comment: this.reviewForm.comment
            })
            
            if (res.data.code === 200) {
              this.$message.success('审核成功')
              this.reviewDialog.visible = false
              this.getList()
            } else {
              this.$message.error(res.data.message || '审核失败')
            }
          } catch (error) {
            console.error('审核失败:', error)
            this.$message.error('审核失败')
          }
        }
      })
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
      return statusMap[status] || '未知'
    },
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      })
    },
    canReview(row) {
      return row.status === 'PENDING'
    },
    canModify(row) {
      return row.status !== 'PENDING'
    },
    handleModify(row) {
      this.currentRecord = row
      this.modifyForm = {
        id: row.id,
        status: row.status || '',
        comment: row.teacherReviewComment || ''
      }
      this.modifyDialog.visible = true
    },
    async submitModify() {
      this.$refs.modifyForm.validate(async (valid) => {
        if (valid) {
          try {
            const res = await this.$http.put(`/api/teacher/exemptions/${this.currentRecord.id}/modify`, {
              status: this.modifyForm.status,
              teacherReviewComment: this.modifyForm.comment
            })
            
            if (res.data.code === 200) {
              this.$message.success('修改成功')
              this.modifyDialog.visible = false
              this.getList()
            } else {
              this.$message.error(res.data.message || '修改失败')
            }
          } catch (error) {
            console.error('修改失败:', error)
            this.$message.error('修改失败')
          }
        }
      })
    },
    // 处理导出
    async handleExport() {
      try {
        this.$message.info('正在导出数据，请稍候...');
        
        const res = await this.$http.get('/api/teacher/exemptions/export', {
          params: {
            type: this.queryParams.type,
            status: this.queryParams.status,
            className: this.queryParams.className,
            keyword: this.queryParams.keyword
          },
          responseType: 'blob'  // 重要：指定响应类型为blob
        });
        
        // 创建下载链接
        const blob = new Blob([res.data], { 
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        
        // 生成文件名
        const now = new Date();
        const fileName = `免测重测申请记录_${now.getFullYear()}${(now.getMonth()+1).toString().padStart(2,'0')}${now.getDate().toString().padStart(2,'0')}.xlsx`;
        link.setAttribute('download', fileName);
        
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(link.href);
        
        this.$message.success('导出成功');
      } catch (error) {
        console.error('导出失败:', error);
        this.$message.error('导出失败');
      }
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

.review-info {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.review-info p {
  margin: 5px 0;
}

.modify-info {
  margin-bottom: 15px;
  color: #E6A23C;
  font-size: 14px;
}
</style> 