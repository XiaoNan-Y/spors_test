<template>
  <el-container class="admin-layout">
    <el-aside width="200px">
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        :router="true"
      >
        <el-menu-item index="/admin/dashboard">
          <i class="el-icon-s-home"></i>
          <span slot="title">首页</span>
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
          <span slot="title">体测项目管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/notices">
          <i class="el-icon-bell"></i>
          <span slot="title">通知管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/data-review">
          <i class="el-icon-document-checked"></i>
          <span slot="title">数据录入与审核</span>
        </el-menu-item>

        <!-- 添加免测/重测申请菜单项 -->
        <el-menu-item index="/admin/exemption-review">
          <i class="el-icon-document"></i>
          <span slot="title">免测/重测申请</span>
        </el-menu-item>

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
  name: 'AdminLayout',
  data() {
    return {
      username: localStorage.getItem('username') || '管理员',
      activeMenu: ''
    }
  },
  created() {
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
      } else if (command === 'profile') {
        this.$router.push('/admin/profile')
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