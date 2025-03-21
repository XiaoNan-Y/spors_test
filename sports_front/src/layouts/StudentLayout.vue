<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#fff"
        active-text-color="#409EFF"
        :router="true"
      >
        <el-menu-item index="/student/home">
          <i class="el-icon-s-home"></i>
          <span slot="title">首页</span>
        </el-menu-item>
        <el-menu-item index="/student/test-records">
          <i class="el-icon-data-line"></i>
          <span slot="title">体测成绩</span>
        </el-menu-item>
        <el-menu-item index="/student/sports-standard">
          <i class="el-icon-document"></i>
          <span slot="title">体测标准</span>
        </el-menu-item>
        <el-menu-item index="/student/score-appeal">
          <i class="el-icon-warning"></i>
          <span slot="title">成绩申诉</span>
        </el-menu-item>
        <el-menu-item index="/student/exemption">
          <i class="el-icon-document-checked"></i>
          <span slot="title">免测/重测申请</span>
        </el-menu-item>
        <el-menu-item index="/student/notices">
          <i class="el-icon-bell"></i>
          <span slot="title">通知公告</span>
        </el-menu-item>
        <el-menu-item index="/student/feedback">
          <i class="el-icon-chat-dot-round"></i>
          <span slot="title">意见反馈</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-left">
          <h2>体育测试管理系统 - 学生端</h2>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
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
  computed: {
    activeMenu() {
      return this.$route.path
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
      switch (command) {
        case 'profile':
          this.$router.push('/student/profile')
          break
        case 'logout':
          localStorage.clear()
          this.$router.push('/login')
          break
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;

  .el-aside {
    background-color: #304156;
    color: #fff;

    .el-menu {
      border-right: none;
    }
  }

  .el-header {
    background-color: #fff;
    color: #333;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-shadow: 0 1px 4px rgba(0,21,41,.08);

    .header-left {
      h2 {
        margin: 0;
        font-size: 18px;
      }
    }

    .header-right {
      .el-dropdown-link {
        cursor: pointer;
        color: #409EFF;
      }
    }
  }

  .el-main {
    background-color: #f0f2f5;
    padding: 20px;
  }
}

.el-menu-vertical {
  height: 100%;
  .el-menu-item {
    &:hover {
      background-color: transparent !important;
      color: #409EFF;
    }
    &.is-active {
      background-color: transparent !important;
      color: #409EFF !important;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 20px;
        background-color: #409EFF;
      }
    }
  }
}
</style> 