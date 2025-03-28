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
            <el-option label="未测试" value="PENDING"></el-option>
            <el-option label="已测试" value="APPROVED"></el-option>
            <el-option label="免测" value="EXEMPTED"></el-option>
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
          <el-button type="success" @click="handleExport">导出成绩</el-button>
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
      <el-table-column label="测试状态" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button
            size="mini"
            type="primary"
            @click="handleEdit(scope.row)"
            :disabled="scope.row.status === 'EXEMPTED'"
          >
            {{ getActionButtonText(scope.row.status) }}
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
      :title="editDialog.title"
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
        visible: false,
        title: '修改成绩'
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
      const typeMap = {
        'APPROVED': 'success',
        'PENDING': 'warning',
        'EXEMPTED': 'info',
        'REJECTED': 'danger'
      };
      return typeMap[status] || 'warning';
    },
    getStatusText(status) {
      const statusMap = {
        'APPROVED': '已测试',
        'PENDING': '未测试',
        'EXEMPTED': '免测',
        'REJECTED': '未测试'
      };
      return statusMap[status] || '未测试';
    },
    getActionButtonText(status) {
      switch(status) {
        case 'APPROVED':
          return '修改';
        case 'PENDING':
        case 'REJECTED':
          return '录入';
        case 'EXEMPTED':
          return '免测';
        default:
          return '录入';
      }
    },
    handleEdit(row) {
      this.editForm = {
        id: row.id,
        studentName: row.studentInfo.realName,
        sportsItemName: row.sportsItem.name,
        score: row.score
      }
      this.editDialog.title = row.status === 'APPROVED' ? '修改成绩' : '录入成绩';
      this.editDialog.visible = true;
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
            const score = parseFloat(this.editForm.score);
            if (isNaN(score)) {
              this.$message.error('请输入有效的成绩');
              return;
            }

            // 根据成绩值决定状态
            const status = score === 0 ? 'PENDING' : 'APPROVED';

            console.log('提交数据:', {
              id: this.editForm.id,
              score: score,
              status: status
            });

            const response = await this.$axios.put(
              `/api/teacher/test-records/${this.editForm.id}`,
              { 
                score: score,
                status: status  // 根据成绩动态设置状态
              }
            );

            if (response.data.code === 200) {
              this.$message.success(this.editDialog.title === '修改成绩' ? '修改成功' : '录入成功');
              this.editDialog.visible = false;
              await this.getList();
            } else {
              this.$message.error(response.data.message || '操作失败');
            }
          } catch (error) {
            console.error('操作失败:', error);
            const errorMessage = error.response?.data?.message || '操作失败';
            this.$message.error(errorMessage);
          }
        } else {
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
    },
    async handleExport() {
      try {
        await this.$confirm('确认要导出当前筛选条件下的成绩数据吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        });

        const loading = this.$loading({
          lock: true,
          text: '正在导出数据，请稍候...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        try {
          // 构建查询参数
          const params = {
            className: this.queryParams.className || '',
            sportsItemId: this.queryParams.sportsItemId || '',
            status: this.queryParams.status || '',
            studentNumber: this.queryParams.studentNumber || ''
          };

          console.log('导出查询参数:', params);

          // 使用原生fetch API进行下载
          const response = await fetch(`/api/teacher/test-records/export?className=${encodeURIComponent(params.className)}&sportsItemId=${encodeURIComponent(params.sportsItemId || '')}&status=${encodeURIComponent(params.status)}&studentNumber=${encodeURIComponent(params.studentNumber)}`, {
            method: 'GET',
            headers: {
              'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
              'Authorization': `Bearer ${localStorage.getItem('token')}`,
              'userId': localStorage.getItem('userId')
            }
          });

          // 检查响应状态
          if (!response.ok) {
            const errorText = await response.text();
            console.error('服务器错误:', errorText);
            throw new Error(`服务器返回错误: ${response.status} ${response.statusText}`);
          }

          // 获取内容类型
          const contentType = response.headers.get('content-type');
          
          // 如果是JSON响应（错误消息）
          if (contentType && contentType.includes('application/json')) {
            const result = await response.json();
            this.$message.warning(result.message || '没有找到符合条件的记录');
            return;
          }

          // 如果是Excel文件
          const blob = await response.blob();
          
          // 获取文件名
          let fileName = '学生成绩记录.xlsx';
          const disposition = response.headers.get('content-disposition');
          if (disposition && disposition.includes('filename')) {
            const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = filenameRegex.exec(disposition);
            if (matches && matches[1]) {
              fileName = matches[1].replace(/['"]/g, '');
            }
          }

          // 创建下载链接
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = fileName;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);

          this.$message.success('导出成功');
        } catch (error) {
          console.error('导出过程出错:', error);
          this.$message.error('导出失败：' + (error.message || '未知错误'));
        } finally {
          loading.close();
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('导出操作失败:', error);
        }
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