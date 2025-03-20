<template>
  <div class="data-review">
    <div class="header">
      <h2>数据录入与审核</h2>
      <div class="actions">
        <el-button type="primary" @click="showAddDialog">
          <i class="el-icon-plus"></i> 录入成绩
        </el-button>
        <el-button type="success" @click="exportData">
          <i class="el-icon-download"></i> 导出数据
        </el-button>
      </div>
    </div>

    <div class="filters">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="测试项目">
          <el-select v-model="filters.sportsItemId" placeholder="选择测试项目" clearable>
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="选择状态" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
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
      <el-table-column label="测试项目" width="120">
        <template slot-scope="scope">
          {{ scope.row.sportsItem ? scope.row.sportsItem.name : '' }}
        </template>
      </el-table-column>
      <el-table-column prop="score" label="成绩" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewComment" label="审核意见" />
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="handleEdit(scope.row)"
          >编辑</el-button>
          <el-button
            size="mini"
            type="success"
            @click="handleReview(scope.row)"
            v-if="scope.row.status === 'PENDING'"
          >审核</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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

    <!-- 录入成绩对话框 -->
    <el-dialog 
      title="录入成绩" 
      :visible.sync="addDialog.visible" 
      width="400px"
      @closed="handleDialogClosed"
    >
      <el-form :model="form" :rules="rules" ref="recordForm" label-width="80px">
        <!-- 学生选择 -->
        <el-form-item label="学生" prop="studentId">
          <el-select 
            v-model="form.studentId" 
            placeholder="请选择学生" 
            filterable
            remote
            :remote-method="searchStudents"
            :loading="studentLoading"
            style="width: 100%"
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

        <!-- 测试项目选择 -->
        <el-form-item label="测试项目" prop="sportsItemId">
          <el-select 
            v-model="form.sportsItemId" 
            placeholder="请选择项目"
            @change="handleSportsItemChange"
            style="width: 100%"
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

        <!-- 成绩输入 -->
        <el-form-item label="成绩" prop="score">
          <div style="display: flex; align-items: center;">
            <el-input-number 
              v-model="form.score" 
              :precision="2"
              :step="0.1"
              :min="0"
              controls-position="right"
              style="width: 160px;"
            ></el-input-number>
            <span style="margin-left: 10px; color: #606266;">{{ selectedItemUnit }}</span>
          </div>
        </el-form-item>

        <!-- 班级选择 -->
        <el-form-item label="班级" prop="className">
          <el-select v-model="form.className" placeholder="请选择班级" style="width: 100%">
            <el-option
              v-for="className in classList"
              :key="className"
              :label="className"
              :value="className"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 对话框底部按钮 -->
      <div slot="footer" class="dialog-footer">
        <el-button @click="addDialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
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
          <el-descriptions-item label="学生">{{ currentRecord.student ? currentRecord.student.realName : '-' }} ({{ currentRecord.studentNumber || '-' }})</el-descriptions-item>
          <el-descriptions-item label="测试项目">{{ currentRecord.sportsItem ? currentRecord.sportsItem.name : '-' }}</el-descriptions-item>
          <el-descriptions-item label="测试成绩">{{ currentRecord.score || '-' }} {{ currentRecord.sportsItem ? currentRecord.sportsItem.unit : '' }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentRecord.className || '-' }}</el-descriptions-item>
          <el-descriptions-item label="录入教师">{{ currentRecord.teacher ? currentRecord.teacher.realName : '-' }}</el-descriptions-item>
          <el-descriptions-item label="录入时间">{{ formatDateTime(currentRecord.createdAt) }}</el-descriptions-item>
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

    <!-- 详情对话框 -->
    <el-dialog 
      title="成绩详情" 
      :visible.sync="detailDialog.visible" 
      width="600px"
    >
      <div v-if="currentRecord" class="detail-container">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生姓名">
            {{ currentRecord.student ? currentRecord.student.realName : '-' }} ({{ currentRecord.studentNumber || '-' }})
          </el-descriptions-item>
          <el-descriptions-item label="学号">
            {{ currentRecord.student ? currentRecord.student.username : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="班级">
            {{ currentRecord.className || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="测试项目">
            {{ currentRecord.sportsItem ? currentRecord.sportsItem.name : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="测试成绩">
            {{ currentRecord.score || '-' }} {{ currentRecord.sportsItem ? currentRecord.sportsItem.unit : '' }}
          </el-descriptions-item>
          <el-descriptions-item label="记录教师">
            {{ currentRecord.teacher ? currentRecord.teacher.realName : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getStatusType(currentRecord.status)">
              {{ getStatusText(currentRecord.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            {{ formatDateTime(currentRecord.reviewTime) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2">
            {{ currentRecord.reviewComment || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 如果需要显示历史成绩，可以添加一个表格 -->
        <div class="history-records" v-if="historyRecords.length > 0">
          <h3>历史成绩记录</h3>
          <el-table :data="historyRecords" size="small" border stripe>
            <el-table-column label="测试时间" align="center">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.testTime) }}
              </template>
            </el-table-column>
            <el-table-column label="成绩" align="center">
              <template slot-scope="scope">
                {{ scope.row.score || '-' }} {{ scope.row.sportsItem ? scope.row.sportsItem.unit : '' }}
              </template>
            </el-table-column>
            <el-table-column label="状态" align="center">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 修改对话框 -->
    <el-dialog 
      title="修改审核状态" 
      :visible.sync="modifyDialog.visible" 
      width="400px"
    >
      <div class="review-info">
        <p>学生：{{ currentRecord ? currentRecord.student ? currentRecord.student.realName : '-' : '-' }} ({{ currentRecord ? currentRecord.studentNumber : '-' }})</p>
        <p>项目：{{ currentRecord ? currentRecord.sportsItem ? currentRecord.sportsItem.name : '-' : '-' }}</p>
        <p>成绩：{{ currentRecord ? currentRecord.score : '-' }} {{ currentRecord ? currentRecord.sportsItem ? currentRecord.sportsItem.unit : '' : '' }}</p>
      </div>
      <el-form :model="modifyForm" :rules="modifyRules" ref="modifyForm" label-width="80px">
        <el-form-item label="审核状态" prop="status">
          <el-select v-model="modifyForm.status" placeholder="请选择审核状态">
            <el-option label="待审核" value="PENDING"></el-option>
            <el-option label="通过" value="APPROVED"></el-option>
            <el-option label="驳回" value="REJECTED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="审核意见" prop="comment">
          <el-input
            type="textarea"
            v-model="modifyForm.comment"
            :rows="3"
            placeholder="请输入审核意见"
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
import { 
  getTestRecords, 
  addTestRecord, 
  reviewTestRecord,
  getHistoryRecords,
  exportRecords,
  downloadTemplate,
  modifyReview
} from '@/api/testRecord'

export default {
  name: 'DataReview',
  data() {
    return {
      loading: false,
      records: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      filters: {
        sportsItemId: undefined,
        status: undefined,
        keyword: ''
      },
      sportsItems: [],
      classList: [],
      addDialog: {
        visible: false
      },
      form: {
        studentId: null,
        sportsItemId: null,
        score: null,
        className: null
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
        className: [
          { required: true, message: '请选择班级', trigger: 'change' }
        ]
      },
      teachers: [],
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
      studentLoading: false,
      detailDialog: {
        visible: false
      },
      historyRecords: [],
      modifyDialog: {
        visible: false
      },
      modifyForm: {
        id: null,
        status: '',
        comment: ''
      },
      modifyRules: {
        status: [{ required: true, message: '请选择审核状态', trigger: 'change' }],
        comment: [{ required: true, message: '请输入审核意见', trigger: 'blur' }]
      },
    }
  },
  created() {
    this.init()
  },
  methods: {
    async init() {
      await Promise.all([
        this.getSportsItems(),
        this.fetchRecords()
      ])
    },
    async fetchRecords() {
      try {
        this.loading = true;
        const { sportsItemId, status, keyword } = this.filters;
        const response = await this.$http.get('/api/admin/test-records', {
          params: {
            sportsItemId,
            status,
            keyword,
            page: this.currentPage - 1,
            size: this.pageSize
          }
        });
        
        if (response.data.code === 200) {
          const { content, totalElements } = response.data.data;
          this.records = content;
          this.total = totalElements;
        } else {
          this.$message.error(response.data.msg || '获取数据失败');
        }
      } catch (error) {
        console.error('获取记录失败:', error);
        this.$message.error('获取记录失败');
      } finally {
        this.loading = false;
      }
    },
    async getSportsItems() {
      try {
        const response = await this.$http.get('/api/sports-items')
        if (response.data.code === 200) {
          this.sportsItems = response.data.data.content || []
        } else {
          this.$message.error(response.data.msg || '获取体测项目列表失败')
        }
      } catch (error) {
        console.error('获取体测项目列表失败:', error)
        this.$message.error('获取体测项目列表失败')
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchRecords()
    },
    resetFilters() {
      this.$refs.filterForm.resetFields()
      this.filters = {
        sportsItemId: undefined,
        status: undefined,
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
      if (!datetime) return ''
      return new Date(datetime).toLocaleString()
    },
    showAddDialog() {
      this.addDialog.visible = true
    },
    async exportData() {
      try {
        const response = await exportRecords(this.filters)
        // 处理导出逻辑
      } catch (error) {
        this.$message.error('导出失败：' + error.message)
      }
    },
    handleDialogClosed() {
      this.$refs.recordForm.resetFields()
    },
    submitForm() {
      this.$refs.recordForm.validate(async (valid) => {
        if (valid) {
          try {
            const response = await addTestRecord(this.form)
            if (response.data.code === 200) {
              this.$message.success('成绩录入成功')
              this.addDialog.visible = false
              this.handleSearch()
            } else {
              this.$message.error(response.data.msg || '录入失败')
            }
          } catch (error) {
            console.error('录入失败:', error)
            this.$message.error('录入失败')
          }
        }
      })
    },
    handleReview(record) {
      this.currentRecord = record
      this.reviewDialog.visible = true
    },
    submitReview() {
      this.$refs.reviewForm.validate(async (valid) => {
        if (valid) {
          try {
            const response = await reviewTestRecord(this.currentRecord.id, this.reviewForm)
            if (response.data.code === 200) {
              this.$message.success('审核成功')
              this.reviewDialog.visible = false
              this.handleSearch()
            } else {
              this.$message.error(response.data.msg || '审核失败')
            }
          } catch (error) {
            console.error('审核失败:', error)
            this.$message.error('审核失败')
          }
        }
      })
    },
    handleEdit(record) {
      this.currentRecord = record
      this.detailDialog.visible = true
    },
    handleSportsItemChange(value) {
      const item = this.sportsItems.find(i => i.id === value)
      if (item) {
        this.selectedItemUnit = item.unit
      }
    },
    searchStudents(query) {
      this.studentLoading = true
      this.$http.get('/api/admin/students', {
        params: {
          query,
          page: 0,
          size: 10
        }
      }).then(response => {
        if (response.data.code === 200) {
          this.students = response.data.data.content || []
        } else {
          this.$message.error(response.data.msg || '获取学生列表失败')
        }
      }).finally(() => {
        this.studentLoading = false
      })
    },
    handleSizeChange(newSize) {
      this.pageSize = newSize
      this.handleSearch()
    },
    handleCurrentChange(newPage) {
      this.currentPage = newPage
      this.handleSearch()
    },
    submitModify() {
      this.$refs.modifyForm.validate(async (valid) => {
        if (valid) {
          try {
            const response = await modifyReview(this.currentRecord.id, this.modifyForm)
            if (response.data.code === 200) {
              this.$message.success('审核状态修改成功')
              this.modifyDialog.visible = false
              this.handleSearch()
            } else {
              this.$message.error(response.data.msg || '修改失败')
            }
          } catch (error) {
            console.error('修改失败:', error)
            this.$message.error('修改失败')
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.data-review {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filters {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>