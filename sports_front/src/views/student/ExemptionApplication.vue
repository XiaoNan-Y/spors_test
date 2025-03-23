<template>
  <div class="exemption-application">
    <!-- 提示信息 -->
    <el-alert
      title="如果您因特殊原因需要申请免测或重测，请在此提交申请。我们会尽快处理您的申请。"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 20px;"
    />

    <!-- 提交申请按钮 -->
    <el-button type="primary" @click="showDialog">提交申请</el-button>

    <!-- 申请记录表格 -->
    <div class="application-history" v-if="applications.length">
      <h3>申请记录</h3>
      <el-table :data="applications" border style="width: 100%">
        <el-table-column prop="applyTime" label="申请时间" width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="申请类型" width="100">
          <template slot-scope="scope">
            {{ scope.row.type === 'EXEMPTION' ? '免测' : '重测' }}
          </template>
        </el-table-column>
        <el-table-column prop="sportsItemName" label="申请项目" width="120">
          <template slot-scope="scope">
            {{ scope.row.sportsItemName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="申请原因" show-overflow-tooltip />
        <el-table-column label="证明材料" width="120" align="center">
          <template slot-scope="scope">
            <el-link 
              v-if="scope.row.attachmentUrl"
              type="primary" 
              @click="previewAttachment(scope.row.attachmentUrl)"
            >
              查看附件
            </el-link>
            <span v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核意见" show-overflow-tooltip>
          <template slot-scope="scope">
            <div v-if="scope.row.adminReviewComment">
              {{ scope.row.adminReviewComment }}
            </div>
            <div v-else-if="scope.row.teacherReviewComment">
              {{ scope.row.teacherReviewComment }}
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.status === 'PENDING'"
              size="mini"
              type="danger"
              @click="handleDelete(scope.row)"
            >
              撤销
            </el-button>
            <el-button
              v-else
              size="mini"
              type="info"
              @click="handleDetail(scope.row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页器 -->
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page.sync="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </div>

    <!-- 申请表单对话框 -->
    <el-dialog title="提交申请" :visible.sync="dialogVisible" width="50%">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <!-- 申请类型 -->
        <el-form-item label="申请类型" prop="type">
          <el-radio-group v-model="form.type" @change="handleTypeChange">
            <el-radio label="EXEMPTION">免测申请</el-radio>
            <el-radio label="RETEST">重测申请</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 申请项目，只在重测时显示 -->
        <el-form-item 
          v-if="form.type === 'RETEST'"
          label="申请项目" 
          prop="sportsItemId"
        >
          <el-select v-model="form.sportsItemId" placeholder="请选择申请项目">
            <el-option
              v-for="item in sportsItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <!-- 申请原因 -->
        <el-form-item label="申请原因" prop="reason">
          <el-input
            type="textarea"
            v-model="form.reason"
            :rows="4"
            :placeholder="form.type === 'EXEMPTION' ? 
              '请详细说明免测申请原因，并说明相关情况' : 
              '请说明重测原因'"
          />
        </el-form-item>

        <!-- 证明材料上传 -->
        <el-form-item 
          v-if="form.type === 'EXEMPTION'"
          label="证明材料" 
          prop="attachmentUrl"
          :required="true"
        >
          <el-upload
            class="upload-demo"
            action="/api/upload/file"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :on-remove="handleRemove"
            :file-list="fileList"
            :limit="1"
          >
            <el-button size="small" type="primary">点击上传</el-button>
            <div slot="tip" class="el-upload__tip">
              必须上传证明材料（医院证明等），只能上传jpg/png/pdf文件，且不超过10MB
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      title="申请详情"
      :visible.sync="detailDialogVisible"
      width="600px"
      class="detail-dialog"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="申请类型">
          {{ currentRecord?.type === 'EXEMPTION' ? '免测申请' : '重测申请' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDateTime(currentRecord?.applyTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="申请原因">
          {{ currentRecord?.reason }}
        </el-descriptions-item>
        <el-descriptions-item label="证明材料">
          <el-button 
            v-if="currentRecord?.attachmentUrl"
            type="text" 
            @click="previewAttachment(currentRecord.attachmentUrl)"
          >
            查看附件
          </el-button>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRecord?.status)">
            {{ getStatusText(currentRecord?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见">
          {{ currentRecord?.adminReviewComment || currentRecord?.teacherReviewComment || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核时间">
          {{ currentRecord?.adminReviewTime ? 
            formatDateTime(currentRecord.adminReviewTime) : 
            (currentRecord?.teacherReviewTime ? 
              formatDateTime(currentRecord.teacherReviewTime) : '-') }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ExemptionApplication',
  data() {
    // 自定义验证规则：免测申请必须上传附件
    const validateAttachment = (rule, value, callback) => {
      if (this.form.type === 'EXEMPTION' && !this.form.attachmentUrl) {
        callback(new Error('免测申请必须上传证明材料'));
      } else {
        callback();
      }
    };

    return {
      dialogVisible: false,
      form: {
        type: 'EXEMPTION',
        reason: '',
        attachmentUrl: '',
        sportsItemId: null
      },
      rules: {
        type: [{ required: true, message: '请选择申请类型' }],
        reason: [{ required: true, message: '请填写申请原因', trigger: 'blur' }],
        attachmentUrl: [{ validator: validateAttachment, trigger: 'change' }],
        sportsItemId: [{ required: true, message: '请选择申请项目' }]
      },
      applications: [],
      fileList: [],
      uploadHeaders: {
        Authorization: 'Bearer ' + localStorage.getItem('token')
      },
      currentPage: 1,
      pageSize: 10,
      total: 0,
      sportsItems: [], // 体育项目列表
      detailDialogVisible: false, // 添加这个控制详情对话框的显示
      currentRecord: null, // 当前查看的记录
    }
  },
  methods: {
    handleTypeChange(value) {
      // 重置表单相关字段
      this.form.attachmentUrl = '';
      this.form.sportsItemId = null;
      this.fileList = [];

      // 重新设置验证规则
      if (value === 'RETEST') {
        this.$set(this.rules, 'sportsItemId', [
          { required: true, message: '请选择申请项目', trigger: 'change' }
        ]);
      } else {
        // 免测时移除体育项目的验证规则
        this.$delete(this.rules, 'sportsItemId');
      }

      // 重新验证表单
      this.$nextTick(() => {
        this.$refs.form.validateField('attachmentUrl');
      });
    },
    showDialog() {
      this.dialogVisible = true;
      this.resetForm();
    },
    async submitForm() {
      try {
        await this.$refs.form.validate();
        
        // 构造申请数据
        const applicationData = {
          ...this.form,
          studentId: localStorage.getItem('userId'),
          // 根据申请类型设置不同的API路径
          type: this.form.type, // 确保type正确传递
          sportsItemId: this.form.type === 'RETEST' ? this.form.sportsItemId : null,
          sportsItemName: this.form.type === 'RETEST' ? 
            this.sportsItems.find(item => item.id === this.form.sportsItemId)?.name : null
        };

        // 根据申请类型选择不同的API端点
        const apiEndpoint = this.form.type === 'RETEST' ? 
          '/api/exemptions/student/submit-retest' : 
          '/api/exemptions/student/submit';

        const response = await this.$http.post(apiEndpoint, applicationData);

        if (response.data.code === 200) {
          this.$message.success('提交成功');
          this.dialogVisible = false;
          this.fetchApplications();
        }
      } catch (error) {
        console.error('提交失败:', error);
        this.$message.error('提交失败: ' + error.message);
      }
    },
    resetForm() {
      if (this.$refs.form) {
        this.$refs.form.resetFields();
      }
      this.fileList = [];
      this.form.attachmentUrl = '';
    },
    handleUploadSuccess(response) {
      if (response.code === 200) {
        this.form.attachmentUrl = response.data;
        this.$message.success('上传成功');
      } else {
        this.$message.error(response.message || '上传失败');
      }
    },
    handleUploadError(err) {
      console.error('上传失败:', err);
      this.$message.error('上传失败: ' + (err.message || '未知错误'));
    },
    handleRemove() {
      this.form.attachmentUrl = '';
      this.fileList = [];
    },
    beforeUpload(file) {
      const isValidType = ['image/jpeg', 'image/png', 'application/pdf'].includes(file.type);
      const isLt10M = file.size / 1024 / 1024 < 10;

      if (!isValidType) {
        this.$message.error('只能上传JPG/PNG/PDF文件!');
        return false;
      }
      if (!isLt10M) {
        this.$message.error('文件大小不能超过10MB!');
        return false;
      }
      return true;
    },
    async fetchApplications() {
      try {
        console.log('Fetching applications for user:', localStorage.getItem('userId'));
        
        // 修改请求路径，使用相对路径
        const [exemptionResponse, retestResponse] = await Promise.all([
          this.$http.get('/api/exemptions/student/list', {
            params: {
              studentId: localStorage.getItem('userId'),
              page: this.currentPage - 1,
              size: this.pageSize
            }
          }),
          this.$http.get('/api/exemptions/retest-applications/student/list', { // 修改这里的路径
            params: {
              studentId: localStorage.getItem('userId'),
              page: this.currentPage - 1,
              size: this.pageSize
            }
          })
        ]);

        if (exemptionResponse.data.code === 200 && retestResponse.data.code === 200) {
          // 合并两种申请记录
          const exemptionApplications = exemptionResponse.data.data.content || [];
          const retestApplications = retestResponse.data.data.content || [];
          
          // 合并并按申请时间排序
          this.applications = [...exemptionApplications, ...retestApplications]
            .sort((a, b) => new Date(b.applyTime) - new Date(a.applyTime));
          
          // 计算总记录数
          this.total = exemptionResponse.data.data.totalElements + 
                      retestResponse.data.data.totalElements;
          
          console.log('Loaded applications:', this.applications);
          console.log('Total records:', this.total);
        } else {
          this.$message.error('获取申请记录失败');
        }
      } catch (error) {
        console.error('获取申请记录失败:', error);
        this.$message.error('获取申请记录失败: ' + error.message);
      }
    },
    getStatusType(status) {
      const types = {
        PENDING: 'warning',
        APPROVED: 'success',
        REJECTED: 'danger'
      };
      return types[status] || 'info';
    },
    getStatusText(status) {
      const texts = {
        PENDING: '待审核',
        APPROVED: '已通过',
        REJECTED: '已驳回'
      };
      return texts[status] || status;
    },
    formatDateTime(dateTime) {
      if (!dateTime) return '-';
      return new Date(dateTime).toLocaleString();
    },
    previewAttachment(url) {
      if (!url) return;
      window.open(url, '_blank');
    },
    handleSizeChange(newSize) {
      this.pageSize = newSize;
      this.fetchApplications();
    },
    handleCurrentChange(newPage) {
      this.currentPage = newPage;
      this.fetchApplications();
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确认撤销该申请吗？', '提示', {
          type: 'warning'
        });
        
        const response = await this.$http.delete(`/api/exemptions/student/${row.id}`, {
          params: {
            studentId: localStorage.getItem('userId')
          }
        });
        
        if (response.data.code === 200) {
          this.$message.success('撤销成功');
          this.fetchApplications(); // 刷新列表
        } else {
          this.$message.error(response.data.message || '撤销失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('撤销申请失败:', error);
          this.$message.error('撤销失败: ' + error.message);
        }
      }
    },
    handleDetail(row) {
      this.currentRecord = row;
      this.detailDialogVisible = true;
    },
    // 获取体育项目列表
    async fetchSportsItems() {
      try {
        const response = await this.$http.get('/api/sports-items/student/list');
        if (response.data.code === 200) {
          this.sportsItems = response.data.data;
        }
      } catch (error) {
        console.error('获取体育项目列表失败:', error);
        this.$message.error('获取体育项目列表失败');
      }
    }
  },
  async created() {
    // 确保在组件创建时获取数据
    await this.fetchApplications();
    await this.fetchSportsItems();
    
    // 添加调试日志
    console.log('Component created, applications:', this.applications);
  }
}
</script>

<style scoped>
.exemption-application {
  padding: 20px;
}

.application-history {
  margin-top: 30px;
}

.el-upload {
  width: 100%;
}

.upload-demo {
  margin-bottom: 10px;
}

.el-upload__tip {
  line-height: 1.5;
  color: #666;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.detail-dialog {
  .el-descriptions {
    margin: 20px 0;
  }
  
  .el-descriptions-item__label {
    width: 120px;
    color: #606266;
  }
  
  .el-descriptions-item__content {
    color: #303133;
  }
}
</style> 