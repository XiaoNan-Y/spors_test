<template>
  <div class="exemption-review">
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
      <el-table-column label="申请类型" min-width="100" align="center">
        <template slot-scope="scope">
          {{ scope.row.type === 'EXEMPTION' ? '免测' : '重测' }}
        </template>
      </el-table-column>
      <el-table-column label="申请原因" prop="reason" min-width="200" show-overflow-tooltip></el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template slot-scope="scope">
          <el-button
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
    <el-dialog title="申请审核" :visible.sync="reviewDialog.visible" width="500px">
      <div class="review-info">
        <p>学生：{{ currentRecord?.studentName }} ({{ currentRecord?.studentNumber }})</p>
        <p>班级：{{ currentRecord?.className }}</p>
        <p>项目：{{ currentRecord?.sportsItemName }}</p>
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
        <el-descriptions-item label="学生姓名">{{ currentRecord?.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRecord?.studentNumber }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ currentRecord?.className }}</el-descriptions-item>
        <el-descriptions-item label="测试项目">{{ currentRecord?.sportsItemName }}</el-descriptions-item>
        <el-descriptions-item label="申请类型">{{ currentRecord?.type === 'EXEMPTION' ? '免测' : '重测' }}</el-descriptions-item>
        <el-descriptions-item label="申请原因">{{ currentRecord?.reason }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatDateTime(currentRecord?.applyTime) }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialog.visible = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ExemptionReview',
  data() {
    return {
      // 查询参数
      queryParams: {
        keyword: '',
        pageNum: 1,
        pageSize: 10
      },
      // 表格数据
      tableData: [],
      // 总记录数
      total: 0,
      // 加载状态
      loading: false,
      // 当前操作的记录
      currentRecord: null,
      // 审核对话框
      reviewDialog: {
        visible: false
      },
      // 审核表单
      reviewForm: {
        status: '',
        comment: ''
      },
      // 审核表单校验规则
      reviewRules: {
        status: [
          { required: true, message: '请选择审核结果', trigger: 'change' }
        ],
        comment: [
          { required: true, message: '请输入审核意见', trigger: 'blur' }
        ]
      },
      // 详情对话框
      detailDialog: {
        visible: false
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
        this.loading = true
        const res = await this.$http.get('/api/teacher/exemptions', {
          params: {
            keyword: this.queryParams.keyword,
            page: this.queryParams.pageNum - 1,
            size: this.queryParams.pageSize
          }
        })
        
        if (res.data.code === 200) {
          this.tableData = res.data.data.content
          this.total = res.data.data.totalElements
        }
      } catch (error) {
        console.error('获取列表失败:', error)
        this.$message.error('获取列表失败')
      } finally {
        this.loading = false
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
            const res = await this.$http.post(`/api/teacher/exemptions/${this.currentRecord.id}/review`, {
              status: this.reviewForm.status,
              comment: this.reviewForm.comment
            })
            
            if (res.data.code === 200) {
              this.$message.success('审核成功')
              this.reviewDialog.visible = false
              this.getList()
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
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return new Date(dateTime).toLocaleString('zh-CN', {
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
</style> 