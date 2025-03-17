<template>
  <el-container class="layout-container">
    <el-header>
      <div class="header-content">
        <div class="logo">体测系统 - 学生端</div>
        <el-dropdown @command="handleCommand" trigger="click">
          <span class="el-dropdown-link">
            {{ username }}<i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="profile">个人信息</el-dropdown-item>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-header>
    
    <el-container>
      <el-aside width="200px">
        <el-menu
          :default-active="$route.path"
          class="el-menu-vertical"
          router
          background-color="#304156"
          text-color="#fff"
          active-text-color="#409EFF">
          <el-menu-item index="/student/dashboard">
            <i class="el-icon-menu"></i>
            <span slot="title">首页</span>
          </el-menu-item>
          <el-menu-item index="/student/test-items">
            <i class="el-icon-document"></i>
            <span slot="title">体测项目</span>
          </el-menu-item>
          <el-menu-item index="/student/test-records">
            <i class="el-icon-data-line"></i>
            <span slot="title">体测成绩</span>
          </el-menu-item>
          <el-menu-item index="/student/notices">
            <i class="el-icon-bell"></i>
            <span slot="title">通知公告</span>
          </el-menu-item>
          <el-menu-item index="/student/feedback">
            <i class="el-icon-message"></i>
            <span slot="title">反馈建议</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
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
      username: ''
    }
  },
  created() {
    const user = JSON.parse(localStorage.getItem('user'))
    if (user) {
      this.username = user.username
    }
  },
  methods: {
    handleCommand(command) {
      if (command === 'profile') {
        this.$router.push('/student/profile')
      } else if (command === 'logout') {
        localStorage.removeItem('user')
        localStorage.removeItem('token')
        this.$router.push('/login')
      }
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 18px;
  font-weight: bold;
}

.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
}

.el-aside {
  background-color: #304156;
  color: #fff;
}

.el-menu {
  border-right: none;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>