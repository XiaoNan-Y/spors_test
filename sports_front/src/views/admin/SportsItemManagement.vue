<template>
  <div class="sports-item-management">
    <div class="operation-bar">
      <div class="search-area">
        <el-input
          v-model="searchKeyword"
          placeholder="输入项目名称搜索"
          style="width: 200px"
          @keyup.enter.native="handleSearch"
        >
          <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
        </el-input>
      </div>
      
      <el-button type="primary" @click="handleAdd">
        <i class="el-icon-plus"></i> 添加项目
      </el-button>
    </div>

    <el-table
      :data="tableData || []"
      border
      stripe
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="name" label="项目名称"></el-table-column>
      <el-table-column prop="type" label="项目类型" width="100"></el-table-column>
      <el-table-column prop="description" label="项目描述" show-overflow-tooltip></el-table-column>
      <el-table-column prop="unit" label="计量单位" width="100"></el-table-column>
      <el-table-column prop="isActive" label="状态" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.isActive"
            @change="handleStatusChange(scope.row)"
            active-color="#13ce66"
            inactive-color="#ff4949"
          >
          </el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button-group>
            <el-button
              size="mini"
              type="primary"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      >
      </el-pagination>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="项目类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择项目类型">
            <el-option label="田径" value="田径"></el-option>
            <el-option label="力量" value="力量"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input type="textarea" v-model="form.description" rows="3"></el-input>
        </el-form-item>
        <el-form-item label="计量单位" prop="unit">
          <el-input v-model="form.unit"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="isActive">
          <el-switch v-model="form.isActive"></el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'SportsItemManagement',
  data() {
    return {
      searchKeyword: '',
      tableData: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      dialogTitle: '',
      form: {
        name: '',
        type: '',
        description: '',
        unit: '',
        isActive: true
      },
      rules: {
        name: [
          { required: true, message: '请输入项目名称', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '请选择项目类型', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.fetchItemList()
  },
  methods: {
    async fetchItemList() {
      try {
        this.loading = true
        const response = await this.$http.get('/api/sports-items', {
          params: {
            keyword: this.searchKeyword,
            page: this.currentPage - 1,
            size: this.pageSize
          }
        })
        
        if (response.data.code === 200) {
          const data = response.data.data
          this.tableData = data.content
          this.total = data.totalElements
        } else {
          this.$message.error(response.data.msg || '获取体测项目列表失败')
        }
      } catch (error) {
        console.error('获取体测项目列表失败:', error)
        this.$message.error('获取体测项目列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchItemList()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchItemList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchItemList()
    },
    handleAdd() {
      this.dialogTitle = '添加体测项目'
      this.form = {
        name: '',
        type: '',
        description: '',
        unit: '',
        isActive: true
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑体测项目'
      this.form = { ...row }
      this.dialogVisible = true
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          try {
            const url = this.form.id ? 
              `/api/sports-items/${this.form.id}` : 
              '/api/sports-items'
            
            const method = this.form.id ? 'put' : 'post'
            const response = await this.$http[method](url, this.form)
            
            if (response.data.code === 200) {
              this.$message.success(this.form.id ? '更新成功' : '添加成功')
              this.dialogVisible = false
              this.fetchItemList()
            } else {
              this.$message.error(response.data.msg || '操作失败')
            }
          } catch (error) {
            console.error('操作失败:', error)
            this.$message.error('操作失败')
          }
        }
      })
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确认删除该体测项目吗？', '提示', {
          type: 'warning'
        })
        const response = await this.$http.delete(`/api/sports-items/${row.id}`)
        if (response.data.code === 200) {
          this.$message.success('删除成功')
          this.fetchItemList()
        } else {
          this.$message.error(response.data.msg || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }
    },
    async handleStatusChange(row) {
      try {
        const response = await this.$http.put(`/api/sports-items/${row.id}/status`, {
          isActive: row.isActive
        })
        if (response.data.code === 200) {
          this.$message.success(`${row.isActive ? '启用' : '禁用'}成功`)
        } else {
          row.isActive = !row.isActive // 恢复状态
          this.$message.error(response.data.msg || '操作失败')
        }
      } catch (error) {
        row.isActive = !row.isActive // 恢复状态
        console.error('操作失败:', error)
        this.$message.error('操作失败')
      }
    }
  }
}
</script>

<style scoped>
.sports-item-management {
  padding: 20px;
}
.operation-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-area {
  display: flex;
  align-items: center;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 