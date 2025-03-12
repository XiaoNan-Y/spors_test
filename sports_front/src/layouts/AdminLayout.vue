<template>
  <el-container class="admin-layout">
    <el-aside width="200px">
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router>
        
        <el-menu-item index="/admin/dashboard">
          <i class="el-icon-s-home"></i>
          <span slot="title">首页</span>
        </el-menu-item>

        <el-submenu index="user">
          <template slot="title">
            <i class="el-icon-user"></i>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/users/student">学生管理</el-menu-item>
          <el-menu-item index="/admin/users/teacher">教师管理</el-menu-item>
        </el-submenu>

        <el-menu-item index="/admin/sports-items">
          <i class="el-icon-trophy"></i>
          <span slot="title">体测项目管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/notices">
          <i class="el-icon-bell"></i>
          <span slot="title">通知管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/data-review">
          <i class="el-icon-data-line"></i>
          <span slot="title">数据录入与审核</span>
        </el-menu-item>

        <!-- 添加个人信息菜单项 -->
        <el-menu-item index="/admin/profile">
          <i class="el-icon-user"></i>
          <span slot="title">个人信息</span>
        </el-menu-item>

      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header>
        <div class="header-left">体育测试管理系统-管理员端</div>
        <div class="header-right">
          <span>{{ username }}</span>
          <el-dropdown @command="handleCommand">
            <i class="el-icon-setting" style="margin-right: 15px"></i>
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
  name: 'AdminLayout',
  data() {
    return {
      username: '',
      activeMenu: ''
    }
  },
  created() {
    const user = JSON.parse(localStorage.getItem('user'))
    this.username = user ? user.username : ''
    this.activeMenu = this.$route.path
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        localStorage.removeItem('user')
        this.$router.push('/login')
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