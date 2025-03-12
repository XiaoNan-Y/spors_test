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
    <el-form-item label="关键字">
      <el-input 
        v-model="queryParams.keyword" 
        placeholder="学生/教师姓名"
        clearable
      ></el-input>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </el-form-item>
  </el-form>
</div>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <!-- 数据表格部分 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
      
      <el-table-column label="学生姓名" prop="student.realName" min-width="120" align="center">
        <template slot-scope="scope">
          {{ scope.row.student ? scope.row.student.realName : '-' }}
        </template>
      </el-table-column>
      
      <el-table-column label="学生学号" prop="studentNumber" min-width="120" align="center">
        <template slot-scope="scope">
          {{ scope.row.student ? scope.row.student.studentNumber : '-' }}
        </template>
      </el-table-column>
      
      <el-table-column label="测试项目" prop="sportsItem.name" min-width="120" align="center">
        <template slot-scope="scope">
          {{ scope.row.sportsItem ? scope.row.sportsItem.name : '-' }}
        </template>
      </el-table-column>
      
      <el-table-column label="成绩" min-width="120" align="center">
        <template slot-scope="scope">
          <span :class="{ 'abnormal-score': scope.row.isAbnormal }">
            {{ scope.row.score }}{{ scope.row.sportsItem?.unit || '' }}
          </span>
        </template>
      </el-table-column>
      
      <el-table-column label="测试时间" min-width="160" align="center">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.testTime) }}
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template slot-scope="scope">
          <el-tooltip content="审核" placement="top" :disabled="!canReview(scope.row)">
            <el-button
              v-if="canReview(scope.row)"
              type="primary"
              icon="el-icon-check"
              size="mini"
              circle
              @click="handleReview(scope.row)"
            ></el-button>
          </el-tooltip>
          <el-tooltip content="修改" placement="top" :disabled="!canModify(scope.row)">
            <el-button
              v-if="canModify(scope.row)"
              type="warning"
              icon="el-icon-edit"
              size="mini"
              circle
              @click="handleModify(scope.row)"
            ></el-button>
          </el-tooltip>
          <el-tooltip content="详情" placement="top">
            <el-button
              type="info"
              icon="el-icon-view"
              size="mini"
              circle
              @click="handleDetail(scope.row)"
            ></el-button>
          </el-tooltip>
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

      <!-- 测试时间选择 -->
      <el-form-item label="测试时间" prop="testTime">
        <el-date-picker
          v-model="form.testTime"
          type="datetime"
          placeholder="选择测试时间"
          format="yyyy-MM-dd HH:mm"
          value-format="yyyy-MM-dd HH:mm:ss"
          style="width: 100%"
        ></el-date-picker>
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
          <el-descriptions-item label="测试时间">{{ formatDateTime(currentRecord.testTime) }}</el-descriptions-item>
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
          <el-descriptions-item label="测试项目">
            {{ currentRecord.sportsItem ? currentRecord.sportsItem.name : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="测试成绩">
            {{ currentRecord.score || '-' }} {{ currentRecord.sportsItem ? currentRecord.sportsItem.unit : '' }}
          </el-descriptions-item>
          <el-descriptions-item label="测试时间">
            {{ formatDateTime(currentRecord.testTime) }}
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
      }
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
      try {
        this.loading = true
        const params = {
          pageNum: this.queryParams.pageNum,
          pageSize: this.queryParams.pageSize,
          status: this.queryParams.status,
          sportsItemId: this.queryParams.sportsItemId
        }
        const res = await getTestRecords(params)
        if (res.data.code === 200) {
          const { content, totalElements } = res.data.data
          this.tableData = content
          this.total = totalElements
        } else {
          this.$message.error(res.data.msg || '获取数据失败')
        }
      } catch (error) {
        console.error('获取数据失败:', error)
        this.$message.error('获取数据失败: ' + error)
      } finally {
        this.loading = false
      }
    },

    getStatusType(status) {
      const types = {
        'PENDING': 'warning',
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

            const res = await addTestRecord(data)
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
      // 构建查询参数
      const params = {
        status: this.queryParams.status,
        teacherId: this.queryParams.teacherId,
        sportsItemId: this.queryParams.sportsItemId
      }
      
      // 构建查询字符串
      const queryString = Object.entries(params)
        .filter(([_, value]) => value != null && value !== '')
        .map(([key, value]) => `${key}=${value}`)
        .join('&')
      
      // 下载文件
      window.location.href = `/api/admin/test-record/export${queryString ? '?' + queryString : ''}`
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

    handleReview(row) {
      this.currentRecord = { ...row }; // 创建副本以避免直接修改
      this.reviewForm = {
        id: row.id,
        status: '',
        comment: ''
      };
      this.reviewDialog.visible = true;
    },

    async handleDetail(row) {
      this.currentRecord = { ...row }; // 创建副本以避免直接修改
      this.detailDialog.visible = true;
      
      try {
        const res = await getHistoryRecords({
          studentNumber: row.studentNumber,
          sportsItemId: row.sportsItemId,
          excludeId: row.id
        });
        
        if (res.data.code === 200) {
          this.historyRecords = res.data.data;
        } else {
          this.$message.warning(res.data.msg || '获取历史记录失败');
          this.historyRecords = [];
        }
      } catch (error) {
        console.error('获取历史记录失败:', error);
        this.$message.error('获取历史记录失败');
        this.historyRecords = [];
      }
    },

    submitReview() {
      this.$refs.reviewForm.validate(async (valid) => {
        if (valid) {
          try {
            const data = {
              id: this.currentRecord.id,
              status: this.reviewForm.status,
              reviewComment: this.reviewForm.comment
            };

            const res = await reviewTestRecord(data);
            if (res.data.code === 200) {
              this.$message.success('审核成功');
              this.reviewDialog.visible = false;
              this.getList();
            } else {
              this.$message.error(res.data.msg || '审核失败');
            }
          } catch (error) {
            console.error('审核失败:', error);
            this.$message.error('审核失败');
          }
        }
      });
    },

    canReview(record) {
      return record.status === 'PENDING' || record.status === 'REJECTED'
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
    },

    handleModify(row) {
      this.currentRecord = { ...row }; // 创建副本以避免直接修改
      this.modifyForm = {
        id: row.id,
        status: row.status,
        comment: row.reviewComment || ''
      };
      this.modifyDialog.visible = true;
    },

    submitModify() {
      this.$refs.modifyForm.validate(async (valid) => {
        if (valid) {
          try {
            const data = {
              id: this.currentRecord.id,
              status: this.modifyForm.status,
              reviewComment: this.modifyForm.comment
            };

            const res = await modifyReview(data);
            if (res.data.code === 200) {
              this.$message.success('修改成功');
              this.modifyDialog.visible = false;
              this.getList();
            } else {
              this.$message.error(res.data.msg || '修改失败');
            }
          } catch (error) {
            console.error('修改失败:', error);
            this.$message.error('修改失败');
          }
        }
      });
    },

    canModify(row) {
      // 已审核的记录可以修改
      return row.status === 'APPROVED' || row.status === 'REJECTED'
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

.detail-container {
  padding: 20px;
}

.history-records {
  margin-top: 20px;
}

.history-records h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #606266;
}

/* 添加按钮间距 */
.el-button + .el-button {
  margin-left: 8px;
}

/* 可选：鼠标悬停效果 */
.el-button.is-circle:hover {
  transform: scale(1.1);
  transition: transform 0.2s;
}
</style> 