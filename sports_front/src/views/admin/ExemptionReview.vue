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
      
      <el-table-column label="申请类型" prop="type" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.type === 'EXEMPTION' ? 'warning' : 'info'">
            {{ scope.row.type === 'EXEMPTION' ? '免测' : '重测' }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="申请原因" prop="reason" min-width="200" align="center">
        <template slot-scope="scope">
          <el-tooltip :content="scope.row.reason" placement="top">
            <span class="reason-text">{{ scope.row.reason }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      
      <el-table-column label="状态" prop="status" align="center" width="120">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="教师审核意见" prop="teacherReviewComment" min-width="200" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <template v-if="scope.row.teacherReviewComment">
            <el-tooltip :content="scope.row.teacherReviewComment" placement="top">
              <div>
                {{ scope.row.teacherReviewComment }}
                <div class="review-time">{{ formatDateTime(scope.row.teacherReviewTime) }}</div>
              </div>
            </el-tooltip>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      
      <el-table-column label="管理员审核意见" prop="adminReviewComment" min-width="200" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <template v-if="scope.row.adminReviewComment">
            <el-tooltip :content="scope.row.adminReviewComment" placement="top">
              <div>
                {{ scope.row.adminReviewComment }}
                <div class="review-time">{{ formatDateTime(scope.row.adminReviewTime) }}</div>
              </div>
            </el-tooltip>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" align="center" width="200">
        <template slot-scope="scope">
          <el-button
            v-if="canReview(scope.row)"
            size="mini"
            type="primary"
            @click="handleReview(scope.row)"
          >审核</el-button>
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
        :total="total">
      </el-pagination>
    </div>

    <!-- 审核对话框 -->
    <el-dialog
      :title="reviewDialog.title"
      :visible.sync="reviewDialog.visible"
      width="500px"
    >
      <div class="review-info">
        <p>学生：{{ currentRecord?.studentName }} ({{ currentRecord?.studentNumber }})</p>
        <p>班级：{{ currentRecord?.className }}</p>
        <p>申请类型：{{ currentRecord?.type === 'EXEMPTION' ? '免测申请' : '重测申请' }}</p>
        <p>申请原因：{{ currentRecord?.reason }}</p>
        <template v-if="currentRecord?.teacherReviewComment">
          <p>教师审核意见：{{ currentRecord.teacherReviewComment }}</p>
          <p>教师审核时间：{{ formatDateTime(currentRecord.teacherReviewTime) }}</p>
        </template>
      </div>
      <el-form :model="reviewForm" :rules="reviewRules" ref="reviewForm" label-width="100px">
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="reviewForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="isTeacher ? '教师意见' : '管理员意见'" prop="comment">
          <el-input
            type="textarea"
            v-model="reviewForm.comment"
            :rows="3"
            :placeholder="isTeacher ? '请输入教师审核意见' : '请输入管理员审核意见'"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="reviewDialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="submitReview">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      title="申请详情"
      :visible.sync="detailDialog.visible"
      width="600px"
    >
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="申请类型">
          {{ currentRecord.type === 'EXEMPTION' ? '免测申请' : '重测申请' }}
        </el-descriptions-item>
        <el-descriptions-item label="当前状态">
          {{ getStatusText(currentRecord.status) }}
        </el-descriptions-item>
        <el-descriptions-item label="学生姓名">
          {{ currentRecord.student ? currentRecord.student.realName : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="学号">
          {{ currentRecord.studentNumber }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间" :span="2">
          {{ formatDateTime(currentRecord.applyTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="申请原因" :span="2">
          {{ currentRecord.reason }}
        </el-descriptions-item>
        <el-descriptions-item label="教师审核时间" :span="2" v-if="currentRecord.teacherReviewTime">
          {{ formatDateTime(currentRecord.teacherReviewTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="教师审核意见" :span="2" v-if="currentRecord.teacherReviewComment">
          {{ currentRecord.teacherReviewComment }}
        </el-descriptions-item>
        <el-descriptions-item label="管理员审核时间" :span="2" v-if="currentRecord.adminReviewTime">
          {{ formatDateTime(currentRecord.adminReviewTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="管理员审核意见" :span="2" v-if="currentRecord.adminReviewComment">
          {{ currentRecord.adminReviewComment }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getExemptionList, teacherReview, adminReview } from '@/api/exemption'

export default {
  name: 'ExemptionReview',
  data() {
    return {
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        type: '',
        status: '',
        className: '',
        keyword: ''
      },
      // 表格数据
      tableData: [],
      // 总记录数
      total: 0,
      // 加载状态
      loading: false,
      // 当前用户类型
      userType: localStorage.getItem('userType'),
      // 审核对话框
      reviewDialog: {
        visible: false,
        title: '申请审核'
      },
      // 审核表单
      reviewForm: {
        id: null,
        status: '',
        comment: ''
      },
      // 审核表单校验规则
      reviewRules: {
        status: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
        comment: [{ required: true, message: '请输入审核意见', trigger: 'blur' }]
      },
      // 详情对话框
      detailDialog: {
        visible: false
      },
      // 当前记录
      currentRecord: null,
      // 班级列表
      classList: []
    }
  },
  computed: {
    isTeacher() {
      return this.userType === 'TEACHER'
    }
  },
  created() {
    this.getList()
    this.getClassList()
  },
  methods: {
    // 获取列表数据
    async getList() {
      try {
        this.loading = true;
        console.log('开始获取数据，参数:', {
          pageNum: this.queryParams.pageNum,
          pageSize: this.queryParams.pageSize,
          type: this.queryParams.type,
          status: this.queryParams.status,
          className: this.queryParams.className,
          keyword: this.queryParams.keyword
        });

        const res = await this.$http.get('/api/exemptions', {
          params: {
            pageNum: this.queryParams.pageNum - 1,
            pageSize: this.queryParams.pageSize,
            type: this.queryParams.type,
            status: this.queryParams.status,
            className: this.queryParams.className,
            keyword: this.queryParams.keyword
          }
        });
        
        console.log('接口响应数据完整内容:', res.data);
        if (res.data.code === 200) {
          console.log('分页数据:', res.data.data);
          console.log('内容数据:', res.data.data.content);
          this.tableData = res.data.data.content || [];
          this.total = res.data.data.totalElements || 0;
          console.log('设置后的表格数据:', this.tableData);
          console.log('数据总数:', this.total);
        } else {
          this.$message.error(res.data.message || '获取数据失败');
        }
      } catch (error) {
        console.error('获取列表失败:', error);
        this.$message.error('获取列表失败');
      } finally {
        this.loading = false;
      }
    },

    // 查询按钮点击
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    // 重置按钮点击
    resetQuery() {
      // 重置查询表单
      this.queryParams = {
        type: '',
        status: '',
        className: '',
        keyword: '',
        pageNum: 1,
        pageSize: 10
      }
      // 重新获取数据
      this.getList()
    },

    // 每页大小改变
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.getList()
    },

    // 当前页改变
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.getList()
    },

    // 获取状态类型
    getStatusType(status) {
      const statusMap = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return statusMap[status] || ''
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
      }
      return statusMap[status] || status
    },

    // 是否可以审核
    canReview(row) {
      return row.status === 'PENDING'
    },

    // 审核按钮点击
    handleReview(row) {
      this.currentRecord = row
      this.reviewForm = {
        id: row.id,
        status: '',
        comment: ''
      }
      this.reviewDialog.visible = true
    },

    // 提交审核
    async submitReview() {
      this.$refs.reviewForm.validate(async (valid) => {
        if (valid) {
          try {
            const reviewFunc = this.isTeacher ? teacherReview : adminReview
            const res = await reviewFunc({
              id: this.currentRecord.id,
              status: this.reviewForm.status,
              comment: this.reviewForm.comment
            })
            
            if (res.data.code === 200) {
              this.$message.success('审核成功')
              this.reviewDialog.visible = false
              this.getList()
            } else {
              this.$message.error(res.data.msg || '审核失败')
            }
          } catch (error) {
            console.error('审核失败:', error)
            this.$message.error('审核失败')
          }
        }
      })
    },

    // 查看详情
    handleDetail(row) {
      this.currentRecord = row
      this.detailDialog.visible = true
    },

    // 格式化日期时间
    formatDateTime(datetime) {
      if (!datetime) return '-'
      const date = new Date(datetime)
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

    // 获取班级列表
    async getClassList() {
      try {
        const res = await this.$http.get('/api/exemptions/classes')
        if (res.data.code === 200) {
          this.classList = res.data.data || []
        }
      } catch (error) {
        console.error('获取班级列表失败:', error)
        this.$message.error('获取班级列表失败')
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

.reason-text {
  display: inline-block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.el-descriptions {
  margin: 20px 0;
}

.review-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.review-info {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.review-info p {
  margin: 5px 0;
  line-height: 1.5;
}
</style> 