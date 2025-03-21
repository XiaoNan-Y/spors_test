<template>
  <div class="exemption">
    <!-- 顶部说明卡片 -->
    <el-card class="info-card">
      <div class="info-content">
        <i class="el-icon-info"></i>
        <div class="info-text">
          <h3>免测/重测申请</h3>
          <p>如果您因特殊原因需要申请免测或重测，请在此提交申请。我们会尽快处理您的申请。</p>
        </div>
      </div>
    </el-card>

    <!-- 操作栏 -->
    <div class="operation-bar">
      <el-button type="primary" @click="handleSubmit">提交申请</el-button>
    </div>

    <!-- 申请列表 -->
    <el-table
      :data="applicationList"
      border
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="applyTime" label="申请时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.applyTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="type" label="申请类型" width="100">
        <template slot-scope="scope">
          <el-tag :type="getTypeTag(scope.row.type)">
            {{ scope.row.type === 'EXEMPTION' ? '免测' : '重测' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sportsItemName" label="申请项目" width="120">
        <template slot-scope="scope">
          {{ scope.row.type === 'RETEST' ? (scope.row.sportsItemName || '-') : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="申请原因" show-overflow-tooltip></el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewComment" label="审核意见" show-overflow-tooltip></el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button-group>
            <el-button
              type="primary"
              size="mini"
              @click="handleEdit(scope.row)"
              :disabled="!canEdit(scope.row)"
            >
              修改
            </el-button>
            <el-button
              type="danger"
              size="mini"
              @click="handleCancel(scope.row)"
              :disabled="!canCancel(scope.row)"
            >
              撤销
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页器 -->
    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.currentPage"
        :page-sizes="[10, 20, 50]"
        :page-size="pagination.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
      >
      </el-pagination>
    </div>

    <!-- 提交/修改申请对话框 -->
    <el-dialog
      :title="editingId ? '修改申请' : '提交申请'"
      :visible.sync="dialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="申请类型" prop="type">
          <el-radio-group v-model="form.type" @change="handleTypeChange">
            <el-radio label="EXEMPTION">免测</el-radio>
            <el-radio label="RETEST">重测</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 当选择重测时显示体测项目选择 -->
        <el-form-item 
          v-if="form.type === 'RETEST'" 
          label="体测项目" 
          prop="sportsItemId"
          :rules="[
            { required: true, message: '请选择体测项目', trigger: 'change' }
          ]"
        >
          <el-select 
            v-model="form.sportsItemId" 
            placeholder="请选择体测项目"
            style="width: 100%"
          >
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="申请原因" prop="reason">
          <el-input
            type="textarea"
            v-model="form.reason"
            :rows="4"
            placeholder="请详细说明申请原因"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Exemption',
  data() {
    // 自定义验证规则
    const validateSportsItem = (rule, value, callback) => {
      if (this.form.type === 'RETEST' && !this.form.sportsItemId) {
        callback(new Error('重测申请必须选择体育项目'));
      } else {
        callback();
      }
    };

    return {
      loading: false,
      dialogVisible: false,
      applicationList: [],
      sportsItems: [],
      editingId: null,
      form: {
        type: 'EXEMPTION',
        reason: '',
        sportsItemId: null
      },
      rules: {
        type: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
        reason: [{ required: true, message: '请输入申请原因', trigger: 'blur' }],
        sportsItemId: [{ validator: validateSportsItem, trigger: 'change' }]
      },
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
    }
  },
  created() {
    this.fetchSportsItems()
    this.fetchApplications()
  },
  methods: {
    async fetchApplications() {
      try {
        this.loading = true
        console.log('Fetching applications...')
        
        const res = await this.$http.get('/api/student/exemptions', {
          params: {
            page: this.pagination.currentPage - 1, // 后端页码从0开始
            size: this.pagination.pageSize
          }
        })
        
        console.log('API response:', res.data)
        
        if (res.data.code === 200) {
          const { content, totalElements } = res.data.data
          this.applicationList = content || []
          this.pagination.total = totalElements || 0
          console.log('Applications loaded:', this.applicationList)
        } else {
          this.$message.error(res.data.message || '获取申请列表失败')
        }
      } catch (error) {
        console.error('获取申请列表失败:', error)
        this.$message.error('获取申请列表失败')
      } finally {
        this.loading = false
      }
    },
    async fetchSportsItems() {
      try {
        const res = await this.$http.get('/api/student/sports-items')
        if (res.data.code === 200) {
          this.sportsItems = res.data.data
        }
      } catch (error) {
        console.error('获取体测项目列表失败:', error)
        this.$message.error('获取体测项目列表失败')
      }
    },
    handleSubmit() {
      this.editingId = null
      this.form = {
        type: 'EXEMPTION',
        reason: '',
        sportsItemId: null
      }
      this.dialogVisible = true
    },
    async submitForm() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          try {
            const url = this.editingId 
              ? `/api/student/exemptions/${this.editingId}`
              : '/api/student/exemptions'
            const method = this.editingId ? 'put' : 'post'
            
            // 构造请求数据
            const data = {
              type: this.form.type,
              reason: this.form.reason
            }
            
            // 只有在重测时才添加体育项目ID
            if (this.form.type === 'RETEST') {
              data.sportsItemId = this.form.sportsItemId
            }
            
            console.log('Submitting form data:', data)
            
            const res = await this.$http[method](url, data)
            console.log('Server response:', res.data)
            
            if (res.data.code === 200) {
              this.$message.success(this.editingId ? '修改成功' : '提交成功')
              this.dialogVisible = false
              
              // 重置分页并强制刷新
              this.pagination.currentPage = 1
              await this.$nextTick()
              await this.fetchApplications()
            } else {
              this.$message.error(res.data.message || '操作失败')
            }
          } catch (error) {
            console.error('提交失败:', error)
            this.$message.error(error.response?.data?.message || '提交失败，请检查网络连接')
          }
        } else {
          console.log('表单验证失败')
          return false
        }
      })
    },
    handleEdit(row) {
      this.editingId = row.id
      this.form = {
        type: row.type,
        reason: row.reason,
        sportsItemId: row.sportsItemId
      }
      this.dialogVisible = true
    },
    async handleCancel(row) {
      try {
        await this.$confirm('确认撤销该申请吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const res = await this.$http.delete(`/api/student/exemptions/${row.id}`)
        if (res.data.code === 200) {
          this.$message.success('撤销成功')
          // 强制刷新列表
          await this.fetchApplications()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('撤销失败:', error)
          this.$message.error('撤销失败')
        }
      }
    },
    getTypeTag(type) {
      return type === 'EXEMPTION' ? 'warning' : 'primary'
    },
    getStatusType(status) {
      switch (status) {
        case 'PENDING_TEACHER': return 'info'
        case 'PENDING_ADMIN': return 'warning'
        case 'APPROVED': return 'success'
        case 'REJECTED_TEACHER':
        case 'REJECTED': return 'danger'
        default: return 'info'
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'PENDING_TEACHER': return '待教师审核'
        case 'PENDING_ADMIN': return '待管理员审核'
        case 'APPROVED': return '已通过'
        case 'REJECTED_TEACHER': return '教师已驳回'
        case 'REJECTED': return '已驳回'
        default: return status
      }
    },
    formatDateTime(datetime) {
      if (!datetime) return ''
      return new Date(datetime).toLocaleString()
    },
    canEdit(row) {
      return row.status === 'PENDING_TEACHER'
    },
    canCancel(row) {
      return row.status === 'PENDING_TEACHER'
    },
    // 当申请类型改变时重置体育项目
    handleTypeChange() {
      if (this.form.type === 'EXEMPTION') {
        this.form.sportsItemId = null
      }
      // 手动触发表单验证
      this.$nextTick(() => {
        this.$refs.form.validateField('sportsItemId')
      })
    },
    // 处理页码改变
    handleCurrentChange(val) {
      this.pagination.currentPage = val
      this.fetchApplications()
    },
    // 处理每页条数改变
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.pagination.currentPage = 1
      this.fetchApplications()
    }
  }
}
</script>

<style lang="scss" scoped>
.exemption {
  padding: 20px;

  .info-card {
    margin-bottom: 20px;
    
    .info-content {
      display: flex;
      align-items: flex-start;

      .el-icon-info {
        font-size: 24px;
        color: #409EFF;
        margin-right: 15px;
      }

      .info-text {
        h3 {
          margin: 0 0 10px;
          color: #303133;
        }

        p {
          margin: 0;
          color: #606266;
        }
      }
    }
  }

  .operation-bar {
    margin: 20px 0;
  }

  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
}
</style> 