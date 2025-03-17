<template>
  <div class="notices-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>通知公告</span>
      </div>
      <el-table
        v-loading="loading"
        :data="notices"
        style="width: 100%">
        <el-table-column
          prop="title"
          label="标题"
          width="180">
        </el-table-column>
        <el-table-column
          prop="content"
          label="内容">
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="发布时间"
          width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          label="操作"
          width="120">
          <template slot-scope="scope">
            <el-button
              @click="viewNoticeDetail(scope.row)"
              type="text"
              size="small">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>

      <!-- 通知详情对话框 -->
      <el-dialog
        title="通知详情"
        :visible.sync="dialogVisible"
        width="50%">
        <div v-if="currentNotice">
          <h3>{{ currentNotice.title }}</h3>
          <div class="notice-meta">
            <span>发布时间：{{ formatDate(currentNotice.createTime) }}</span>
          </div>
          <div class="notice-content">
            {{ currentNotice.content }}
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'
import moment from 'moment'

export default {
  name: 'Notices',
  data() {
    return {
      loading: false,
      notices: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      currentNotice: null
    }
  },
  created() {
    this.fetchNotices()
  },
  methods: {
    async fetchNotices() {
      this.loading = true
      try {
        const response = await axios.get('/api/notices', {
          params: {
            page: this.currentPage - 1,
            size: this.pageSize
          }
        })
        this.notices = response.data.content
        this.total = response.data.totalElements
      } catch (error) {
        this.$message.error('获取通知列表失败')
        console.error('Error fetching notices:', error)
      } finally {
        this.loading = false
      }
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchNotices()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchNotices()
    },
    formatDate(date) {
      return moment(date).format('YYYY-MM-DD HH:mm')
    },
    viewNoticeDetail(notice) {
      this.currentNotice = notice
      this.dialogVisible = true
    }
  }
}
</script>

<style scoped>
.notices-container {
  padding: 20px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
.notice-meta {
  color: #666;
  margin: 10px 0;
  font-size: 14px;
}
.notice-content {
  margin-top: 20px;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>