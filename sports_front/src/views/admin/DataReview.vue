<template>
  <div class="data-review">
    <!-- 顶部操作栏 -->
    <div class="operation-bar">
      <el-button type="primary" @click="handleAdd">
        <i class="el-icon-plus"></i> 录入成绩
      </el-button>
      <el-button type="success" @click="handleExport">
        <i class="el-icon-download"></i> 导出数据
      </el-button>
    </div>

    <!-- 搜索筛选区 -->
    <div class="filter-section">
      <el-form :inline="true" :model="queryParams" ref="queryForm">
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
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60"></el-table-column>
        
        <el-table-column label="学生ID" prop="student_id" width="100"></el-table-column>
        
        <el-table-column label="教师ID" prop="teacher_id" width="100"></el-table-column>
        
        <el-table-column label="项目ID" prop="sports_item_id" width="100"></el-table-column>
        
        <el-table-column label="成绩" prop="score" width="120">
          <template slot-scope="scope">
            <span :class="{ 'abnormal-score': scope.row.isAbnormal }">
              {{ scope.row.score }}{{ scope.row.sportsItem?.unit || '' }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column label="测试时间" prop="test_time" width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.test_time) }}
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="审核意见" prop="review_comment" min-width="200">
          <template slot-scope="scope">
            <el-tooltip v-if="scope.row.review_comment" :content="scope.row.review_comment" placement="top">
              <span class="review-comment">{{ scope.row.review_comment }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        
        <el-table-column label="审核时间" prop="review_time" width="180">
          <template slot-scope="scope">
            {{ scope.row.review_time ? formatDateTime(scope.row.review_time) : '-' }}
          </template>
        </el-table-column>
        
        <el-table-column label="创建时间" prop="created_at" width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.created_at) }}
          </template>
        </el-table-column>
        
        <el-table-column label="更新时间" prop="updated_at" width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.updated_at) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button 
              v-if="scope.row.status === 'PENDING'"
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
          :current-page.sync="queryParams.pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="queryParams.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </el-card>

    <!-- 录入成绩对话框 -->
    <el-dialog 
      title="录入成绩" 
      :visible.sync="addDialog.visible" 
      width="500px"
      @closed="handleDialogClosed"
    >
      <el-form :model="form" :rules="rules" ref="recordForm" label-width="100px">
        <el-form-item label="学生" prop="studentId">
          <el-select 
            v-model="form.studentId" 
            placeholder="选择学生" 
            filterable
            remote
            :remote-method="searchStudents"
            :loading="studentLoading"
          >
            <el-option
              v-for="student in students"
              :key="student.id"
              :label="student.realName"
              :value="student.id"
            >
              <span>{{ student.realName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ student.username }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试项目" prop="sportsItemId">
          <el-select 
            v-model="form.sportsItemId" 
            placeholder="选择测试项目"
            @change="handleSportsItemChange"
          >
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <span>{{ item.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ item.unit }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试成绩" prop="score">
          <el-input-number 
            v-model="form.score" 
            :precision="2"
            :step="0.1"
            :min="0"
            controls-position="right"
          ></el-input-number>
          <span style="margin-left: 10px">{{ selectedItemUnit }}</span>
        </el-form-item>
        <el-form-item label="测试时间" prop="testTime">
          <el-date-picker
            v-model="form.testTime"
            type="datetime"
            placeholder="选择测试时间"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
          ></el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="addDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog 
      title="成绩审核" 
      :visible.sync="reviewDialog.visible" 
      width="500px"
    >
      <div class="review-info" v-if="currentRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生">{{ currentRecord.student.realName }}</el-descriptions-item>
          <el-descriptions-item label="测试项目">{{ currentRecord.sportsItem.name }}</el-descriptions-item>
          <el-descriptions-item label="测试成绩">
            {{ currentRecord.score }}{{ currentRecord.sportsItem.unit }}
          </el-descriptions-item>
          <el-descriptions-item label="测试时间">
            {{ formatDateTime(currentRecord.testTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="录入教师">{{ currentRecord.teacher.realName }}</el-descriptions-item>
          <el-descriptions-item label="录入时间">
            {{ formatDateTime(currentRecord.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form :model="reviewForm" ref="reviewForm" :rules="reviewRules" class="review-form">
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
            placeholder="请输入审核意见">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="reviewDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'DataReview',
  data() {
    return {
      loading: false,
      tableData: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        status: undefined,
        teacherId: undefined,
        sportsItemId: undefined
      },
      total: 0,
      addDialog: {
        visible: false
      },
      form: {
        studentId: null,
        sportsItemId: null,
        score: null,
        testTime: null
      },
      rules: {
        studentId: [
          { required: true, message: '请选择学生', trigger: 'change' }
        ],
        sportsItemId: [
          { required: true, message: '请选择测试项目', trigger: 'change' }
        ],
        score: [
          { required: true, message: '请输入测试成绩', trigger: 'blur' },
          { type: 'number', message: '成绩必须为数字', trigger: 'blur' }
        ],
        testTime: [
          { required: true, message: '请选择测试时间', trigger: 'change' }
        ]
      },
      teachers: [],
      sportsItems: [],
      selectedItemUnit: '',
      students: [],
      reviewDialog: {
        visible: false
      },
      currentRecord: null,
      reviewForm: {
        id: null,
        status: '',
        comment: ''
      },
      reviewRules: {
        status: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
        comment: [{ required: true, message: '请输入审核意见', trigger: 'blur' }]
      },
      studentLoading: false
    }
  },
  created() {
    this.getList()
    this.getTeachers()
    this.getStudents()
    this.getSportsItems()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const res = await this.$http.get('/api/admin/test-records/review', {
          params: this.queryParams
        })
        if (res.data.code === 200) {
          this.tableData = res.data.data.content
          this.total = res.data.data.totalElements
        }
      } catch (error) {
        console.error('获取数据失败:', error)
        this.$message.error('获取数据失败')
      }
      this.loading = false
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
      return texts[status] || '未知'
    },

    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.getList()
    },

    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.getList()
    },

    handleAdd() {
      this.addDialog.visible = true
      this.resetForm()
    },

    resetForm() {
      this.$nextTick(() => {
        if (this.$refs.recordForm) {
          this.$refs.recordForm.resetFields()
        }
        this.form = {
          studentId: null,
          sportsItemId: null,
          score: null,
          testTime: null
        }
        this.selectedItemUnit = ''
      })
    },

    handleDialogClosed() {
      this.resetForm()
    },

    submitForm() {
      this.$refs.recordForm.validate(async (valid) => {
        if (valid) {
          try {
            const data = {
              studentId: this.form.studentId,
              sportsItemId: this.form.sportsItemId,
              score: Number(this.form.score),
              testTime: this.form.testTime,
              teacherId: JSON.parse(localStorage.getItem('user')).id
            }

            const res = await this.$http.post('/api/admin/test-records', data)
            if (res.data.code === 200) {
              this.$message.success('成绩录入成功')
              this.addDialog.visible = false
              this.getList()
            } else {
              this.$message.error(res.data.message || '录入失败')
            }
          } catch (error) {
            console.error('录入成绩失败:', error)
            this.$message.error(error.response?.data?.message || '录入成绩失败')
          }
        }
      })
    },

    async getTeachers() {
      try {
        const res = await this.$http.get('/api/admin/users', {
          params: { userType: 'TEACHER' }
        })
        if (res.data.code === 200) {
          this.teachers = res.data.data.content
        }
      } catch (error) {
        console.error('获取教师列表失败:', error)
      }
    },

    async getSportsItems() {
      try {
        const res = await this.$http.get('/api/admin/sports-items')
        if (res.data.code === 200) {
          this.sportsItems = res.data.data.filter(item => item.isActive)
          console.log('体测项目列表:', this.sportsItems)
        } else {
          this.$message.error('获取体测项目失败')
        }
      } catch (error) {
        console.error('获取体测项目列表失败:', error)
        this.$message.error('获取体测项目列表失败')
      }
    },

    async getStudents() {
      try {
        const res = await this.$http.get('/api/admin/users', {
          params: { userType: 'STUDENT' }
        })
        if (res.data.code === 200) {
          this.students = res.data.data.content
        }
      } catch (error) {
        console.error('获取学生列表失败:', error)
      }
    },

    async handleExport() {
      try {
        const res = await this.$http.get('/api/admin/test-records/export', {
          params: this.queryParams,
          responseType: 'blob'
        })
        
        const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' })
        const link = document.createElement('a')
        link.href = window.URL.createObjectURL(blob)
        link.download = '体测成绩数据.xlsx'
        link.click()
        window.URL.revokeObjectURL(link.href)
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
      }
    },

    handleSportsItemChange(value) {
      const selectedItem = this.sportsItems.find(item => item.id === value)
      if (selectedItem) {
        this.selectedItemUnit = selectedItem.unit
        console.log('选中的项目:', selectedItem)
      } else {
        this.selectedItemUnit = ''
      }
      this.form.score = null
    },

    handleQuery() {
      this.getList()
    },

    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        status: undefined,
        teacherId: undefined,
        sportsItemId: undefined
      }
      this.getList()
    },

    handleReview(record) {
      this.currentRecord = record
      this.reviewDialog.visible = true
    },

    handleDetail(record) {
      // 实现详情功能
    },

    submitReview() {
      this.$refs.reviewForm.validate(async (valid) => {
        if (valid) {
          try {
            const data = {
              id: this.currentRecord.id,
              status: this.reviewForm.status,
              comment: this.reviewForm.comment
            }

            const res = await this.$http.put('/api/admin/test-records/review', data)
            if (res.data.code === 200) {
              this.$message.success('审核成功')
              this.reviewDialog.visible = false
              this.getList()
            } else {
              this.$message.error(res.data.message || '审核失败')
            }
          } catch (error) {
            console.error('审核失败:', error)
            this.$message.error(error.response?.data?.message || '审核失败')
          }
        }
      })
    },

    searchStudents(query) {
      this.studentLoading = true
      this.$http.get('/api/admin/users', {
        params: {
          userType: 'STUDENT',
          realName: query
        }
      }).then(res => {
        if (res.data.code === 200) {
          this.students = res.data.data.content
        }
        this.studentLoading = false
      }).catch(error => {
        console.error('获取学生列表失败:', error)
        this.studentLoading = false
      })
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
    }
  }
}
</script>

<style scoped>
.data-review {
  padding: 20px;
}

.operation-bar {
  margin-bottom: 20px;
}

.filter-section {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.table-card {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.review-info {
  margin-bottom: 20px;
}

.review-form {
  margin-top: 20px;
}

.abnormal-score {
  color: #f56c6c;
  font-weight: bold;
}

.review-comment {
  display: inline-block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.el-descriptions {
  margin: 20px 0;
}
</style> 