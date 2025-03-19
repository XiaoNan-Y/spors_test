<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <el-menu
        :default-active="$route.path"
        router
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#fff"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/student/dashboard">
          <i class="el-icon-s-home"></i>
          <span slot="title">首页</span>
        </el-menu-item>
        
        <el-menu-item index="/student/test-records">
          <i class="el-icon-s-data"></i>
          <span slot="title">测试记录</span>
        </el-menu-item>
        
        <el-menu-item index="/student/exemption">
          <i class="el-icon-s-order"></i>
          <span slot="title">免测/重测申请</span>
        </el-menu-item>
        
        <el-menu-item index="/student/notices">
          <i class="el-icon-bell"></i>
          <span slot="title">通知公告</span>
        </el-menu-item>
        
        <el-menu-item index="/student/profile">
          <i class="el-icon-user"></i>
          <span slot="title">个人信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header>
        <div class="header-left">体育测试管理系统-学生端</div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="el-dropdown-link">
              {{ userInfo.realName || '用户' }}<i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  name: 'StudentLayout',
  data() {
    return {
      userInfo: {}
    }
  },
  created() {
    this.fetchUserInfo()
  },
  methods: {
    async fetchUserInfo() {
      try {
        const response = await this.$http.get('/api/user/info')
        if (response.data.code === 200) {
          this.userInfo = response.data.data
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },
    handleCommand(command) {
      if (command === 'logout') {
        localStorage.removeItem('token')
        this.$router.push('/login')
      } else if (command === 'profile') {
        this.$router.push('/student/profile')
      }
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  border-bottom: 1px solid #e6e6e6;
}

.header-right {
  float: right;
  margin-right: 20px;
}

.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
}

.el-main {
  background-color: #f0f2f5;
  padding: 0;
}
</style> 