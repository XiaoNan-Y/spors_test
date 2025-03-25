<template>
  <div class="home">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card">
      <div class="welcome-content">
        <div class="user-info">
          <div class="welcome-header">
            <h2>欢迎回来</h2>
            <div class="datetime">
              <div class="time">{{ currentTime }}</div>
              <div class="date">{{ currentDate }}</div>
            </div>
          </div>
          <div class="user-detail">
            <p class="name">{{ studentName }}</p>
            <p class="info">{{ className }} | {{ studentNumber }}</p>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 快速导航 -->
    <div class="section-title">快速导航</div>
    <el-row :gutter="20" class="module-row">
      <el-col :span="8" v-for="module in modules" :key="module.path">
        <el-card 
          class="module-card" 
          shadow="hover"
          @click.native="navigateTo(module.path)"
        >
          <div class="module-content">
            <i :class="module.icon"></i>
            <div class="module-info">
              <h3>{{ module.title }}</h3>
              <p>{{ module.description }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'StudentHome',
  data() {
    return {
      studentName: '',
      className: '',
      studentNumber: '',
      currentTime: '',
      currentDate: '',
      timer: null,
      modules: [
        {
          title: '体测成绩',
          description: '查看您的体育测试成绩记录',
          icon: 'el-icon-data-line',
          path: '/student/test-records'
        },
        {
          title: '体测标准',
          description: '了解体育测试的评分标准',
          icon: 'el-icon-document',
          path: '/student/sports-standard'
        },
        {
          title: '成绩申诉',
          description: '对体测成绩有异议可提出申诉',
          icon: 'el-icon-warning',
          path: '/student/score-appeal'
        },
        {
          title: '免测/重测申请',
          description: '提交免测或重测申请',
          icon: 'el-icon-document-checked',
          path: '/student/exemption'
        },
        {
          title: '通知公告',
          description: '查看最新的通知和公告',
          icon: 'el-icon-bell',
          path: '/student/notices'
        },
        {
          title: '意见反馈',
          description: '提交您的意见和建议',
          icon: 'el-icon-chat-dot-round',
          path: '/student/feedback'
        }
      ]
    }
  },
  created() {
    this.loadUserInfo()
    this.updateDateTime()
    this.timer = setInterval(this.updateDateTime, 1000)
  },
  activated() {
    this.loadUserInfo()
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    updateDateTime() {
      const now = new Date()
      
      this.currentTime = now.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      })
      
      this.currentDate = now.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      })
    },
    loadUserInfo() {
      const userInfo = JSON.parse(localStorage.getItem('studentInfo'));
      if (userInfo && userInfo.realName) {
        this.studentName = userInfo.realName;
        this.className = userInfo.className;
        this.studentNumber = userInfo.studentNumber;
      } else {
        this.fetchUserInfo();
      }
    },
    fetchUserInfo() {
      this.$axios.get('/api/student/info')
        .then(response => {
          if (response.data.code === 200 && response.data.data) {
            const userData = response.data.data;
            this.studentName = userData.realName;
            this.className = userData.className;
            this.studentNumber = userData.studentNumber;
            
            localStorage.setItem('studentInfo', JSON.stringify(userData));
          } else {
            console.error('获取用户信息失败:', response.data.message);
          }
        })
        .catch(error => {
          console.error('获取用户信息请求失败:', error);
        });
    },
    navigateTo(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style lang="scss" scoped>
.home {
  padding: 20px;

  .welcome-card {
    margin-bottom: 20px;
    
    .welcome-content {
      .user-info {
        .welcome-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 20px;

          h2 {
            margin: 0;
            color: #303133;
            font-size: 28px;
            font-weight: bold;
          }

          .datetime {
            text-align: right;
            
            .time {
              font-size: 24px;
              color: #409EFF;
              font-weight: bold;
              margin-bottom: 5px;
            }
            
            .date {
              font-size: 14px;
              color: #909399;
            }
          }
        }

        .user-detail {
          .name {
            font-size: 20px;
            color: #303133;
            margin: 0 0 10px;
            font-weight: 500;
          }
          
          .info {
            font-size: 14px;
            color: #909399;
            margin: 0;
          }
        }
      }
    }
  }

  .section-title {
    font-size: 18px;
    font-weight: bold;
    color: #303133;
    margin: 20px 0;
    padding-left: 10px;
    border-left: 4px solid #409EFF;
  }

  .module-row {
    margin-bottom: 20px;
  }

  .module-card {
    cursor: pointer;
    transition: all 0.3s;
    margin-bottom: 20px;

    &:hover {
      transform: translateY(-5px);
    }

    .module-content {
      display: flex;
      align-items: center;
      padding: 10px;

      i {
        font-size: 32px;
        color: #409EFF;
        margin-right: 16px;
      }

      .module-info {
        h3 {
          margin: 0 0 8px;
          color: #303133;
          font-size: 16px;
        }

        p {
          margin: 0;
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }
}
</style> 