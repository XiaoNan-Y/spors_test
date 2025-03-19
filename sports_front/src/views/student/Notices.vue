<template>
  <div class="notices">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>通知公告</span>
      </div>

      <el-table :data="notices" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="type" label="类型">
          <template slot-scope="scope">
            <el-tag :type="getNoticeTypeTag(scope.row.type)">
              {{ getNoticeTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button type="text" @click="viewNotice(scope.row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="query.page"
          :page-sizes="[10, 20, 50]"
          :page-size="query.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>

    <!-- 通知详情对话框 -->
    <el-dialog :title="currentNotice.title" :visible.sync="dialogVisible" width="600px">
      <div class="notice-info">
        <p class="notice-meta">
          <span>类型：{{ getNoticeTypeText(currentNotice.type) }}</span>
          <span>发布时间：{{ formatDate(currentNotice.createTime) }}</span>
        </p>
        <div class="notice-content" v-html="currentNotice.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { formatDate } from '@/utils/date'

export default {
  name: 'StudentNotices',
  data() {
    return {
      notices: [],
      loading: false,
      total: 0,
      query: {
        page: 1,
        size: 10
      },
      dialogVisible: false,
      currentNotice: {}
    }
  },
  created() {
    this.fetchNotices()
  },
  methods: {
    async fetchNotices() {
      this.loading = true
      try {
        const response = await this.$http.get('/api/notices', {
          params: {
            page: this.query.page - 1,
            size: this.query.size
          }
        })
        if (response.data.code === 200) {
          this.notices = response.data.data.content
          this.total = response.data.data.totalElements
        }
      } catch (error) {
        console.error('获取通知列表失败:', error)
        this.$message.error('获取通知列表失败')
      } finally {
        this.loading = false
      }
    },
    viewNotice(notice) {
      this.currentNotice = notice
      this.dialogVisible = true
    },
    handleSizeChange(val) {
      this.query.size = val
      this.fetchNotices()
    },
    handleCurrentChange(val) {
      this.query.page = val
      this.fetchNotices()
    },
    formatDate,
    getNoticeTypeTag(type) {
      const types = {
        TEST_SCHEDULE: 'primary',
        SCORE_RELEASE: 'success',
        SYSTEM_MAINTENANCE: 'warning',
        OTHER: 'info'
      }
      return types[type] || 'info'
    },
    getNoticeTypeText(type) {
      const texts = {
        TEST_SCHEDULE: '体测安排',
        SCORE_RELEASE: '成绩公布',
        SYSTEM_MAINTENANCE: '系统维护',
        OTHER: '其他'
      }
      return texts[type] || '其他'
    }
  }
}
</script>

<style scoped>
.notices {
  padding: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.notice-info {
  padding: 0 20px;
}

.notice-meta {
  color: #909399;
  margin-bottom: 20px;
}

.notice-meta span {
  margin-right: 20px;
}

.notice-content {
  line-height: 1.6;
}
</style> 