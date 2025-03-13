<template>
  <div class="student-records">
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
      :data="tableData"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
      
      <el-table-column label="学生姓名" prop="studentName" min-width="120" align="center"></el-table-column>
      
      <el-table-column label="学号" prop="studentNumber" min-width="120" align="center"></el-table-column>
      
      <el-table-column label="班级" prop="className" min-width="120" align="center"></el-table-column>
      
      <el-table-column label="测试项目" prop="sportsItem.name" min-width="120" align="center"></el-table-column>
      
      <el-table-column label="成绩" min-width="120" align="center">
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
        :current-page="queryParams.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="queryParams.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      ></el-pagination>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialog.visible" width="500px">
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
              v-for="className in classList"
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
      tableData: [],
      total: 0,
      queryParams: {
        className: '',
        sportsItemId: null,
        keyword: '',
        pageNum: 1,
        pageSize: 10
      },
      sportsItems: [],
      classList: [],
      studentList: [],
      dialog: {
        visible: false
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
    dialogTitle() {
      return this.form.id ? '修改成绩' : '录入成绩'
    },
    selectedItemUnit() {
      const item = this.sportsItems.find(item => item.id === this.form.sportsItemId)
      return item ? item.unit : ''
    }
  },
  created() {
    this.init()
  },
  methods: {
    async init() {
      await Promise.all([
        this.getSportsItems(),
        this.getClassList(),
        this.getList()
      ])
    },
    async getList() {
      try {
        this.loading = true;
        const res = await this.$http.get('/api/teacher/student-records', {
          params: {
            className: this.queryParams.className,
            sportsItemId: this.queryParams.sportsItemId,
            keyword: this.queryParams.keyword,
            page: this.queryParams.pageNum - 1,  // 注意这里需要减1，因为后端从0开始计数
            size: this.queryParams.pageSize
          }
        });
        
        if (res.data.code === 200) {
          this.tableData = res.data.data.content;
          this.total = res.data.data.totalElements;
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
    async getSportsItems() {
      try {
        const res = await getSportsItems()
        if (res.data.code === 200) {
          this.sportsItems = res.data.data
        }
      } catch (error) {
        console.error('获取体育项目列表失败:', error)
        this.$message.error('获取体育项目列表失败')
      }
    },
    async getClassList() {
      try {
        const res = await getClassList()
        if (res.data.code === 200) {
          this.classList = res.data.data
        }
      } catch (error) {
        console.error('获取班级列表失败:', error)
        this.$message.error('获取班级列表失败')
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.$refs.queryForm.resetFields()
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        sportsItemId: undefined,
        className: undefined,
        keyword: ''
      }
      this.getList()
    },
    handleAdd() {
      this.form = {
        id: undefined,
        studentNumber: undefined,
        className: undefined,
        sportsItemId: undefined,
        score: undefined
      }
      this.dialog.visible = true
    },
    handleEdit(row) {
      this.form = {
        id: row.id,
        studentNumber: row.studentNumber,
        className: row.className,
        sportsItemId: row.sportsItemId,
        score: row.score
      }
      this.dialog.visible = true
    },
    handleDelete(row) {
      this.$confirm('确认删除该记录吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteTestRecord(row.id)
          if (res.data.code === 200) {
            this.$message.success('删除成功')
            this.getList()
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
            const submitFunc = this.form.id ? updateTestRecord : addTestRecord
            const res = await submitFunc(this.form.id, this.form)
            if (res.data.code === 200) {
              this.$message.success(this.form.id ? '修改成功' : '添加成功')
              this.dialog.visible = false
              this.getList()
            }
          } catch (error) {
            console.error('提交失败:', error)
            this.$message.error('提交失败')
          }
        }
      })
    },
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.getList()
    },
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.getList()
    },
    async handleExport() {
      try {
        this.$message.info('正在导出数据，请稍候...');
        
        const res = await this.$http.get('/api/teacher/test-records/export', {
          params: {
            sportsItemId: this.queryParams.sportsItemId,
            className: this.queryParams.className,
            keyword: this.queryParams.keyword
          },
          responseType: 'blob'  // 重要：指定响应类型为blob
        });
        
        // 创建下载链接
        const blob = new Blob([res.data], { 
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        
        // 生成文件名
        const now = new Date();
        const fileName = `学生成绩记录_${now.getFullYear()}${(now.getMonth()+1).toString().padStart(2,'0')}${now.getDate().toString().padStart(2,'0')}.xlsx`;
        link.setAttribute('download', fileName);
        
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(link.href);
        
        this.$message.success('导出成功');
      } catch (error) {
        console.error('导出失败:', error);
        this.$message.error('导出失败');
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