<template>
  <div class="notice-management">
    <div class="operation-bar">
      <div class="search-area">
        <el-input
          v-model="searchKeyword"
          placeholder="输入标题搜索"
          style="width: 200px"
          @keyup.enter.native="handleSearch"
        >
          <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
        </el-input>
        <el-select v-model="searchType" placeholder="通知类型" style="width: 120px; margin-left: 10px">
          <el-option label="全部" value=""></el-option>
          <el-option label="体测安排" value="TEST_SCHEDULE"></el-option>
          <el-option label="成绩公布" value="SCORE_RELEASE"></el-option>
          <el-option label="系统维护" value="SYSTEM_MAINTENANCE"></el-option>
          <el-option label="其他通知" value="OTHER"></el-option>
        </el-select>
      </div>
      
      <el-button type="primary" @click="handleAdd">
        <i class="el-icon-plus"></i> 发布通知
      </el-button>
    </div>

    <el-table
      :data="noticeList"
      border
      stripe
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="type" label="类型" width="100">
        <template slot-scope="scope">
          <el-tag :type="getTypeTagType(scope.row.type)">
            {{ getTypeLabel(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" show-overflow-tooltip>
        <template slot-scope="scope">
          <span :class="{'important-notice': scope.row.priority === 'HIGH'}">
            {{ scope.row.title }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="classIds" label="发送班级" width="150">
        <template slot-scope="scope">
          <span v-if="scope.row.isGlobal">全部班级</span>
          <span v-else>{{ formatClassIds(scope.row.classIds) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
            active-color="#13ce66"
            inactive-color="#ff4949"
          >
          </el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button-group class="operation-group">
            <el-button
              type="primary"
              size="mini"
              @click="handleView(scope.row)">
              查看
            </el-button>
            <el-button
              type="warning"
              size="mini"
              @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button
              type="danger"
              size="mini"
              @click="handleDelete(scope.row)">
              删除
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      >
      </el-pagination>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择通知类型" style="width: 100%">
            <el-option label="体测安排" value="TEST_SCHEDULE"></el-option>
            <el-option label="成绩公布" value="SCORE_RELEASE"></el-option>
            <el-option label="系统维护" value="SYSTEM_MAINTENANCE"></el-option>
            <el-option label="其他通知" value="OTHER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="重要程度" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择重要程度" style="width: 100%">
            <el-option label="普通" value="NORMAL"></el-option>
            <el-option label="重要" value="HIGH"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="发送范围" prop="isGlobal">
          <el-radio-group v-model="form.isGlobal">
            <el-radio :label="true">全部班级</el-radio>
            <el-radio :label="false">指定班级</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择班级" prop="classIds" v-if="!form.isGlobal">
          <el-select 
            v-model="selectedClasses" 
            multiple 
            placeholder="请选择班级"
            style="width: 100%"
          >
            <el-option 
              v-for="item in classList" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            type="textarea"
            v-model="form.content"
            :rows="10"
            maxlength="2000"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          >
          </el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog title="查看通知" :visible.sync="viewDialogVisible" width="700px">
      <div class="notice-header">
        <el-tag :type="getTypeTagType(currentNotice.type)" class="notice-type">
          {{ getTypeLabel(currentNotice.type) }}
        </el-tag>
        <h3 class="notice-title">{{ currentNotice.title }}</h3>
      </div>
      <div class="notice-meta">
        <span>发布时间：{{ formatDate(currentNotice.createTime) }}</span>
        <span v-if="currentNotice.priority === 'HIGH'" class="notice-priority">
          <i class="el-icon-warning"></i> 重要通知
        </span>
        <span>
          发送范围：
          <template v-if="currentNotice.isGlobal">全部班级</template>
          <template v-else>{{ formatClassIds(currentNotice.classIds) }}</template>
        </span>
      </div>
      <div class="notice-content">{{ currentNotice.content }}</div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TeacherNoticeManagement',
  data() {
    return {
      searchKeyword: '',
      searchType: '',
      noticeList: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      viewDialogVisible: false,
      dialogTitle: '',
      currentNotice: {},
      classList: [],
      selectedClasses: [],
      form: {
        id: null,
        type: 'OTHER',
        priority: 'NORMAL',
        title: '',
        content: '',
        status: 1,
        isGlobal: true,
        classIds: '',
        createTime: null,
        updateTime: null,
        creatorId: null
      },
      rules: {
        type: [
          { required: true, message: '请选择通知类型', trigger: 'change' }
        ],
        priority: [
          { required: true, message: '请选择重要程度', trigger: 'change' }
        ],
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入内容', trigger: 'blur' },
          { min: 5, max: 2000, message: '长度在 5 到 2000 个字符', trigger: 'blur' }
        ]
      },
      currentUserId: null
    }
  },
  created() {
    // 检查用户角色
    const userRole = localStorage.getItem('userRole')
    if (userRole !== 'TEACHER') {
      this.$message.error('无权访问')
      this.$router.push('/login')
      return
    }
    
    console.log('教师通知管理页面初始化，用户ID:', this.currentUserId)
    this.fetchNoticeList()
    this.fetchClassList() // 获取班级列表
  },
  watch: {
    selectedClasses(val) {
      this.form.classIds = val.join(',')
    }
  },
  methods: {
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString()
    },
    getTypeTagType(type) {
      const typeMap = {
        'TEST_SCHEDULE': 'primary',
        'SCORE_RELEASE': 'success',
        'SYSTEM_MAINTENANCE': 'warning',
        'OTHER': 'info'
      }
      return typeMap[type] || 'info'
    },
    getTypeLabel(type) {
      const typeMap = {
        'TEST_SCHEDULE': '体测安排',
        'SCORE_RELEASE': '成绩公布',
        'SYSTEM_MAINTENANCE': '系统维护',
        'OTHER': '其他通知'
      }
      return typeMap[type] || '其他通知'
    },
    formatClassIds(classIds) {
      if (!classIds) return '无'
      return classIds.split(',').join(', ')
    },
    async fetchClassList() {
      try {
        const response = await this.$http.get('/api/teacher/classes')
        if (response.data.code === 200) {
          // 将班级列表转换为选择器需要的格式
          this.classList = response.data.data.map(className => ({
            label: className,
            value: className
          }))
        } else {
          console.error('获取班级列表失败:', response.data.msg)
        }
      } catch (error) {
        console.error('获取班级列表失败:', error)
        // 如果API调用失败，使用一些默认班级
        this.classList = [
          { label: '计科1班', value: '计科1班' },
          { label: '计科2班', value: '计科2班' },
          { label: '软工1班', value: '软工1班' },
          { label: '软工2班', value: '软工2班' }
        ]
      }
    },
    async fetchNoticeList() {
      try {
        this.loading = true
        console.log('开始获取教师通知列表，当前用户ID:', this.currentUserId)
        
        const response = await this.$http.get('/api/teacher/notices', {
          params: {
            keyword: this.searchKeyword,
            type: this.searchType,
            page: this.currentPage - 1,
            size: this.pageSize
          }
        })
        
        console.log('获取到的响应:', response)
        
        if (response.data.code === 200) {
          const data = response.data.data
          this.noticeList = data.content || []
          this.total = data.totalElements || 0
          console.log('解析后的通知列表:', this.noticeList)
        } else {
          this.$message.error(response.data.msg || '获取通知列表失败')
        }
      } catch (error) {
        console.error('获取通知列表失败:', error)
        this.$message.error('获取通知列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchNoticeList()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchNoticeList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchNoticeList()
    },
    handleAdd() {
      this.dialogTitle = '发布通知'
      this.form = {
        id: null,
        type: 'OTHER',
        priority: 'NORMAL',
        title: '',
        content: '',
        status: 1,
        isGlobal: true,
        classIds: '',
        createTime: null,
        updateTime: null,
        creatorId: this.currentUserId
      }
      this.selectedClasses = []
      this.dialogVisible = true
    },
    handleView(row) {
      this.currentNotice = { ...row }
      this.viewDialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑通知'
      this.form = { ...row }
      this.selectedClasses = this.form.classIds ? this.form.classIds.split(',') : []
      this.dialogVisible = true
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          try {
            // 处理班级选择
            if (this.form.isGlobal) {
              this.form.classIds = null;
            } else if (this.selectedClasses && this.selectedClasses.length > 0) {
              this.form.classIds = this.selectedClasses.join(',');
            } else {
              return this.$message.error('请选择至少一个班级');
            }
            
            console.log('提交的表单数据:', this.form);
            
            const url = this.form.id ? `/api/teacher/notices/${this.form.id}` : '/api/teacher/notices';
            const method = this.form.id ? 'put' : 'post';
            
            const res = await this.$http[method](url, this.form);
            if (res.data.code === 200) {
              this.$message.success(this.form.id ? '更新成功' : '添加成功');
              this.dialogVisible = false;
              this.fetchNoticeList();
            } else {
              this.$message.error(res.data.msg || '操作失败');
            }
          } catch (error) {
            console.error('操作失败:', error);
            this.$message.error(error.response?.data?.msg || '操作失败');
          }
        }
      });
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确认删除该通知吗？', '提示', {
          type: 'warning'
        })
        const res = await this.$http.delete(`/api/teacher/notices/${row.id}`)
        if (res.data.code === 200) {
          this.$message.success('删除成功')
          this.fetchNoticeList()
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    async handleStatusChange(row) {
      try {
        const res = await this.$http.put(`/api/teacher/notices/${row.id}/status`, {
          status: row.status
        })
        if (res.data.code === 200) {
          this.$message.success(`${row.status === 1 ? '启用' : '禁用'}成功`)
        } else {
          row.status = row.status === 1 ? 0 : 1 // 恢复状态
          this.$message.error(res.data.msg || '操作失败')
        }
      } catch (error) {
        row.status = row.status === 1 ? 0 : 1 // 恢复状态
        this.$message.error('操作失败')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.notice-management {
  padding: 20px;

  .operation-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .search-area {
      display: flex;
      align-items: center;
    }
  }

  .important-notice {
    color: #f56c6c;
    font-weight: bold;
  }

  .operation-group {
    .el-button {
      margin-left: 0;
    }
  }

  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }

  .notice-header {
    margin-bottom: 20px;

    .notice-type {
      margin-bottom: 10px;
    }

    .notice-title {
      margin: 0;
      font-size: 20px;
      font-weight: bold;
    }
  }

  .notice-meta {
    margin-bottom: 20px;
    color: #909399;
    font-size: 14px;

    .notice-priority {
      margin-left: 20px;
      color: #f56c6c;

      .el-icon-warning {
        margin-right: 5px;
      }
    }
  }

  .notice-content {
    line-height: 1.6;
    white-space: pre-wrap;
  }
}
</style>