<template>
  <div class="test-record-review">
    <el-dialog
      title="测试记录审核"
      :visible.sync="visible"
      width="500px"
      @close="handleClose">
      <el-form :model="form" ref="form" label-width="100px">
        <el-form-item label="学生姓名">
          <span>{{ record.student ? record.student.realName : '' }}</span>
        </el-form-item>
        <el-form-item label="学号">
          <span>{{ record.studentNumber }}</span>
        </el-form-item>
        <el-form-item label="测试项目">
          <span>{{ record.sportsItem ? record.sportsItem.name : '' }}</span>
        </el-form-item>
        <el-form-item label="测试成绩">
          <span>{{ record.score }}</span>
        </el-form-item>
        <el-form-item label="测试时间">
          <span>{{ formatDateTime(record.testTime) }}</span>
        </el-form-item>
        <el-form-item label="审核状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="comment">
          <el-input
            type="textarea"
            v-model="form.comment"
            :rows="3"
            placeholder="请输入审核意见">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleClose">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { formatDateTime } from '@/utils/datetime'

export default {
  name: 'TestRecordReview',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    record: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      form: {
        status: '',
        comment: ''
      }
    }
  },
  methods: {
    formatDateTime,
    handleClose() {
      this.$emit('update:visible', false)
      this.resetForm()
    },
    resetForm() {
      this.form = {
        status: '',
        comment: ''
      }
    },
    async handleSubmit() {
      try {
        const response = await this.$axios.put(`/api/admin/test-record/review`, {
          id: this.record.id,
          status: this.form.status,
          comment: this.form.comment,
          reviewerId: this.$store.state.user.id // 假设存储了当前用户信息
        })
        
        if (response.data.code === 200) {
          this.$message.success('审核成功')
          this.$emit('success')
          this.handleClose()
        } else {
          this.$message.error(response.data.msg || '审核失败')
        }
      } catch (error) {
        this.$message.error('审核失败：' + error.message)
      }
    }
  },
  watch: {
    record: {
      handler(newVal) {
        if (newVal) {
          this.form.status = ''
          this.form.comment = ''
        }
      },
      immediate: true
    }
  }
}
</script>

<style scoped>
.test-record-review {
  .el-form-item {
    margin-bottom: 20px;
  }
}
</style> 