<template>
  <div class="sports-items">
    <div class="operation-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="输入项目名称搜索"
        style="width: 200px"
        @keyup.enter.native="handleSearch"
      >
        <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
      </el-input>
      
      <el-button type="primary" @click="handleAdd">
        <i class="el-icon-plus"></i> 添加项目
      </el-button>
    </div>

    <el-table
      :data="itemList"
      border
      stripe
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="name" label="项目名称" width="150"></el-table-column>
      <el-table-column prop="type" label="项目类型" width="120">
        <template slot-scope="scope">
          <el-tag :type="getTypeTagType(scope.row.type)">
            {{ scope.row.type }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="计量单位" width="100"></el-table-column>
      <el-table-column prop="description" label="项目描述" show-overflow-tooltip></el-table-column>
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
      <el-table-column label="操作" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="handleEdit(scope.row)"
          >编辑</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="form.description"></el-input>
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit"></el-input>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option label="田径" value="田径"></el-option>
            <el-option label="球类" value="球类"></el-option>
            <el-option label="力量" value="力量"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
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
  name: 'SportsItems',
  data() {
    return {
      searchKeyword: '',
      itemList: [],
      loading: false,
      dialogVisible: false,
      isEdit: false,
      form: {
        id: null,
        name: '',
        description: '',
        unit: '',
        type: '',
        isActive: true
      },
      rules: {
        name: [
          { required: true, message: '请输入项目名称', trigger: 'blur' }
        ],
        unit: [
          { required: true, message: '请输入计量单位', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '请选择项目类型', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑项目' : '添加项目'
    }
  },
  created() {
    this.fetchItemList()
  },
  methods: {
    getTypeTagType(type) {
      const typeMap = {
        '田赛': 'success',
        '径赛': 'primary',
        '体能': 'warning'
      }
      return typeMap[type] || 'info'
    },
    async fetchItemList() {
      try {
        this.loading = true
        const res = await this.$http.get('/api/admin/sports-items', {
          params: {
            keyword: this.searchKeyword
          }
        })
        if (res.data.code === 200) {
          this.itemList = res.data.data
        }
      } catch (error) {
        this.$message.error('获取项目列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.fetchItemList()
    },
    handleAdd() {
      this.isEdit = false
      this.form = {
        id: null,
        name: '',
        description: '',
        unit: '',
        type: '',
        isActive: true
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          try {
            const url = this.isEdit 
              ? `/api/admin/sports-items/${this.form.id}`
              : '/api/admin/sports-items'
            const method = this.isEdit ? 'put' : 'post'
            const res = await this.$http[method](url, this.form)
            if (res.data.code === 200) {
              this.$message.success(this.isEdit ? '更新成功' : '添加成功')
              this.dialogVisible = false
              this.fetchItemList()
            } else {
              this.$message.error(res.data.msg || '操作失败')
            }
          } catch (error) {
            console.error('操作失败:', error)
            this.$message.error(error.response?.data?.msg || '操作失败')
          }
        }
      })
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确认删除该项目?', '提示', {
          type: 'warning'
        })
        const res = await this.$http.delete(`/api/admin/sports-items/${row.id}`)
        if (res.data.code === 200) {
          this.$message.success('删除成功')
          this.fetchItemList()
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    async handleStatusChange(row) {
      try {
        const res = await this.$http.put(`/api/admin/sports-items/${row.id}/status`, {
          isActive: row.isActive
        })
        if (res.data.code === 200) {
          this.$message.success(`${row.isActive ? '启用' : '禁用'}成功`)
        } else {
          row.isActive = !row.isActive // 恢复状态
          this.$message.error('操作失败')
        }
      } catch (error) {
        row.isActive = !row.isActive // 恢复状态
        this.$message.error('操作失败')
      }
    }
  }
}
</script>

<style scoped>
.sports-items {
  padding: 20px;
}
.operation-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}
</style> 