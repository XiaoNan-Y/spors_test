<template>
  <div>
    <el-table :data="users" border>
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="realName" label="姓名"></el-table-column>
      <el-table-column prop="studentNumber" label="学号" v-if="userType === 'STUDENT'"></el-table-column>
      <el-table-column prop="email" label="邮箱"></el-table-column>
      <el-table-column prop="phone" label="电话"></el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      @current-change="handlePageChange"
      :current-page.sync="currentPage"
      :page-size="pageSize"
      layout="total, prev, pager, next"
      :total="total">
    </el-pagination>
  </div>
</template>

<script>
export default {
  data() {
    return {
      users: [],
      userType: '', // 'STUDENT' or 'TEACHER'
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  methods: {
    async fetchUsers() {
      try {
        const endpoint = this.userType === 'STUDENT' ? '/students' : '/teachers';
        const response = await this.$axios.get(`/api/admin/users${endpoint}`, {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize
          }
        });
        if (response.data.code === 200) {
          this.users = response.data.data.content;
          this.total = response.data.data.totalElements;
        } else {
          this.$message.error(response.data.msg || '获取用户列表失败');
        }
      } catch (error) {
        this.$message.error('获取用户列表失败：' + error.message);
      }
    },
    handlePageChange(page) {
      this.currentPage = page;
      this.fetchUsers();
    },
    handleEdit(row) {
      this.$emit('edit', row);
    },
    handleDelete(row) {
      this.$confirm('确认删除该用户?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.delete(`/api/admin/users/${row.id}`);
          if (response.data.code === 200) {
            this.$message.success('删除成功');
            this.fetchUsers();
          } else {
            this.$message.error(response.data.msg || '删除失败');
          }
        } catch (error) {
          this.$message.error('删除失败：' + error.message);
        }
      }).catch(() => {});
    }
  },
  created() {
    this.fetchUsers();
  },
  watch: {
    userType: {
      handler(newVal) {
        if (newVal) {
          this.currentPage = 1;
          this.fetchUsers();
        }
      },
      immediate: true
    }
  }
}
</script> 