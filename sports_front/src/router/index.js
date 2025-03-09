import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../views/Login.vue'
import AdminLayout from '../layouts/AdminLayout.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'users/student',
        name: 'StudentManagement',
        component: () => import('../views/admin/StudentManagement.vue')
      },
      {
        path: 'users/teacher',
        name: 'TeacherManagement',
        component: () => import('../views/admin/TeacherManagement.vue')
      },
      {
        path: 'sports-items',
        name: 'SportsItems',
        component: () => import('../views/admin/SportsItems.vue')
      },
      {
        path: 'notices',
        name: 'NoticeManagement',
        component: () => import('../views/admin/NoticeManagement.vue')
      },
      {
        path: 'data-review',
        name: 'DataReview',
        component: () => import('../views/admin/DataReview.vue')
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/admin/Profile.vue')
      }
    ]
  },
  {
    path: '/',
    redirect: '/login'
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 简化路由守卫逻辑
router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user'))
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  
  if (!requiresAuth) {
    // 不需要认证的路由，直接通过
    next()
    return
  }

  if (!user) {
    // 需要认证但未登录，重定向到登录页
    next('/login')
    return
  }

  // 检查角色权限
  const requiredRole = to.matched.find(record => record.meta.role)?.meta.role
  if (requiredRole && requiredRole !== user.userType) {
    next('/login')
    return
  }

  next()
})

export default router 