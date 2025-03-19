<template>
  <div class="exemption">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>免测/重测申请</span>
        <el-button style="float: right" type="primary" @click="showDialog">
          新建申请
        </el-button>
      </div>

      <el-table :data="applications" style="width: 100%" v-loading="loading">
        <el-table-column prop="type" label="申请类型">
          <template slot-scope="scope">
            {{ scope.row.type === 'EXEMPTION' ? '免测' : '重测' }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="申请原因" show-overflow-tooltip />
        <el-table-column prop="status" label="状态">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="query.page"
          :page-sizes="[10, 20, 50]"
          :page-size="query.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>

    <!-- 申请表单对话框 -->
    <el-dialog title="免测/重测申请" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="申请类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="EXEMPTION">免测</el-radio>
            <el-radio label="RETEST">重测</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="申请原因" prop="reason">
          <el-input
            type="textarea"
            v-model="form.reason"
            :rows="4"
            placeholder="请详细说明申请原因"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          提 交
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { formatDate } from '@/utils/date'

export default {
  name: 'StudentExemption',
  data() {
    return {
      applications: [],
      loading: false,
      submitting: false,
      total: 0,
      query: {
        page: 1,
        size: 10
      },
      dialogVisible: false,
      form: {
        type: 'EXEMPTION',
        reason: ''
      },
      rules: {
        type: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
        reason: [{ required: true, message: '请输入申请原因', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.fetchApplications()
  },
  methods: {
    async fetchApplications() {
      this.loading = true
      try {
        const response = await this.$http.get('/api/student/exemptions', {
          params: {
            page: this.query.page - 1,
            size: this.query.size
          }
        })
        if (response.data.code === 200) {
          this.applications = response.data.data.content
          this.total = response.data.data.totalElements
        }
      } catch (error) {
        console.error('获取申请列表失败:', error)
        this.$message.error('获取申请列表失败')
      } finally {
        this.loading = false
      }
    },
    showDialog() {
      this.dialogVisible = true
      this.form = {
        type: 'EXEMPTION',
        reason: ''
      }
    },
    async submitForm() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.submitting = true
          try {
            const response = await this.$http.post('/api/student/exemptions', this.form)
            if (response.data.code === 200) {
              this.$message.success('申请提交成功')
              this.dialogVisible = false
              this.fetchApplications()
            }
          } catch (error) {
            console.error('提交申请失败:', error)
            this.$message.error('提交申请失败')
          } finally {
            this.submitting = false
          }
        }
      })
    },
    handleSizeChange(val) {
      this.query.size = val
      this.fetchApplications()
    },
    handleCurrentChange(val) {
      this.query.page = val
      this.fetchApplications()
    },
    formatDate,
    getStatusType(status) {
      const types = {
        PENDING: 'info',
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
    }
  }
}
</script>

<style scoped>
.exemption {
  padding: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 