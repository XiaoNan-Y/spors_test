<template>
  <div class="student-records">
    <!-- 搜索筛选区 -->
    <div class="filter-section">
      <el-form :inline="true" :model="queryParams" ref="queryForm">
        <el-form-item label="班级">
          <el-select v-model="queryParams.className" placeholder="选择班级" clearable>
            <el-option
              v-for="className in classNames"
              :key="className"
              :label="className"
              :value="className"
            ></el-option>
          </el-select>
        </el-form-item>
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
        <el-form-item label="学号">
          <el-input 
            v-model="queryParams.studentNumber" 
            placeholder="请输入学号"
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
      style="width: 100%"
      border>
      <el-table-column type="index" label="序号" width="60"></el-table-column>
      <el-table-column prop="studentInfo.realName" label="学生姓名"></el-table-column>
      <el-table-column prop="studentInfo.studentNumber" label="学号"></el-table-column>
      <el-table-column prop="studentInfo.className" label="班级"></el-table-column>
      <el-table-column prop="sportsItem.name" label="测试项目"></el-table-column>
      <el-table-column prop="score" label="成绩"></el-table-column>
      <el-table-column label="操作" width="180" align="center">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="handleEdit(scope.row)"
            :disabled="!canEdit(scope.row)">
            修改
          </el-button>
          <el-button
            size="mini"
            type="info"
            @click="handleDetail(scope.row)">
            详情
          </el-button>
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

    <!-- 添加修改对话框 -->
    <el-dialog
      title="修改成绩"
      :visible.sync="editDialog.visible"
      width="500px"
      @closed="handleEditDialogClosed">
      <el-form
        :model="editForm"
        :rules="editRules"
        ref="editForm"
        label-width="100px">
        <el-form-item label="学生姓名">
          <span>{{ editForm.studentName }}</span>
        </el-form-item>
        <el-form-item label="测试项目">
          <span>{{ editForm.sportsItemName }}</span>
        </el-form-item>
        <el-form-item label="成绩" prop="score">
          <el-input v-model.number="editForm.score" type="number"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="submitEdit">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 添加详情对话框 -->
    <el-dialog
      title="成绩详情"
      :visible.sync="detailDialog.visible"
      width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="学生姓名">{{ detailForm.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailForm.studentNumber }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ detailForm.className }}</el-descriptions-item>
        <el-descriptions-item label="测试项目">{{ detailForm.sportsItemName }}</el-descriptions-item>
        <el-descriptions-item label="成绩">{{ detailForm.score }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailForm.status)">
            {{ getStatusText(detailForm.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailForm.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailForm.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'StudentRecords',
  data() {
    return {
      loading: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        className: '',
        sportsItemId: undefined,
        status: '',
        studentNumber: ''
      },
      classNames: [],
      sportsItems: [],
      tableData: [],
      total: 0,
      editDialog: {
        visible: false
      },
      editForm: {
        id: null,
        studentName: '',
        sportsItemName: '',
        score: null
      },
      editRules: {
        score: [
          { required: true, message: '请输入成绩', trigger: 'blur' },
          { type: 'number', message: '成绩必须为数字', trigger: 'blur' }
        ]
      },
      detailDialog: {
        visible: false
      },
      detailForm: {
        studentName: '',
        studentNumber: '',
        className: '',
        sportsItemName: '',
        score: null,
        status: '',
        createdAt: '',
        updatedAt: ''
      }
    }
  },
  created() {
    this.init()
  },
  methods: {
    async init() {
      await Promise.all([
        this.getClassNames(),
        this.getSportsItems(),
        this.getList()
      ])
    },
    async getList() {
      try {
        this.loading = true
        const response = await this.$axios.get('/api/teacher/student-records', {
          params: {
            page: this.queryParams.pageNum - 1,
            size: this.queryParams.pageSize,
            className: this.queryParams.className,
            sportsItemId: this.queryParams.sportsItemId,
            status: this.queryParams.status,
            keyword: this.queryParams.studentNumber
          }
        })
        
        if (response.data.code === 200) {
          this.tableData = response.data.data.content
          this.total = response.data.data.totalElements
        }
      } catch (error) {
        console.error('获取列表失败:', error)
        this.$message.error('获取列表失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    async getClassNames() {
      try {
        const res = await this.$axios.get('/api/teacher/classes')
        if (res.data.code === 200) {
          this.classNames = res.data.data
        }
      } catch (error) {
        console.error('获取班级列表失败:', error)
      }
    },
    async getSportsItems() {
      try {
        const res = await this.$axios.get('/api/teacher/sports-items')
        if (res.data.code === 200) {
          this.sportsItems = res.data.data
        }
      } catch (error) {
        console.error('获取体测项目列表失败:', error)
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
        className: '',
        sportsItemId: undefined,
        status: '',
        studentNumber: ''
      }
      this.getList()
    },
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.getList()
    },
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.getList()
    },
    getStatusType(status) {
      const statusMap = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return statusMap[status] || 'info'
    },
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
      }
      return statusMap[status] || status
    },
    canEdit(row) {
      return row.status === 'PENDING' || row.status === 'REJECTED'
    },
    handleEdit(row) {
      this.editForm = {
        id: row.id,
        studentName: row.studentInfo.realName,
        sportsItemName: row.sportsItem.name,
        score: row.score
      }
      this.editDialog.visible = true
    },
    handleDetail(row) {
      this.detailForm = {
        studentName: row.studentInfo.realName,
        studentNumber: row.studentInfo.studentNumber,
        className: row.studentInfo.className,
        sportsItemName: row.sportsItem.name,
        score: row.score,
        status: row.status,
        createdAt: row.createdAt,
        updatedAt: row.updatedAt
      }
      this.detailDialog.visible = true
    },
    submitEdit() {
      this.$refs.editForm.validate(async (valid) => {
        if (valid) {
          try {
            console.log('开始提交修改请求，表单数据：', this.editForm);
            
            // 确保成绩是数字类型
            const score = parseFloat(this.editForm.score);
            if (isNaN(score)) {
              this.$message.error('请输入有效的成绩');
              return;
            }

            console.log('发送请求前的数据：', {
              id: this.editForm.id,
              score: score
            });

            const response = await this.$axios.put(
              `/api/teacher/test-records/${this.editForm.id}`,
              { score: score },
              {
                headers: {
                  'Content-Type': 'application/json'
                }
              }
            );
            
            console.log('服务器响应：', response);

            if (response.data.code === 200) {
              this.$message.success('修改成功');
              this.editDialog.visible = false;
              this.getList();
            } else {
              this.$message.error(response.data.message || '修改失败');
            }
          } catch (error) {
            console.error('修改失败，详细错误信息：', {
              error: error,
              response: error.response,
              message: error.message,
              stack: error.stack
            });

            let errorMessage = '修改失败';
            if (error.response) {
              console.log('服务器响应状态：', error.response.status);
              console.log('服务器响应数据：', error.response.data);
              errorMessage = error.response.data.message || errorMessage;
            }

            this.$message.error(errorMessage);
          }
        } else {
          console.log('表单验证失败');
          this.$message.warning('请正确填写表单');
        }
      });
    },
    handleEditDialogClosed() {
      this.$refs.editForm?.resetFields()
      this.editForm = {
        id: null,
        studentName: '',
        sportsItemName: '',
        score: null
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
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
.dialog-footer {
  text-align: right;
}
</style> 