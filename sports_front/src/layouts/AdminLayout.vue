<template>
  <el-container class="admin-layout">
    <el-aside width="200px">
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#fff"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/dashboard">
          <i class="el-icon-s-home"></i>
          <span>首页</span>
        </el-menu-item>
        
        <el-submenu index="1">
          <template slot="title">
            <i class="el-icon-user"></i>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/users/student">学生管理</el-menu-item>
          <el-menu-item index="/admin/users/teacher">教师管理</el-menu-item>
        </el-submenu>

        <el-menu-item index="/admin/sports-items">
          <i class="el-icon-trophy"></i>
          <span>体测项目管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/notices">
          <i class="el-icon-bell"></i>
          <span>通知管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/data-review">
          <i class="el-icon-s-data"></i>
          <span>数据录入与审核</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <div class="header-left">体育测试管理系统-管理员端</div>
        <div class="header-right">
          <span>{{ userInfo.realName }}</span>
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <i class="el-icon-setting"></i>
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
import { mapState } from 'vuex'

export default {
  name: 'AdminLayout',
  computed: {
    ...mapState({
      userInfo: state => state.user
    })
  },
  methods: {
    handleCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push('/admin/profile')
          break
        case 'logout':
          this.$store.dispatch('clearUser')
          this.$router.push('/login')
          break
      }
    }
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
  color: #fff;
}

.el-header {
  background-color: #fff;
  color: #333;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ddd;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
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