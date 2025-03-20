<template>
  <el-container class="app-wrapper">
    <!-- 侧边栏 -->
    <el-aside width="200px">
      <el-menu
        :default-active="$route.path"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/student/test-records">
          <i class="el-icon-document"></i>
          <span slot="title">体测成绩</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主要内容区 -->
    <el-container>
      <el-header style="height: 50px;">
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
        <router-view />
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
.app-wrapper {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
  height: 100vh;
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}

.header-right {
  cursor: pointer;
  color: #333;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}

.el-dropdown-link {
  color: #333;
  cursor: pointer;
}
</style> 