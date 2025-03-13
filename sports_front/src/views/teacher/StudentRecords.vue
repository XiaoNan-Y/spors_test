<template>
  <div class="student-records">
    <!-- 搜索筛选区 -->
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="班级">
          <el-select v-model="filterForm.className" placeholder="选择班级" clearable>
            <el-option
              v-for="className in classNames"
              :key="className"
              :label="className"
              :value="className">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试项目">
          <el-select v-model="filterForm.sportsItemId" placeholder="选择测试项目" clearable>
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="filterForm.keyword"
            placeholder="输入学号或姓名搜索"
            clearable
            @keyup.enter.native="handleSearch">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区 -->
    <div class="operation-bar">
      <el-button type="primary" @click="handleAdd">
        <i class="el-icon-plus"></i> 录入成绩
      </el-button>
      <el-button type="success" @click="handleExport">
        <i class="el-icon-download"></i> 导出数据
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="records"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
      
      <el-table-column 
        label="学生姓名" 
        prop="studentName" 
        min-width="100" 
        align="center"
        :show-overflow-tooltip="true"
      ></el-table-column>
      
      <el-table-column 
        label="学号" 
        prop="studentNumber" 
        min-width="120" 
        align="center"
      ></el-table-column>
      
      <el-table-column 
        label="班级" 
        prop="className" 
        min-width="120" 
        align="center"
      ></el-table-column>
      
      <el-table-column 
        label="测试项目" 
        min-width="120" 
        align="center"
      >
        <template slot-scope="scope">
          {{ scope.row.sportsItem ? scope.row.sportsItem.name : '' }}
        </template>
      </el-table-column>
      
      <el-table-column 
        label="成绩" 
        min-width="120" 
        align="center"
      >
        <template slot-scope="scope">
          {{ scope.row.score }}{{ scope.row.sportsItem ? scope.row.sportsItem.unit : '' }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" align="center" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="handleEdit(scope.row)"
          >编辑</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)"
          >删除</el-button>
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
      ></el-pagination>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialog.title" :visible.sync="dialog.visible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="学生" prop="studentNumber">
          <el-select v-model="form.studentNumber" placeholder="请选择学生" filterable>
            <el-option
              v-for="student in studentList"
              :key="student.studentNumber"
              :label="`${student.realName} (${student.studentNumber})`"
              :value="student.studentNumber"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-select v-model="form.className" placeholder="请选择班级" style="width: 100%">
            <el-option
              v-for="className in classNames"
              :key="className"
              :label="className"
              :value="className"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试项目" prop="sportsItemId">
          <el-select v-model="form.sportsItemId" placeholder="请选择项目">
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="成绩" prop="score">
          <el-input v-model.number="form.score" type="number">
            <template slot="append">{{ selectedItemUnit }}</template>
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getTestRecords, addTestRecord, updateTestRecord, deleteTestRecord } from '@/api/teacher'
import { getSportsItems, getClassList } from '@/api/teacher'

export default {
  name: 'StudentRecords',
  data() {
    return {
      loading: false,
      records: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      classNames: [],
      sportsItems: [],
      filterForm: {
        className: '',
        sportsItemId: '',
        keyword: ''
      },
      studentList: [],
      dialog: {
        visible: false,
        title: '录入成绩'
      },
      form: {
        id: undefined,
        studentNumber: undefined,
        className: undefined,
        sportsItemId: undefined,
        score: undefined
      },
      rules: {
        studentNumber: [
          { required: true, message: '请选择学生', trigger: 'change' }
        ],
        className: [
          { required: true, message: '请选择班级', trigger: 'change' }
        ],
        sportsItemId: [
          { required: true, message: '请选择测试项目', trigger: 'change' }
        ],
        score: [
          { required: true, message: '请输入成绩', trigger: 'blur' },
          { type: 'number', message: '成绩必须为数字值', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    selectedItemUnit() {
      const item = this.sportsItems.find(item => item.id === this.form.sportsItemId)
      return item ? item.unit : ''
    }
  },
  created() {
    this.loadClassNames()
    this.loadSportsItems()
    this.loadRecords()
  },
  methods: {
    async loadClassNames() {
      try {
        const response = await this.$axios.get('/api/teacher/classes')
        if (response.data.code === 200) {
          this.classNames = response.data.data
        }
      } catch (error) {
        console.error('获取班级列表失败:', error)
        this.$message.error('获取班级列表失败')
      }
    },
    async loadSportsItems() {
      try {
        const response = await this.$axios.get('/api/teacher/sports-items')
        if (response.data.code === 200) {
          this.sportsItems = response.data.data
        }
      } catch (error) {
        console.error('获取体育项目列表失败:', error)
        this.$message.error('获取体育项目列表失败')
      }
    },
    async loadRecords() {
      this.loading = true;
      try {
        const params = {
          className: this.filterForm.className || '',
          sportsItemId: this.filterForm.sportsItemId || '',
          status: '',
          studentNumber: '',
          page: this.currentPage - 1,
          size: this.pageSize
        };
        
        console.log('发送请求参数:', params);
        
        const response = await this.$axios.get('/api/teacher/test-records', { params });
        
        if (response.data.code === 200) {
          this.records = response.data.data.content;
          this.total = response.data.data.totalElements;
          console.log('获取到的数据:', this.records);
        } else {
          this.$message.error(response.data.message || '获取数据失败');
        }
      } catch (error) {
        console.error('获取记录失败:', error);
        this.$message.error('获取记录失败');
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.loadRecords()
    },
    handleReset() {
      this.filterForm = {
        className: '',
        sportsItemId: '',
        keyword: ''
      }
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadRecords()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadRecords()
    },
    handleAdd() {
      this.form = {
        id: undefined,
        studentNumber: undefined,
        className: undefined,
        sportsItemId: undefined,
        score: undefined
      }
      this.dialog.title = '录入成绩'
      this.dialog.visible = true
    },
    handleEdit(row) {
      console.log('编辑的原始数据:', row);
      this.dialog.title = '修改成绩';
      this.form = {
        id: row.id,
        studentNumber: row.studentNumber,
        className: row.className,
        sportsItemId: row.sportsItemId,
        score: row.score,
        status: row.status || 'PENDING',
        studentName: row.studentName // 保存学生姓名
      };
      console.log('编辑表单数据:', this.form);
      this.dialog.visible = true;
    },
    handleDelete(row) {
      this.$confirm('确认删除该记录吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteTestRecord(row.id)
          if (res.data.code === 200) {
            this.$message.success('删除成功')
            this.loadRecords()
          }
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    submitForm() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            console.log('提交的表单数据:', this.form);
            
            // 构造提交的数据，只包含必要字段
            const submitData = {
              id: this.form.id,
              score: parseFloat(this.form.score),
              sportsItemId: this.form.sportsItemId,
              status: 'PENDING'
            };
            
            console.log('处理后的提交数据:', submitData);
            
            const url = this.form.id ? 
              `/api/teacher/test-records/${this.form.id}` : 
              '/api/teacher/test-records';
            const method = this.form.id ? 'put' : 'post';
            
            const response = await this.$axios[method](url, submitData);
            console.log('服务器响应:', response);
            
            if (response.data.code === 200) {
              this.$message.success(this.form.id ? '更新成功' : '添加成功');
              this.dialog.visible = false;
              await this.loadRecords();
            } else {
              throw new Error(response.data.message || '操作失败');
            }
          } catch (error) {
            console.error('提交失败:', error);
            let errorMsg = error.response?.data?.message || error.message;
            if (error.response?.data?.data) {
              errorMsg += '\n' + error.response.data.data;
            }
            this.$message.error('提交失败: ' + errorMsg);
          }
        }
      });
    },
    async handleExport() {
      try {
        this.$message.info('正在导出数据，请稍候...');
        
        // 构建完整的URL，包含查询参数
        const params = new URLSearchParams({
          className: this.filterForm.className || '',
          sportsItemId: this.filterForm.sportsItemId || '',
          keyword: this.filterForm.keyword || ''
        });
        
        // 直接使用window.open下载文件
        window.open(`/api/teacher/test-records/export?${params.toString()}`);
        
        this.$message.success('导出请求已发送，请等待浏览器下载');
      } catch (error) {
        console.error('导出失败:', error);
        this.$message.error('导出失败: ' + (error.message || '未知错误'));
      }
    }
  }
}
</script>

<style scoped>
.student-records {
  padding: 20px;
}

.filter-section {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.operation-bar {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 