<template>
  <el-container class="app-wrapper">
    <el-aside width="200px">
      <!-- 侧边栏 -->
      <sidebar />
    </el-aside>
    
    <el-container>
      <el-header>
        <!-- 顶部导航栏 -->
        <navbar />
      </el-header>
      
      <el-main>
        <!-- 主要内容区域 -->
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template> 