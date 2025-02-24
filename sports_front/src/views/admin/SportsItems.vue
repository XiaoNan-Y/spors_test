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
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="项目类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择项目类型" style="width: 100%">
            <el-option label="田赛" value="田赛"></el-option>
            <el-option label="径赛" value="径赛"></el-option>
            <el-option label="体能" value="体能"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="计量单位" prop="unit">
          <el-select v-model="form.unit" placeholder="请选择计量单位" style="width: 100%">
            <el-option label="米" value="米"></el-option>
            <el-option label="秒" value="秒"></el-option>
            <el-option label="分钟" value="分钟"></el-option>
            <el-option label="次" value="次"></el-option>
            <el-option label="个" value="个"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            type="textarea"
            v-model="form.description"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入项目描述"
          ></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="isActive">
          <el-switch
            v-model="form.isActive"
            active-color="#13ce66"
            inactive-color="#ff4949"
            active-text="启用"
            inactive-text="禁用"
          >
          </el-switch>
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
      dialogTitle: '',
      form: {
        id: null,
        name: '',
        type: '',
        unit: '',
        description: '',
        isActive: true
      },
      rules: {
        name: [
          { required: true, message: '请输入项目名称', trigger: 'blur' },
          { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '请选择项目类型', trigger: 'change' }
        ],
        unit: [
          { required: true, message: '请选择计量单位', trigger: 'change' }
        ],
        description: [
          { max: 500, message: '描述不能超过500个字符', trigger: 'blur' }
        ]
      }
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
        const res = await this.$http.get('/admin/sports-items', {
          params: { keyword: this.searchKeyword }
        })
        if (res.data.code === 200) {
          this.itemList = res.data.data
        } else {
          this.$message.error(res.data.msg || '获取项目列表失败')
        }
      } catch (error) {
        console.error('获取项目列表失败:', error)
        this.$message.error('获取项目列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.fetchItemList()
    },
    handleAdd() {
      this.dialogTitle = '添加项目'
      this.form = {
        id: null,
        name: '',
        type: '',
        unit: '',
        description: '',
        isActive: true
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑项目'
      this.form = { ...row }
      this.dialogVisible = true
    },
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          try {
            const url = this.form.id ? '/admin/sports-items/update' : '/admin/sports-items/add'
            const res = await this.$http[this.form.id ? 'put' : 'post'](url, this.form)
            if (res.data.code === 200) {
              this.$message.success(this.form.id ? '更新成功' : '添加成功')
              this.dialogVisible = false
              this.fetchItemList()
            } else {
              this.$message.error(res.data.msg || '操作失败')
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
        await this.$confirm('确认删除该项目吗？', '提示', {
          type: 'warning'
        })
        const res = await this.$http.delete(`/admin/sports-items/${row.id}`)
        if (res.data.code === 200) {
          this.$message.success('删除成功')
          this.fetchItemList()
        } else {
          this.$message.error(res.data.msg || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    async handleStatusChange(row) {
      try {
        const res = await this.$http.put(`/admin/sports-items/${row.id}/status`, {
          isActive: row.isActive
        })
        if (res.data.code === 200) {
          this.$message.success(`${row.isActive ? '启用' : '禁用'}成功`)
        } else {
          row.isActive = !row.isActive // 恢复状态
          this.$message.error(res.data.msg || '操作失败')
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