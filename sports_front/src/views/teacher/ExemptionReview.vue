<template>
  <div class="exemption-review">
    <div class="header">
      <h2>重测申请审核</h2>
      <div class="header-desc">
        审核学生提交的重测申请，可直接通过或驳回
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <div class="filter-section">
      <el-form :inline="true" :model="queryParams" ref="queryForm">
        <el-form-item label="关键字">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="学生姓名/学号"
            clearable
            @keyup.enter.native="handleQuery"
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
      <el-table-column label="测试项目" min-width="120" align="center">
        <template slot-scope="scope">
          {{ scope.row.sportsItemName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="申请原因" prop="reason" min-width="200" show-overflow-tooltip></el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请时间" width="160" align="center">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.applyTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180">
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
    <el-dialog
      title="审核重测申请"
      :visible.sync="reviewDialog.visible"
      width="500px"
    >
      <div class="review-info">
        <p><strong>学生信息：</strong>{{ currentRecord?.studentName }} ({{ currentRecord?.studentNumber }})</p>
        <p><strong>班级：</strong>{{ currentRecord?.className }}</p>
        <p><strong>测试项目：</strong>{{ currentRecord?.sportsItemName }}</p>
        <p><strong>申请原因：</strong>{{ currentRecord?.reason }}</p>
      </div>
      <el-form :model="reviewForm" :rules="reviewRules" ref="reviewForm" label-width="100px">
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
            :rows="4"
            placeholder="请输入审核意见"
          ></el-input>
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
      <el-descriptions :column="1" border>
        <el-descriptions-item label="学生姓名">{{ currentRecord?.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRecord?.studentNumber }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ currentRecord?.className }}</el-descriptions-item>
        <el-descriptions-item label="测试项目">{{ currentRecord?.sportsItemName }}</el-descriptions-item>
        <el-descriptions-item label="申请原因">{{ currentRecord?.reason }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatDateTime(currentRecord?.applyTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRecord?.status)">
            {{ getStatusText(currentRecord?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" v-if="currentRecord?.teacherReviewComment">
          {{ currentRecord?.teacherReviewComment }}
        </el-descriptions-item>
        <el-descriptions-item label="审核时间" v-if="currentRecord?.teacherReviewTime">
          {{ formatDateTime(currentRecord?.teacherReviewTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'RetestReview',
  data() {
    return {
      loading: false,
      queryParams: {
        keyword: '',
        pageNum: 1,
        pageSize: 10
      },
      total: 0,
      tableData: [],
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
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    // 获取列表数据
    async getList() {
      try {
        this.loading = true;
        const response = await this.$http.get('/api/teacher/retest-applications', {
          params: {
            keyword: this.queryParams.keyword,
            page: this.queryParams.pageNum - 1,
            size: this.queryParams.pageSize
          }
        });
        
        console.log('API Response:', response.data);
        
        if (response.data.code === 200) {
          this.tableData = response.data.data.content;
          this.total = response.data.data.totalElements;
          console.log('Loaded data:', this.tableData);
        } else {
          this.$message.error(response.data.message || '获取列表失败');
        }
      } catch (error) {
        console.error('获取列表失败:', error);
        this.$message.error('获取列表失败');
      } finally {
        this.loading = false;
      }
    },
    // 查询按钮
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    // 重置按钮
    resetQuery() {
      this.$refs.queryForm.resetFields()
      this.handleQuery()
    },
    // 处理审核
    handleReview(row) {
      this.currentRecord = row
      this.reviewForm = {
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
            const response = await this.$http.post(
              `/api/teacher/retest-applications/${this.currentRecord.id}/review`,
              {
                status: this.reviewForm.status,
                comment: this.reviewForm.comment,
                reviewerId: localStorage.getItem('userId')
              }
            );
            
            if (response.data.code === 200) {
              this.$message.success('审核成功');
              this.reviewDialog.visible = false;
              this.getList();
            } else {
              this.$message.error(response.data.message || '审核失败');
            }
          } catch (error) {
            console.error('审核失败:', error);
            this.$message.error('审核失败: ' + error.message);
          }
        }
      });
    },
    // 查看详情
    handleDetail(row) {
      this.currentRecord = row
      this.detailDialog.visible = true
    },
    // 分页大小改变
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.getList()
    },
    // 页码改变
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.getList()
    },
    // 格式化时间
    formatDateTime(datetime) {
      if (!datetime) return '-'
      return new Date(datetime).toLocaleString()
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

.header-desc {
  color: #666;
  margin-top: 10px;
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
  background-color: #f5f7fa;
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 4px;
}

.review-info p {
  margin: 8px 0;
  line-height: 1.5;
}
</style> 