<template>
  <div class="sports-standard">
    <!-- 顶部说明卡片 -->
    <el-card class="intro-card" shadow="hover">
      <div class="intro-content">
        <i class="el-icon-info intro-icon"></i>
        <div class="intro-text">
          <h3>体测标准说明</h3>
          <p>在这里您可以查看各项体育测试项目的详细标准和要求。请选择或点击下方具体项目进行查看。</p>
        </div>
      </div>
    </el-card>

    <!-- 项目选择区域 -->
    <div class="filter-container">
      <el-select 
        v-model="selectedItemId" 
        placeholder="选择体育项目" 
        clearable 
        @change="handleItemChange"
        class="item-selector"
        :loading="loading">
        <el-option
          v-for="item in sportsItems"
          :key="item.id"
          :label="item.name"
          :value="item.id">
        </el-option>
      </el-select>
    </div>

    <!-- 项目详情展示 -->
    <div v-if="selectedItem" class="item-details">
      <el-card class="detail-card" shadow="hover">
        <div slot="header" class="card-header">
          <span class="item-name">{{ selectedItem.name }}</span>
          <el-tag :type="getItemType(selectedItem.type)" size="medium">
            {{ selectedItem.type }}
          </el-tag>
        </div>
        
        <div class="detail-content">
          <!-- 基本信息部分 -->
          <div class="info-section">
            <div class="info-item">
              <h4><i class="el-icon-info"></i> 项目说明</h4>
              <p>{{ selectedItem.description }}</p>
            </div>
            
            <div class="info-item">
              <h4><i class="el-icon-guide"></i> 测试方法</h4>
              <p>{{ selectedItem.testMethod }}</p>
            </div>
            
            <div class="info-grid">
              <div class="grid-item">
                <h4><i class="el-icon-location"></i> 测试地点</h4>
                <p>{{ selectedItem.location }}</p>
              </div>
              
              <div class="grid-item">
                <h4><i class="el-icon-time"></i> 测试时间</h4>
                <p>{{ selectedItem.testTime }}</p>
              </div>
              
              <div class="grid-item">
                <h4><i class="el-icon-odometer"></i> 计量单位</h4>
                <p>{{ selectedItem.unit }}</p>
              </div>
            </div>

            <!-- 评分标准文本 -->
            <div class="standard-text">
              {{ selectedItem.standard }}
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 未选择项目时显示项目卡片网格 -->
    <div v-else class="items-grid">
      <el-card
        v-for="item in sportsItems"
        :key="item.id"
        class="item-card"
        shadow="hover"
        @click.native="handleItemSelect(item.id)"
      >
        <div class="item-card-content">
          <div class="item-icon">
            <i :class="getItemIcon(item.type)"></i>
          </div>
          <div class="item-info">
            <h3>{{ item.name }}</h3>
            <el-tag :type="getItemType(item.type)" size="small">
              {{ item.type }}
            </el-tag>
          </div>
          <div class="item-description">
            {{ item.description || '暂无描述' }}
          </div>
          <div class="item-footer">
            <span class="location">
              <i class="el-icon-location"></i>
              {{ item.location }}
            </span>
            <el-button type="text" size="small">
              查看详情 <i class="el-icon-arrow-right"></i>
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SportsStandard',
  data() {
    return {
      sportsItems: [],
      selectedItemId: null,
      selectedItem: null,
      loading: false
    }
  },
  created() {
    this.fetchSportsItems()
  },
  methods: {
    async fetchSportsItems() {
      try {
        this.loading = true
        const res = await this.$http.get('/api/sports-items/student/list')
        if (res.data.code === 200) {
          this.sportsItems = res.data.data
        }
      } catch (error) {
        this.$message.error('获取体测项目失败')
      } finally {
        this.loading = false
      }
    },
    handleItemChange(id) {
      if (!id) {
        this.selectedItem = null
        return
      }
      this.selectedItem = this.sportsItems.find(item => item.id === id)
    },
    getItemType(type) {
      const types = {
        '田径': 'primary',
        '力量': 'success',
        '耐力': 'warning',
        '球类': 'info'
      }
      return types[type] || 'info'
    },
    handleItemSelect(id) {
      this.selectedItemId = id;
      this.handleItemChange(id);
    },
    getItemIcon(type) {
      const icons = {
        '田径': 'el-icon-timer',
        '力量': 'el-icon-medal',
        '耐力': 'el-icon-data-line',
        '球类': 'el-icon-basketball',
        '体操': 'el-icon-position'
      };
      return icons[type] || 'el-icon-medal';
    }
  }
}
</script>

<style lang="scss" scoped>
.sports-standard {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;

  .intro-card {
    margin-bottom: 20px;
    background: linear-gradient(135deg, #e0f2f1 0%, #b2dfdb 100%);
    
    .intro-content {
      display: flex;
      align-items: center;
      
      .intro-icon {
        font-size: 24px;
        color: #00897b;
        margin-right: 15px;
      }
      
      .intro-text {
        h3 {
          margin: 0 0 10px;
          color: #00695c;
        }
        
        p {
          margin: 0;
          color: #004d40;
        }
      }
    }
  }

  .filter-container {
    margin-bottom: 20px;
    text-align: center;
    
    .item-selector {
      width: 300px;
    }
  }

  .item-details {
    .detail-card {
      .card-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        
        .item-name {
          font-size: 18px;
          font-weight: bold;
          color: #2c3e50;
        }
      }
      
      .detail-content {
        .info-section {
          margin-bottom: 30px;
          
          .info-item {
            margin-bottom: 20px;
            
            h4 {
              color: #2c3e50;
              margin-bottom: 10px;
              display: flex;
              align-items: center;
              
              i {
                margin-right: 8px;
                color: #1abc9c;
              }
            }
            
            p {
              color: #34495e;
              line-height: 1.6;
              margin: 0;
              padding-left: 24px;
            }
          }
          
          .info-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 20px;
            margin-top: 20px;
            
            .grid-item {
              background: #f8f9fa;
              padding: 15px;
              border-radius: 8px;
              
              h4 {
                color: #2c3e50;
                margin: 0 0 10px;
                font-size: 14px;
                display: flex;
                align-items: center;
                
                i {
                  margin-right: 8px;
                  color: #1abc9c;
                }
              }
              
              p {
                margin: 0;
                color: #34495e;
                font-weight: bold;
              }
            }
          }

          .standard-text {
            margin-top: 20px;
            padding: 15px;
            background: #f8f9fa;
            border-radius: 8px;
            color: #34495e;
            line-height: 1.8;
          }
        }
      }
    }
  }

  .items-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    margin-top: 20px;
    
    .item-card {
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
      
      .item-card-content {
        .item-icon {
          text-align: center;
          margin-bottom: 15px;
          
          i {
            font-size: 40px;
            color: #1abc9c;
          }
        }
        
        .item-info {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 10px;
          
          h3 {
            margin: 0;
            font-size: 18px;
            color: #2c3e50;
          }
        }
        
        .item-description {
          color: #7f8c8d;
          font-size: 14px;
          margin-bottom: 15px;
          height: 40px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }
        
        .item-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          color: #95a5a6;
          font-size: 14px;
          
          .location {
            display: flex;
            align-items: center;
            
            i {
              margin-right: 5px;
            }
          }
        }
      }
    }
  }
}

::v-deep .el-card__header {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  background: #f8f9fa;
}

::v-deep .el-tag {
  margin-left: 10px;
}
</style> 