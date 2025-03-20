<template>
  <div class="sports-standard">
    <!-- 顶部说明卡片 -->
    <el-card class="intro-card">
      <div class="intro-content">
        <i class="el-icon-info intro-icon"></i>
        <div class="intro-text">
          <h3>体测标准说明</h3>
          <p>在这里您可以查看各项体育测试项目的详细标准和要求。请从下方选择具体项目进行查看。</p>
        </div>
      </div>
    </el-card>

    <!-- 搜索和项目选择区域 -->
    <div class="filter-container">
      <el-select 
        v-model="selectedItem" 
        placeholder="选择体育项目" 
        clearable 
        @change="handleItemChange"
        :loading="loading">
        <el-option
          v-for="item in sportsItems"
          :key="item.id"
          :label="item.name"
          :value="item.id">
        </el-option>
      </el-select>
    </div>

    <!-- 项目标准展示区域 -->
    <el-row :gutter="20" v-if="!selectedItem">
      <el-col :span="8" v-for="item in sportsItems" :key="item.id">
        <el-card class="item-card" shadow="hover" @click.native="handleItemChange(item.id)">
          <div class="item-card-content">
            <i :class="getItemIcon(item.name)" class="item-icon"></i>
            <h4>{{ item.name }}</h4>
            <p>{{ item.description || '点击查看详细标准' }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细标准展示 -->
    <el-card v-if="selectedItemDetails" class="standard-card" v-loading="detailsLoading">
      <div slot="header" class="card-header">
        <div class="header-left">
          <i :class="getItemIcon(selectedItemDetails.name)" class="item-icon"></i>
          <span>{{ selectedItemDetails.name }}</span>
        </div>
        <el-button 
          type="text" 
          icon="el-icon-refresh" 
          @click="refreshDetails"
          :loading="detailsLoading">
          刷新
        </el-button>
      </div>
      <div class="item-details">
        <div class="description">
          <h4><i class="el-icon-document"></i> 项目说明：</h4>
          <p>{{ selectedItemDetails.description || '暂无说明' }}</p>
        </div>
        <div class="scoring-rules">
          <h4><i class="el-icon-trophy"></i> 评分标准：</h4>
          <el-table 
            :data="selectedItemDetails.standards || []" 
            border 
            style="width: 100%"
            :empty-text="standardsEmptyText">
            <el-table-column prop="grade" label="等级" width="100">
              <template slot-scope="scope">
                <el-tag :type="getGradeType(scope.row.grade)">{{ scope.row.grade }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="scoreRange" label="分数范围"></el-table-column>
            <el-table-column prop="requirement" label="要求说明"></el-table-column>
          </el-table>
        </div>
        <div class="notes" v-if="selectedItemDetails.notes">
          <h4><i class="el-icon-warning-outline"></i> 注意事项：</h4>
          <p>{{ selectedItemDetails.notes }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'SportsStandard',
  data() {
    return {
      selectedItem: null,
      selectedItemDetails: null,
      sportsItems: [],
      loading: false,
      detailsLoading: false,
      standardsEmptyText: '暂无评分标准数据'
    }
  },
  created() {
    this.getSportsItems()
  },
  methods: {
    async getSportsItems() {
      try {
        this.loading = true
        const response = await this.$axios.get('/api/sports-items/student/list')
        if (response.data.code === 200) {
          this.sportsItems = response.data.data
        }
      } catch (error) {
        console.error('获取体育项目列表失败:', error)
        this.$message.error('获取体育项目列表失败')
      } finally {
        this.loading = false
      }
    },
    async handleItemChange(itemId) {
      if (!itemId) {
        this.selectedItemDetails = null
        return
      }
      await this.getItemDetails(itemId)
    },
    async getItemDetails(itemId) {
      try {
        this.detailsLoading = true
        const response = await this.$axios.get(`/api/sports-items/student/${itemId}/standards`)
        if (response.data.code === 200) {
          this.selectedItemDetails = response.data.data
        }
      } catch (error) {
        console.error('获取体育项目标准详情失败:', error)
        this.$message.error('获取体育项目标准详情失败')
        this.selectedItemDetails = null
      } finally {
        this.detailsLoading = false
      }
    },
    async refreshDetails() {
      if (this.selectedItem) {
        await this.getItemDetails(this.selectedItem)
      }
    },
    getGradeType(grade) {
      const types = {
        'A': 'success',
        'B': 'warning',
        'C': 'info',
        'D': 'danger'
      }
      return types[grade] || 'info'
    },
    getItemIcon(itemName) {
      const icons = {
        '50米跑': 'el-icon-position',
        '立定跳远': 'el-icon-sort',
        '引体向上': 'el-icon-top',
        '1000米跑': 'el-icon-time',
        '仰卧起坐': 'el-icon-refresh'
      }
      return icons[itemName] || 'el-icon-data-analysis'
    }
  }
}
</script>

<style scoped>
.sports-standard {
  padding: 20px;
}

.intro-card {
  margin-bottom: 20px;
  background-color: #f0f9eb;
}

.intro-content {
  display: flex;
  align-items: center;
}

.intro-icon {
  font-size: 32px;
  color: #67c23a;
  margin-right: 20px;
}

.intro-text h3 {
  margin: 0 0 10px 0;
  color: #67c23a;
}

.intro-text p {
  margin: 0;
  color: #606266;
}

.filter-container {
  margin: 20px 0;
  display: flex;
  justify-content: center;
}

.el-select {
  width: 300px;
}

.item-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}

.item-card-content {
  text-align: center;
  padding: 20px;
}

.item-icon {
  font-size: 40px;
  color: #409EFF;
  margin-bottom: 15px;
}

.item-card h4 {
  margin: 10px 0;
  color: #303133;
}

.item-card p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.standard-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left .item-icon {
  font-size: 24px;
  margin-right: 10px;
}

.item-details {
  padding: 20px;
}

.item-details h4 {
  margin: 15px 0 10px;
  color: #303133;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.item-details h4 i {
  margin-right: 8px;
  color: #409EFF;
}

.description, .scoring-rules, .notes {
  margin-bottom: 30px;
}

.description p, .notes p {
  color: #606266;
  line-height: 1.6;
  margin: 0;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 4px solid #409EFF;
}

.notes p {
  border-left-color: #E6A23C;
  background-color: #fdf6ec;
}

.el-table {
  margin-top: 15px;
}
</style> 