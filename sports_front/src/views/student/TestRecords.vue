<template>
  <div class="test-records-container">
    <el-card>
      <div slot="header" class="card-header">
        <span>体测成绩记录</span>
        <el-select v-model="selectedSemester" placeholder="选择学期" size="small" @change="fetchRecords">
          <el-option
            v-for="item in semesters"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </div>

      <el-table :data="testRecords" style="width: 100%">
        <el-table-column prop="itemName" label="测试项目"></el-table-column>
        <el-table-column prop="testDate" label="测试日期" width="180"></el-table-column>
        <el-table-column prop="score" label="成绩" width="100">
          <template slot-scope="scope">
            <span :class="{'score-pass': scope.row.isPassed, 'score-fail': !scope.row.isPassed}">
              {{ scope.row.score }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="standard" label="达标要求" width="150"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isPassed ? 'success' : 'danger'">
              {{ scope.row.isPassed ? '达标' : '未达标' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button type="text" @click="viewDetail(scope.row)">查看详情</el-button>
            <el-button type="text" @click="submitAppeal(scope.row)" v-if="!scope.row.hasAppeal">
              成绩申诉
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 成绩详情对话框 -->
    <el-dialog title="成绩详情" :visible.sync="detailDialogVisible" width="40%">
      <div class="detail-content" v-if="currentRecord">
        <div class="detail-item">
          <span class="label">测试项目：</span>
          <span>{{ currentRecord.itemName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">测试时间：</span>
          <span>{{ currentRecord.testDate }}</span>
        </div>
        <div class="detail-item">
          <span class="label">测试地点：</span>
          <span>{{ currentRecord.location }}</span>
        </div>
        <div class="detail-item">
          <span class="label">测试成绩：</span>
          <span :class="{'score-pass': currentRecord.isPassed, 'score-fail': !currentRecord.isPassed}">
            {{ currentRecord.score }}
          </span>
        </div>
        <div class="detail-item">
          <span class="label">评分标准：</span>
          <span>{{ currentRecord.standard }}</span>
        </div>
        <div class="detail-item">
          <span class="label">评分说明：</span>
          <p>{{ currentRecord.scoreDescription }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- 成绩申诉对话框 -->
    <el-dialog title="成绩申诉" :visible.sync="appealDialogVisible" width="50%">
      <el-form :model="appealForm" :rules="appealRules" ref="appealForm" label-width="100px">
        <el-form-item label="申诉理由" prop="reason">
          <el-input type="textarea" v-model="appealForm.reason" :rows="4"
                    placeholder="请详细说明申诉理由"></el-input>
        </el-form-item>
        <el-form-item label="证据材料" prop="evidence">
          <el-upload
            class="upload-demo"
            action="/api/upload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :file-list="appealForm.evidenceFiles">
            <el-button size="small" type="primary">上传文件</el-button>
            <div slot="tip" class="el-upload__tip">可上传图片、视频等证据材料</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="appealDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAppealForm">提交申诉</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TestRecords',
  data() {
    return {
      selectedSemester: '',
      semesters: [],
      testRecords: [],
      detailDialogVisible: false,
      appealDialogVisible: false,
      currentRecord: null,
      appealForm: {
        reason: '',
        evidenceFiles: []
      },
      appealRules: {
        reason: [
          { required: true, message: '请输入申诉理由', trigger: 'blur' },
          { min: 10, message: '申诉理由不能少于10个字', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchSemesters()
    this.fetchRecords()
  },
  methods: {
    async fetchSemesters() {
      try {
        const response = await this.$http.get('/api/student/semesters')
        this.semesters = response.data
        if (this.semesters.length > 0) {
          this.selectedSemester = this.semesters[0].value
        }
      } catch (error) {
        this.$message.error('获取学期信息失败')
      }
    },
    async fetchRecords() {
      try {
        const response = await this.$http.get('/api/student/test-records', {
          params: { semester: this.selectedSemester }
        })
        this.testRecords = response.data
      } catch (error) {
        this.$message.error('获取成绩记录失败')
      }
    },
    viewDetail(record) {
      this.currentRecord = record
      this.detailDialogVisible = true
    },
    submitAppeal(record) {
      this.currentRecord = record
      this.appealDialogVisible = true
      this.appealForm = {
        reason: '',
        evidenceFiles: []
      }
    },
    handleUploadSuccess(response, file, fileList) {
      this.appealForm.evidenceFiles = fileList
      this.$message.success('文件上传成功')
    },
    handleUploadError() {
      this.$message.error('文件上传失败')
    },
    async submitAppealForm() {
      try {
        await this.$refs.appealForm.validate()
        await this.$http.post(`/api/student/test-records/${this.currentRecord.id}/appeal`, {
          reason: this.appealForm.reason,
          evidenceFiles: this.appealForm.evidenceFiles.map(file => file.response.fileUrl)
        })
        this.$message.success('申诉提交成功')
        this.appealDialogVisible = false
        this.fetchRecords()
      } catch (error) {
        if (error.message) {
          this.$message.error(error.message)
        }
      }
    }
  }
}
</script>

<style scoped>
.test-records-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.score-pass {
  color: #67C23A;
  font-weight: bold;
}

.score-fail {
  color: #F56C6C;
  font-weight: bold;
}

.detail-content {
  padding: 20px;
}

.detail-item {
  margin-bottom: 15px;
}

.detail-item .label {
  font-weight: bold;
  margin-right: 10px;
  color: #606266;
}

.detail-item p {
  margin: 10px 0;
  line-height: 1.6;
  color: #303133;
}

.upload-demo {
  margin-top: 10px;
}
</style>