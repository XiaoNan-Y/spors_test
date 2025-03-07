<template>
  <div class="data-review">
    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="测试项目">
          <el-select v-model="filterForm.sportsItemId" placeholder="选择测试项目">
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="录入教师">
          <el-select v-model="filterForm.teacherId" placeholder="选择教师">
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.realName"
              :value="teacher.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="filterForm.status" placeholder="选择状态">
            <el-option label="待审核" value="PENDING"></el-option>
            <el-option label="已通过" value="APPROVED"></el-option>
            <el-option label="已驳回" value="REJECTED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="operation-buttons">
        <el-button type="primary" @click="handleAdd">
          <i class="el-icon-plus"></i> 录入成绩
        </el-button>
      </div>
    </div>

    <!-- 数据列表 -->
    <el-table
      :data="recordList || []"
      border
      stripe
      v-loading="loading"
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column label="学生姓名">
        <template slot-scope="scope">
          {{ scope.row?.student?.realName || '未知' }}
        </template>
      </el-table-column>
      <el-table-column label="测试项目">
        <template slot-scope="scope">
          {{ scope.row?.sportsItem?.name || '未知' }}
        </template>
      </el-table-column>
      <el-table-column label="测试成绩">
        <template slot-scope="scope">
          <span :class="{ 'abnormal-score': isAbnormalScore(scope.row) }">
            {{ scope.row?.score }}{{ scope.row?.sportsItem?.unit || '' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="录入教师">
        <template slot-scope="scope">
          {{ scope.row?.teacher?.realName || '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="testTime" label="测试时间" width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.testTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="handleReview(scope.row, 'approve')"
            v-if="scope.row.status === 'PENDING'"
          >审核</el-button>
          <el-button
            size="mini"
            type="warning"
            @click="handleEdit(scope.row)"
          >修改</el-button>
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
        :current-page="page.current"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="page.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="page.total"
      ></el-pagination>
    </div>

    <!-- 审核对话框 -->
    <el-dialog
      :title="reviewDialogTitle"
      :visible.sync="reviewDialogVisible"
      width="500px"
    >
      <div class="review-info" v-if="currentRecord">
        <p>学生：{{ currentRecord.student?.realName || '未知' }}</p>
        <p>测试项目：{{ currentRecord.sportsItem?.name || '未知' }}</p>
        <p>测试成绩：{{ currentRecord.score }}{{ currentRecord.sportsItem?.unit || '' }}</p>
        <p>测试时间：{{ formatDate(currentRecord.testTime) }}</p>
      </div>

      <div v-if="reviewType === 'reject'" class="reject-form">
        <el-form :model="reviewForm" :rules="reviewRules" ref="reviewForm">
          <el-form-item label="驳回原因" prop="reviewComment">
            <el-input
              type="textarea"
              v-model="reviewForm.reviewComment"
              :rows="3"
              placeholder="请输入驳回原因"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="reviewDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitReview">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="详细信息" :visible.sync="detailDialogVisible" width="500px">
      <div class="detail-info" v-if="currentRecord">
        <p><strong>学生：</strong>{{ currentRecord.student?.realName || '未知' }}</p>
        <p><strong>测试项目：</strong>{{ currentRecord.sportsItem?.name || '未知' }}</p>
        <p><strong>测试成绩：</strong>{{ currentRecord.score }}{{ currentRecord.sportsItem?.unit || '' }}</p>
        <p><strong>录入教师：</strong>{{ currentRecord.teacher?.realName || '未知' }}</p>
        <p><strong>测试时间：</strong>{{ formatDate(currentRecord.testTime) }}</p>
        <p><strong>审核状态：</strong>{{ getStatusLabel(currentRecord.status) }}</p>
        <p><strong>审核意见：</strong>{{ currentRecord.reviewComment || '无' }}</p>
        <p><strong>审核时间：</strong>{{ formatDate(currentRecord.reviewTime) || '未审核' }}</p>
      </div>
    </el-dialog>

    <!-- 修改对话框 -->
    <el-dialog title="修改测试记录" :visible.sync="editDialogVisible" width="500px">
      <el-form :model="editForm" ref="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="学生姓名" prop="studentId">
          <el-select v-model="editForm.studentId" placeholder="选择学生" disabled>
            <el-option
              :label="currentRecord.student?.realName"
              :value="currentRecord.student?.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试项目" prop="sportsItemId">
          <el-select v-model="editForm.sportsItemId" placeholder="选择测试项目" disabled>
            <el-option
              :label="currentRecord.sportsItem?.name"
              :value="currentRecord.sportsItem?.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试成绩" prop="score">
          <el-input-number 
            v-model="editForm.score" 
            :precision="2"
            :step="0.1"
            :min="0"
          ></el-input-number>
          <span style="margin-left: 10px">{{ currentRecord.sportsItem?.unit }}</span>
        </el-form-item>
        <el-form-item label="测试时间" prop="testTime">
          <el-date-picker
            v-model="editForm.testTime"
            type="datetime"
            placeholder="选择测试时间"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="审核状态" prop="status">
          <el-select v-model="editForm.status" placeholder="选择状态">
            <el-option label="待审核" value="PENDING"></el-option>
            <el-option label="已通过" value="APPROVED"></el-option>
            <el-option label="已驳回" value="REJECTED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="审核意见" prop="reviewComment">
          <el-input
            type="textarea"
            v-model="editForm.reviewComment"
            :rows="3"
            placeholder="请输入审核意见"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 添加数据录入对话框 -->
    <el-dialog title="录入成绩" :visible.sync="addDialogVisible" width="500px">
      <el-form :model="addForm" :rules="addRules" ref="addForm" label-width="100px">
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="addForm.studentId" placeholder="选择学生" filterable>
            <el-option
              v-for="student in students"
              :key="student.id"
              :label="student.realName"
              :value="student.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试项目" prop="sportsItemId">
          <el-select v-model="addForm.sportsItemId" placeholder="选择测试项目" @change="handleItemChange">
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试成绩" prop="score">
          <el-input-number 
            v-model="addForm.score" 
            :precision="2"
            :step="0.1"
            :min="0"
          ></el-input-number>
          <span style="margin-left: 10px">{{ selectedItem?.unit }}</span>
        </el-form-item>
        <el-form-item label="测试时间" prop="testTime">
          <el-date-picker
            v-model="addForm.testTime"
            type="datetime"
            placeholder="选择测试时间"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
          ></el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'DataReview',
  data() {
    return {
      filterForm: {
        sportsItemId: '',
        teacherId: '',
        status: '',
        dateRange: []
      },
      sportsItems: [],
      teachers: [],
      recordList: [],
      loading: false,
      page: {
        current: 1,
        size: 10,
        total: 0
      },
      reviewDialogVisible: false,
      detailDialogVisible: false,
      editDialogVisible: false,
      currentRecord: null,
      reviewType: '',
      reviewForm: {
        reviewComment: ''
      },
      reviewRules: {
        reviewComment: [
          { required: true, message: '请输入驳回原因', trigger: 'blur' }
        ]
      },
      editForm: {
        studentId: '',
        sportsItemId: '',
        score: 0,
        testTime: '',
        status: '',
        reviewComment: ''
      },
      editRules: {
        studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
        teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
        sportsItemId: [{ required: true, message: '请选择测试项目', trigger: 'change' }],
        score: [{ required: true, message: '请输入成绩', trigger: 'blur' }],
        testTime: [{ required: true, message: '请选择测试时间', trigger: 'change' }]
      },
      addDialogVisible: false,
      addForm: {
        studentId: '',
        sportsItemId: '',
        score: 0,
        testTime: ''
      },
      addRules: {
        studentId: [
          { required: true, message: '请选择学生', trigger: 'change' }
        ],
        sportsItemId: [
          { required: true, message: '请选择测试项目', trigger: 'change' }
        ],
        score: [
          { required: true, message: '请输入测试成绩', trigger: 'blur' }
        ],
        testTime: [
          { required: true, message: '请选择测试时间', trigger: 'change' }
        ]
      },
      selectedItem: null,
      students: [],
      rejectReason: ''
    }
  },
  computed: {
    reviewDialogTitle() {
      return this.reviewType === 'approve' ? '审核通过确认' : '驳回确认'
    }
  },
  async created() {
    try {
      this.loading = true
      // 先获取基础数据
      await Promise.all([
        this.fetchSportsItems(),
        this.fetchTeachers(),
        this.fetchStudents()
      ])
      // 再获取记录列表
      await this.fetchRecordList()
    } catch (error) {
      console.error('初始化数据失败:', error)
      this.$message.error('初始化数据失败')
    } finally {
      this.loading = false
    }
  },
  methods: {
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString()
    },
    getStatusType(status) {
      const typeMap = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return typeMap[status] || 'info'
    },
    getStatusLabel(status) {
      const labelMap = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
      }
      return labelMap[status] || '未知'
    },
    isAbnormalScore(record) {
      if (!record || !record.sportsItem) return false;
      
      const abnormalRules = {
        '100米跑': (score) => score < 9 || score > 20,
        '立定跳远': (score) => score < 1.5 || score > 3.5,
        '引体向上': (score) => score < 0 || score > 30,
        '1000米跑': (score) => score < 180 || score > 600,
        '仰卧起坐': (score) => score < 10 || score > 80
      };

      const rule = abnormalRules[record.sportsItem.name];
      return rule ? rule(record.score) : false;
    },
    async fetchSportsItems() {
      try {
        const res = await this.$http.get('/api/admin/sports-items')
        if (res.data.code === 200) {
          this.sportsItems = res.data.data || []
        }
      } catch (error) {
        console.error('获取体育项目失败:', error)
        this.$message.error('获取体育项目失败: ' + error.message)
        this.sportsItems = []
      }
    },
    async fetchTeachers() {
      try {
        const res = await this.$http.get('/api/admin/users', {
          params: { userType: 'TEACHER' }
        })
        if (res.data.code === 200) {
          this.teachers = res.data.data || []
        }
      } catch (error) {
        console.error('获取教师列表失败:', error)
        this.$message.error('获取教师列表失败: ' + error.message)
        this.teachers = []
      }
    },
    async fetchRecordList() {
      try {
        this.loading = true
        const params = {
          status: this.filterForm.status,
          teacherId: this.filterForm.teacherId,
          sportsItemId: this.filterForm.sportsItemId,
          startDate: this.filterForm.dateRange?.[0] || null,
          endDate: this.filterForm.dateRange?.[1] || null,
          page: this.page.current - 1,
          size: this.page.size
        }
        
        console.log('Fetching records with params:', params)
        const res = await this.$http.get('/api/admin/test-records/review', { params })
        console.log('Response:', res.data)

        if (res.data.code === 200) {
          this.recordList = Array.isArray(res.data.data.content) ? res.data.data.content : []
          this.page.total = res.data.data.totalElements || 0
        } else {
          throw new Error(res.data.msg || '获取记录列表失败')
        }
      } catch (error) {
        console.error('获取记录列表失败:', error)
        this.$message.error('获取记录列表失败: ' + error.message)
        this.recordList = []
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.page.current = 1
      this.fetchRecordList()
    },
    resetFilter() {
      this.filterForm = {
        sportsItemId: '',
        teacherId: '',
        status: '',
        dateRange: []
      }
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.page.size = val
      this.fetchRecordList()
    },
    handleCurrentChange(val) {
      this.page.current = val
      this.fetchRecordList()
    },
    handleReview(record, type) {
      this.currentRecord = { ...record }
      this.reviewType = type
      this.reviewForm.reviewComment = ''
      this.reviewDialogVisible = true
    },
    handleDetail(record) {
      this.currentRecord = { ...record }
      this.detailDialogVisible = true
    },
    handleEdit(record) {
      this.currentRecord = record;
      this.editForm = {
        studentId: record.student?.id,
        teacherId: record.teacher?.id,
        sportsItemId: record.sportsItem?.id,
        score: record.score,
        testTime: record.testTime,
        status: record.status,
        remark: record.remark || ''
      };
      this.editDialogVisible = true;
    },
    async submitEdit() {
      try {
        await this.$refs.editForm.validate();
        
        // 构造请求数据，确保数据格式正确
        const editData = {
          id: this.currentRecord.id,
          student: { id: this.editForm.studentId },
          teacher: { id: this.editForm.teacherId },
          sportsItem: { id: this.editForm.sportsItemId },
          score: parseFloat(this.editForm.score),
          testTime: this.editForm.testTime ? new Date(this.editForm.testTime).toISOString() : null,
          status: this.editForm.status,
          remark: this.editForm.remark
        };

        console.log('Submitting edit data:', editData);

        const res = await this.$http.put(
          `/api/admin/test-records/${this.currentRecord.id}`,
          editData
        );

        if (res.data.code === 200) {
          this.$message.success('修改成功');
          this.editDialogVisible = false;
          await this.fetchRecordList();
        } else {
          throw new Error(res.data.msg || '修改失败');
        }
      } catch (error) {
        console.error('修改失败:', error);
        this.$message.error('修改失败: ' + (error.response?.data?.msg || error.message));
      }
    },
    handleAdd() {
      this.addForm = {
        studentId: '',
        sportsItemId: '',
        score: 0,
        testTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
      }
      this.addDialogVisible = true
      if (this.students.length === 0) {
        this.fetchStudents()
      }
    },
    handleItemChange(itemId) {
      this.selectedItem = this.sportsItems.find(item => item.id === itemId)
    },
    async submitAdd() {
      try {
        await this.$refs.addForm.validate()
        
        // 转换日期时间格式
        const testTime = this.addForm.testTime ? new Date(this.addForm.testTime) : new Date();
        
        // 构造请求数据
        const recordData = {
          student: { id: parseInt(this.addForm.studentId) },
          sportsItem: { id: parseInt(this.addForm.sportsItemId) },
          teacher: { id: parseInt(this.$store.state.user.id) },
          score: parseFloat(this.addForm.score),
          testTime: testTime.toISOString(),
          status: 'PENDING'
        }
        
        console.log('Submitting record data:', recordData);
        
        const res = await this.$http.post('/admin/test-records/add', recordData)
        
        if (res.data.code === 200) {
          this.$message.success('录入成功')
          this.addDialogVisible = false
          this.fetchRecordList()
        } else {
          this.$message.error(res.data.msg || '录入失败')
        }
      } catch (error) {
        console.error('录入失败:', error)
        this.$message.error(error.response?.data?.msg || '录入失败')
      }
    },
    async submitReview() {
      try {
        if (this.reviewType === 'reject') {
          // 驳回时验证表单
          await this.$refs.reviewForm.validate()
        }

        const params = {
          id: this.currentRecord.id,
          status: this.reviewType === 'approve' ? 'APPROVED' : 'REJECTED',
          reviewComment: this.reviewType === 'approve' ? '审核通过' : this.reviewForm.reviewComment,
          reviewerId: this.$store.state.user.id
        }

        console.log('Submitting review:', params)

        const res = await this.$http.put('/api/admin/test-records/review', params)

        if (res.data.code === 200) {
          this.$message.success(this.reviewType === 'approve' ? '审核通过成功' : '驳回成功')
          this.reviewDialogVisible = false
          await this.fetchRecordList()
        } else {
          throw new Error(res.data.msg || '审核失败')
        }
      } catch (error) {
        console.error('审核失败:', error)
        this.$message.error('审核失败: ' + (error.response?.data?.msg || error.message))
      }
    },
    async fetchStudents() {
      try {
        const res = await this.$http.get('/admin/users', {
          params: { userType: 'STUDENT' }
        })
        if (res.data.code === 200) {
          this.students = res.data.data
        }
      } catch (error) {
        console.error('获取学生列表失败:', error)
        this.$message.error('获取学生列表失败')
      }
    }
  }
}
</script>

<style scoped>
.data-review {
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.abnormal-score {
  color: #f56c6c;
  font-weight: bold;
}

.review-info, .detail-info {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.warning-text {
  color: #e6a23c;
  margin-top: 10px;
}

.warning-text i {
  margin-right: 5px;
}

.operation-buttons {
  margin-left: 20px;
}

.reject-form {
  margin-top: 20px;
}
</style> 