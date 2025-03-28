<template>
  <div class="data-review">
    <div class="header">
      <h2>数据录入与审核</h2>
      <div class="actions">
        <el-button type="success" @click="exportData">
          <i class="el-icon-download"></i> 导出数据
        </el-button>
      </div>
    </div>

    <div class="filters">
      <el-form :inline="true" class="filter-form">
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
      <el-table-column prop="reviewStatus" label="审核状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getReviewStatusType(scope.row.reviewStatus)">
            {{ getReviewStatusText(scope.row.reviewStatus) }}
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
            @click="handleReview(scope.row, 'APPROVED')"
            v-if="scope.row.reviewStatus === 'PENDING'"
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
      width="500px"
      @closed="resetForm('addForm')"
    >
      <el-form :model="addForm" :rules="rules" ref="addForm" label-width="100px">
        <el-form-item label="学生" prop="studentId" required>
          <el-select
            v-model="addForm.studentId"
            filterable
            remote
            reserve-keyword
            placeholder="请选择学生"
            :remote-method="searchStudents"
            :loading="studentLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in students"
              :key="item.id"
              :label="`${item.realName} (${item.studentNumber})`"
              :value="item.id"
            >
              <span>{{ item.realName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ item.studentNumber }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="测试项目" prop="sportsItemId" required>
          <el-select
            v-model="addForm.sportsItemId"
            filterable
            placeholder="请选择测试项目"
            style="width: 100%"
            @change="handleSportsItemChange"
          >
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="成绩" prop="score" required>
          <el-input-number
            v-model="addForm.score"
            :precision="2"
            :step="0.01"
            :min="0"
            controls-position="right"
            style="width: 100%"
          ></el-input-number>
        </el-form-item>
        
        <el-form-item label="班级" prop="className">
          <el-select v-model="addForm.className" placeholder="请选择班级" style="width: 100%">
            <el-option
              v-for="className in classNames"
              :key="className"
              :label="className"
              :value="className"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addDialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="submitAdd">确 定</el-button>
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
            <el-tag :type="getReviewStatusType(currentRecord.reviewStatus)">
              {{ getReviewStatusText(currentRecord.reviewStatus) }}
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
                <el-tag :type="getReviewStatusType(scope.row.reviewStatus)">
                  {{ getReviewStatusText(scope.row.reviewStatus) }}
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
import axios from 'axios'
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
        status: '',
        keyword: ''
      },
      sportsItems: [],
      classList: [],
      addDialog: {
        visible: false
      },
      addForm: {
        studentId: null,
        sportsItemId: null,
        score: 0,
        className: ''
      },
      rules: {
        studentId: [
          { required: true, message: '请选择学生', trigger: 'change' }
        ],
        sportsItemId: [
          { required: true, message: '请选择测试项目', trigger: 'change' }
        ],
        score: [
          { required: true, message: '请输入成绩', trigger: 'blur' }
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
      classNames: [],
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
        const { status, keyword } = this.filters;
        const response = await this.$http.get('/api/admin/test-records', {
          params: {
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
      this.filters = {
        status: '',
        keyword: ''
      };
      this.handleSearch(); // 重置后立即搜索
    },
    getReviewStatusText(reviewStatus) {
      const statusMap = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
      };
      return statusMap[reviewStatus] || reviewStatus;
    },
    getReviewStatusType(reviewStatus) {
      const typeMap = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      };
      return typeMap[reviewStatus] || 'info';
    },
    formatDateTime(datetime) {
      if (!datetime) return ''
      return new Date(datetime).toLocaleString()
    },
    showAddDialog() {
      this.addDialog.visible = true;
      this.loadSportsItems();
      this.loadClassNames();
    },
    async exportData() {
      try {
        const loading = this.$loading({
          lock: true,
          text: '正在导出数据...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        
        // 获取所有数据进行导出
        const params = {
          status: this.filters.status || null,
          keyword: this.filters.keyword || null
        };
        
        // 使用正确的API路径
        const baseUrl = process.env.VUE_APP_API_URL || 'http://localhost:8080';
        const response = await axios({
          url: `${baseUrl}/api/admin/test-records/export`,
          method: 'GET',
          params,
          responseType: 'blob',
          headers: {
            'X-User-ID': localStorage.getItem('userId') || '1'
          }
        });
        
        // 创建下载链接
        const blob = new Blob([response.data], { 
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        });
        
        // 获取文件名
        let fileName = '体测数据.xlsx';
        const contentDisposition = response.headers['content-disposition'];
        if (contentDisposition) {
          const matches = contentDisposition.match(/filename="(.+?)"/);
          if (matches && matches.length > 1) {
            fileName = decodeURIComponent(matches[1]);
          }
        }
        
        // 下载文件
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        
        loading.close();
        this.$message.success('数据导出成功');
      } catch (error) {
        console.error('导出失败:', error);
        this.$message.error('导出失败: ' + (error.message || '未知错误'));
      }
    },
    resetForm(formName) {
      if (this.$refs[formName]) {
        this.$refs[formName].resetFields();
      }
      this.addForm = {
        studentId: null,
        sportsItemId: null,
        score: 0,
        className: ''
      };
    },
    submitAdd() {
      this.$refs.addForm.validate(async (valid) => {
        if (valid) {
          try {
            const response = await this.$http.post('/api/admin/test-records', this.addForm);
            if (response.data.code === 200) {
              this.$message.success('成绩录入成功');
              this.addDialog.visible = false;
              this.handleSearch(); // 刷新列表
            } else {
              this.$message.error(response.data.msg || '录入失败');
            }
          } catch (error) {
            console.error('录入失败:', error);
            this.$message.error('录入失败');
          }
        }
      });
    },
    handleReview(row, action) {
      this.currentRecord = row;
      this.reviewForm.status = action;
      this.reviewDialog.visible = true;
    },
    submitReview() {
      this.$refs.reviewForm.validate(async (valid) => {
        if (valid) {
          try {
            // 先尝试正常审核
            const response = await this.$http.put(
              `/api/admin/records/${this.currentRecord.id}/review`, 
              null,
              {
                params: {
                  status: this.reviewForm.status,
                  comment: this.reviewForm.comment || ''
                }
              }
            );
            
            this.$message.success('审核成功');
            this.reviewDialog.visible = false;
            this.handleSearch();
          } catch (error) {
            // 如果是异常成绩错误
            if (error.response && error.response.data && 
                error.response.data.msg && 
                error.response.data.msg.includes('异常成绩')) {
              
              // 显示确认对话框
              this.$confirm(
                `${error.response.data.msg}，确定要强制通过审核吗？`, 
                '异常成绩警告', 
                {
                  confirmButtonText: '强制通过',
                  cancelButtonText: '取消',
                  type: 'warning'
                }
              ).then(async () => {
                // 用户确认，强制审核
                const forceResponse = await this.$http.put(
                  `/api/admin/records/${this.currentRecord.id}/review`, 
                  null,
                  {
                    params: {
                      status: this.reviewForm.status,
                      comment: this.reviewForm.comment + ' (强制通过异常成绩)',
                      forceApprove: true
                    }
                  }
                );
                
                this.$message.success('已强制通过审核');
                this.reviewDialog.visible = false;
                this.handleSearch();
              }).catch(() => {
                this.$message.info('已取消审核');
              });
            } else {
              // 其他错误
              console.error('审核失败:', error);
              this.$message.error('审核失败: ' + (error.response?.data?.msg || error.message || '未知错误'));
            }
          }
        }
      });
    },
    handleEdit(record) {
      this.currentRecord = record
      this.detailDialog.visible = true
    },
    handleSportsItemChange(value) {
      const item = this.sportsItems.find(i => i.id === value);
      if (item) {
        this.selectedItemUnit = item.unit;
      }
    },
    searchStudents(query) {
      if (query) {
        this.studentLoading = true;
        this.$http.get('/api/admin/students', {
          params: {
            query,
            page: 0,
            size: 10
          }
        }).then(response => {
          if (response.data.code === 200) {
            this.students = response.data.data.content || [];
          } else {
            this.$message.error(response.data.msg || '获取学生列表失败');
          }
        }).finally(() => {
          this.studentLoading = false;
        });
      }
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
    },
    async loadSportsItems() {
      try {
        const response = await this.$http.get('/api/sports-items');
        if (response.data.code === 200) {
          this.sportsItems = response.data.data;
        }
      } catch (error) {
        console.error('获取体育项目失败:', error);
        this.$message.error('获取体育项目失败');
      }
    },
    async loadClassNames() {
      try {
        const response = await this.$http.get('/api/admin/class-names');
        if (response.data.code === 200) {
          this.classNames = response.data.data;
        }
      } catch (error) {
        console.error('获取班级列表失败:', error);
      }
    },
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