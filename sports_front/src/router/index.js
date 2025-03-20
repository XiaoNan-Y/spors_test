import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../views/Login.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import TestRecordList from '@/views/admin/TestRecordList.vue'
import UserList from '@/views/admin/UserList.vue'
import Dashboard from '@/views/admin/Dashboard.vue'
import StudentManagement from '@/views/admin/StudentManagement.vue'
import TeacherManagement from '@/views/admin/TeacherManagement.vue'
import SportsItemManagement from '@/views/admin/SportsItemManagement.vue'
import NoticeManagement from '@/views/admin/NoticeManagement.vue'
import Profile from '@/views/admin/Profile.vue'
import DataReview from '@/views/admin/DataReview.vue'
import studentRouter from './student'

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
        component: Dashboard
      },
      {
        path: 'users/student',
        name: 'StudentManagement',
        component: StudentManagement
      },
      {
        path: 'users/teacher',
        name: 'TeacherManagement',
        component: TeacherManagement
      },
      {
        path: 'sports-items',
        name: 'SportsItemManagement',
        component: SportsItemManagement,
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'notices',
        name: 'NoticeManagement',
        component: NoticeManagement
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index'),
        meta: { title: '个人信息', icon: 'el-icon-user' }
      },
      {
        path: 'data-review',
        name: 'DataReview',
        component: DataReview,
        meta: {
          title: '数据录入与审核',
          requiresAuth: true,
          role: 'ADMIN'
        }
      },
      {
        path: 'exemption-review',
        name: 'ExemptionReview',
        component: () => import('@/views/admin/ExemptionReview.vue'),
        meta: { title: '免测/重测申请', icon: 'el-icon-document-checked' }
      }
    ]
  },
  {
    path: '/admin/test-records',
    name: 'TestRecordList',
    component: TestRecordList,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/users',
    name: 'UserList',
    component: UserList,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/teacher',
    component: () => import('@/layouts/TeacherLayout'),
    meta: { requiresAuth: true, role: 'TEACHER' },
    children: [
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('@/views/teacher/Dashboard'),
        meta: { title: '首页' }
      },
      {
        path: 'class-stats',
        name: 'ClassStats',
        component: () => import('@/views/teacher/ClassStats'),
        meta: { title: '班级成绩统计' }
      },
      {
        path: 'student-records',
        name: 'StudentRecords',
        component: () => import('@/views/teacher/StudentRecords'),
        meta: { title: '学生成绩管理' }
      },
      {
        path: 'exemption-review',
        name: 'TeacherExemptionReview',
        component: () => import('@/views/teacher/ExemptionReview'),
        meta: { title: '免测/重测审核' }
      },
      {
        path: 'profile',
        name: 'TeacherProfile',
        component: () => import('@/views/profile/index'),
        meta: { title: '个人信息' }
      },
      {
        path: 'student-management',
        name: 'TeacherStudentManagement',
        component: () => import('@/views/teacher/StudentManagement.vue'),
        meta: { title: '学生管理', roles: ['TEACHER'] }
      },
      {
        path: 'notice-management',
        name: 'TeacherNoticeManagement',
        component: () => import('@/views/teacher/NoticeManagement.vue'),
        meta: { title: '通知管理', roles: ['TEACHER'] }
      },
      {
        path: '/teacher/student-records',
        name: 'TeacherStudentRecords',
        component: () => import('@/views/teacher/StudentRecords.vue'),
        meta: { title: '学生成绩管理', roles: ['TEACHER'] }
      }
    ]
  },
  studentRouter,
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
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router