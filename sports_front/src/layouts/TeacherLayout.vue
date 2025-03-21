<template>
  <el-container class="teacher-layout">
    <el-aside width="200px">
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        :router="true"
      >
        <el-menu-item index="/teacher/dashboard">
          <i class="el-icon-s-home"></i>
          <span slot="title">首页</span>
        </el-menu-item>

        <el-menu-item index="/teacher/class-stats">
          <i class="el-icon-s-data"></i>
          <span slot="title">班级成绩统计</span>
        </el-menu-item>

        <el-menu-item index="/teacher/student-records">
          <i class="el-icon-document"></i>
          <span slot="title">学生成绩管理</span>
        </el-menu-item>

        <el-menu-item index="/teacher/exemption-review">
          <i class="el-icon-document-checked"></i>
          <span slot="title">免测/重测审核</span>
        </el-menu-item>

        <el-menu-item index="/teacher/score-appeals">
          <i class="el-icon-warning"></i>
          <span slot="title">成绩申诉审核</span>
        </el-menu-item>

        <el-menu-item index="/teacher/student-management">
          <i class="el-icon-user-solid"></i>
          <span slot="title">学生管理</span>
        </el-menu-item>

        <el-menu-item index="/teacher/notice-management">
          <i class="el-icon-bell"></i>
          <span slot="title">通知管理</span>
        </el-menu-item>

        <el-menu-item index="/teacher/profile">
          <i class="el-icon-user"></i>
          <span slot="title">个人信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header>
        <div class="header-left">体育测试管理系统-教师端</div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              {{ username }}<i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
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
  name: 'TeacherLayout',
  data() {
    return {
      username: localStorage.getItem('username') || '教师',
      activeMenu: ''
    }
  },
  created() {
    // 检查用户信息
    const userId = localStorage.getItem('userId')
    const userRole = localStorage.getItem('userRole')
    const token = localStorage.getItem('token')
    
    console.log('TeacherLayout created - 用户信息:', {
      userId,
      userRole,
      token
    })
    
    if (!token || !userId || !userRole || userRole !== 'TEACHER') {
      console.log('用户信息无效，重定向到登录页')
      localStorage.clear() // 清除可能存在的无效信息
      this.$router.push('/login')
    }
    this.activeMenu = this.$route.path
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        localStorage.clear()
        
        this.$nextTick(async () => {
          try {
            await this.$router.push('/login')
            this.$message.success('已退出登录')
          } catch (error) {
            console.error('退出登录时发生错误:', error)
          }
        })
      }
    }
  }
}
</script>

<style scoped>
.teacher-layout {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ddd;
}

.header-left {
  margin-left: 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-right span {
  margin-right: 15px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>