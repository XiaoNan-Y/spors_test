<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#fff"
        active-text-color="#409EFF">
        <el-menu-item index="/student/test-records">
          <i class="el-icon-document"></i>
          <span>体测成绩</span>
        </el-menu-item>
        <el-menu-item index="/student/sports-standard">
          <i class="el-icon-document-checked"></i>
          <span>体测标准</span>
        </el-menu-item>
        <el-menu-item index="/student/score-appeal">
          <i class="el-icon-warning"></i>
          <span>成绩申诉</span>
        </el-menu-item>
        <el-menu-item index="/student/notices">
          <i class="el-icon-bell"></i>
          <span>通知公告</span>
        </el-menu-item>
        <!-- 其他菜单项 -->
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <span>体育测试管理系统 - 学生端</span>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
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
      username: localStorage.getItem('username') || '学生'
    }
  },
  created() {
    // 检查用户信息
    const userId = localStorage.getItem('userId')
    const userRole = localStorage.getItem('userRole')
    const token = localStorage.getItem('token')
    
    console.log('StudentLayout created - 用户信息:', {
      userId,
      userRole,
      token
    })
    
    if (!token || !userId || !userRole || userRole !== 'STUDENT') {
      console.log('用户信息无效，重定向到登录页')
      localStorage.clear() // 清除可能存在的无效信息
      this.$router.push('/login')
    }
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        localStorage.clear()
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
  color: #fff;
}
.el-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e6e6e6;
}
.header-right {
  margin-right: 20px;
}
.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
}
.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style> 