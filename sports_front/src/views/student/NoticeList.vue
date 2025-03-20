<template>
  <div class="notice-list">
    <!-- 顶部搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索通知标题"
        style="width: 200px"
        @keyup.enter.native="handleSearch"
      >
        <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
      </el-input>
      <el-select 
        v-model="searchType" 
        placeholder="通知类型" 
        clearable
        style="width: 120px; margin-left: 10px"
      >
        <el-option label="全部" value=""></el-option>
        <el-option label="体测安排" value="TEST_SCHEDULE"></el-option>
        <el-option label="成绩公布" value="SCORE_RELEASE"></el-option>
        <el-option label="系统维护" value="SYSTEM_MAINTENANCE"></el-option>
        <el-option label="其他通知" value="OTHER"></el-option>
      </el-select>
    </div>

    <!-- 通知列表 -->
    <el-table
      :data="noticeList"
      border
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="type" label="类型" width="120">
        <template slot-scope="scope">
          <el-tag :type="getTypeTagType(scope.row.type)">
            {{ getTypeLabel(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" show-overflow-tooltip>
        <template slot-scope="scope">
          <span :class="{'important-notice': scope.row.priority === 'HIGH'}">
            {{ scope.row.title }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template slot-scope="scope">
          <el-button
            type="text"
            size="small"
            @click="handleView(scope.row)"
          >
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
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      >
      </el-pagination>
    </div>

    <!-- 查看通知详情的对话框 -->
    <el-dialog
      :title="viewingNotice.title"
      :visible.sync="dialogVisible"
      width="600px"
    >
      <div class="notice-detail">
        <div class="notice-header">
          <div class="notice-type">
            <el-tag :type="getTypeTagType(viewingNotice.type)">
              {{ getTypeLabel(viewingNotice.type) }}
            </el-tag>
          </div>
        </div>
        <div class="notice-meta">
          <span>发布时间：{{ formatDateTime(viewingNotice.createTime) }}</span>
          <span v-if="viewingNotice.priority === 'HIGH'" class="notice-priority">
            <i class="el-icon-warning"></i>重要通知
          </span>
        </div>
        <div class="notice-content">{{ viewingNotice.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'NoticeList',
  data() {
    return {
      searchKeyword: '',
      searchType: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      noticeList: [],
      dialogVisible: false,
      viewingNotice: {}
    }
  },
  created() {
    this.fetchNoticeList()
  },
  methods: {
    async fetchNoticeList() {
      try {
        this.loading = true;
        const response = await this.$axios.get('/api/student/notices', {
          params: {
            keyword: this.searchKeyword,
            type: this.searchType,
            page: this.currentPage - 1,
            size: this.pageSize
          }
        });

        if (response.data.code === 200) {
          const data = response.data.data;
          this.noticeList = data.content || [];
          this.total = data.totalElements || 0;
        } else {
          this.$message.error(response.data.message || '获取通知列表失败');
        }
      } catch (error) {
        console.error('获取通知列表失败:', error);
        this.$message.error(error.message || '获取通知列表失败');
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchNoticeList()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchNoticeList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchNoticeList()
    },
    handleView(notice) {
      this.viewingNotice = { ...notice }
      this.dialogVisible = true
    },
    getTypeTagType(type) {
      const types = {
        'TEST_SCHEDULE': 'primary',
        'SCORE_RELEASE': 'success',
        'SYSTEM_MAINTENANCE': 'warning',
        'OTHER': 'info'
      }
      return types[type] || 'info'
    },
    getTypeLabel(type) {
      const labels = {
        'TEST_SCHEDULE': '体测安排',
        'SCORE_RELEASE': '成绩公布',
        'SYSTEM_MAINTENANCE': '系统维护',
        'OTHER': '其他通知'
      }
      return labels[type] || '其他通知'
    },
    formatDateTime(datetime) {
      if (!datetime) return ''
      return new Date(datetime).toLocaleString()
    }
  }
}
</script>

<style lang="scss" scoped>
.notice-list {
  padding: 20px;

  .search-bar {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
  }

  .important-notice {
    color: #f56c6c;
    font-weight: bold;
  }

  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }

  .notice-detail {
    .notice-header {
      margin-bottom: 20px;
    }

    .notice-meta {
      color: #909399;
      margin-bottom: 20px;
      font-size: 14px;

      .notice-priority {
        margin-left: 20px;
        color: #f56c6c;
      }
    }

    .notice-content {
      line-height: 1.6;
      white-space: pre-wrap;
      color: #303133;
    }
  }
}
</style> 