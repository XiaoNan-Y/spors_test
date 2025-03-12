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
        <el-form-item label="学号">
          <el-input v-model="queryParams.studentNumber" placeholder="请输入学号" clearable></el-input>
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
      
      <el-table-column label="学生姓名" prop="student.realName" min-width="120" align="center"></el-table-column>
      
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
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        sportsItemId: undefined,
        className: undefined,
        studentNumber: undefined
      },
      // 表格数据
      tableData: [],
      total: 0,
      // 体育项目列表
      sportsItems: [],
      // 班级列表
      classList: [],
      // 学生列表
      studentList: [],
      // 对话框控制
      dialog: {
        visible: false
      },
      // 表单数据
      form: {
        id: undefined,
        studentNumber: undefined,
        className: undefined,
        sportsItemId: undefined,
        score: undefined
      },
      // 表单验证规则
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
    this.getList()
    this.getClassList()
    this.getSportsItems()
  },
  methods: {
    // 获取列表数据
    async getList() {
      try {
        this.loading = true
        const res = await getTestRecords(this.queryParams)
        if (res.data.code === 200) {
          const { content, totalElements } = res.data.data
          this.tableData = content
          this.total = totalElements
        }
      } catch (error) {
        console.error('获取列表失败:', error)
        this.$message.error('获取列表失败')
      } finally {
        this.loading = false
      }
    },

    // 获取体育项目列表
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

    // 获取班级列表
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

    // 处理查询
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    // 重置查询
    resetQuery() {
      this.$refs.queryForm.resetFields()
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        sportsItemId: undefined,
        className: undefined,
        studentNumber: undefined
      }
      this.getList()
    },

    // 处理添加
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

    // 处理编辑
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

    // 处理删除
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

    // 提交表单
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

    // 处理分页大小变化
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.getList()
    },

    // 处理页码变化
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.getList()
    },

    // 处理导出
    handleExport() {
      // TODO: 实现导出功能
      this.$message.info('导出功能开发中')
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