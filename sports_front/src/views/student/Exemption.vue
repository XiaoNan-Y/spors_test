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
      <el-table-column prop="createTime" label="申请时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="type" label="申请类型" width="100">
        <template slot-scope="scope">
          <el-tag :type="getTypeTag(scope.row.type)">
            {{ scope.row.type === 'EXEMPTION' ? '免测' : '重测' }}
          </el-tag>
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

    <!-- 提交申请对话框 -->
    <el-dialog
      title="提交申请"
      :visible.sync="dialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="申请类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="EXEMPTION">免测</el-radio>
            <el-radio label="RETEST">重测</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 当选择重测时显示体测项目选择 -->
        <el-form-item 
          v-if="form.type === 'RETEST'" 
          label="体测项目" 
          prop="sportsItemId"
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">提交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Exemption',
  data() {
    return {
      applicationList: [],
      loading: false,
      submitting: false,
      dialogVisible: false,
      form: {
        type: 'EXEMPTION',
        reason: '',
        sportsItemId: null
      },
      rules: {
        type: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
        reason: [{ required: true, message: '请输入申请原因', trigger: 'blur' }],
        sportsItemId: [{ 
          required: true, 
          message: '请选择体测项目', 
          trigger: 'change',
          validator: (rule, value, callback) => {
            if (this.form.type === 'RETEST' && !value) {
              callback(new Error('请选择体测项目'));
            } else {
              callback();
            }
          }
        }]
      },
      sportsItems: [],
      editingId: null
    }
  },
  created() {
    this.fetchApplications()
    this.fetchSportsItems()
  },
  methods: {
    async fetchApplications() {
      try {
        this.loading = true
        const res = await this.$axios.get('/api/student/exemptions', {
          params: {
            page: 0,
            size: 10
          }
        })
        
        if (res.data.code === 200) {
          this.applicationList = res.data.data.content || []
          console.log('获取到的申请列表:', this.applicationList)
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
    handleSubmit() {
      this.form = {
        type: 'EXEMPTION',
        reason: '',
        sportsItemId: null
      }
      this.dialogVisible = true
    },
    async submitForm() {
      try {
        await this.$refs.form.validate()
        this.submitting = true
        
        const data = {
          ...this.form,
          sportsItemId: this.form.type === 'RETEST' ? this.form.sportsItemId : null
        }
        
        let res
        if (this.editingId) {
          // 修改申请
          res = await this.$axios.put(`/api/student/exemptions/${this.editingId}`, data)
        } else {
          // 新建申请
          res = await this.$axios.post('/api/student/exemptions', data)
        }

        if (res.data.code === 200) {
          this.$message.success(this.editingId ? '修改成功' : '提交成功')
          this.dialogVisible = false
          this.fetchApplications()
          this.resetForm()
        } else {
          this.$message.error(res.data.message || '提交失败')
        }
      } catch (error) {
        console.error('提交失败:', error)
        this.$message.error('提交失败')
      } finally {
        this.submitting = false
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确认删除该申请?', '提示', {
          type: 'warning'
        })
        const res = await this.$axios.delete(`/api/student/exemptions/${row.id}`)
        if (res.data.code === 200) {
          this.$message.success('删除成功')
          this.fetchApplications()
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    getTypeTag(type) {
      return type === 'EXEMPTION' ? 'warning' : 'primary'
    },
    getStatusType(status) {
      const types = {
        'PENDING': 'info',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
      }
      return texts[status] || status
    },
    formatDateTime(datetime) {
      if (!datetime) return ''
      return new Date(datetime).toLocaleString()
    },
    async fetchSportsItems() {
      try {
        const res = await this.$axios.get('/api/sports-items/student/list')
        if (res.data.code === 200) {
          this.sportsItems = res.data.data
        }
      } catch (error) {
        console.error('获取体测项目列表失败:', error)
        this.$message.error('获取体测项目列表失败')
      }
    },
    resetForm() {
      this.form = {
        type: 'EXEMPTION',
        reason: '',
        sportsItemId: null
      }
      this.editingId = null
      if (this.$refs.form) {
        this.$refs.form.resetFields()
      }
    },
    canEdit(row) {
      return row.status === 'PENDING_TEACHER'
    },
    canCancel(row) {
      return row.status === 'PENDING_TEACHER'
    },
    handleEdit(row) {
      this.form = {
        type: row.type,
        reason: row.reason,
        sportsItemId: row.sportsItemId
      }
      this.editingId = row.id
      this.dialogVisible = true
    },
    async handleCancel(row) {
      try {
        await this.$confirm('确认撤销该申请吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        const res = await this.$axios.delete(`/api/student/exemptions/${row.id}`)
        if (res.data.code === 200) {
          this.$message.success('撤销成功')
          this.fetchApplications()
        } else {
          this.$message.error(res.data.message || '撤销失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('撤销失败:', error)
          this.$message.error('撤销失败')
        }
      }
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
}
</style> 