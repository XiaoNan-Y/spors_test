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
import TestRecords from '@/views/student/TestRecords'

Vue.use(VueRouter)

// 修改学生路由配置
const studentRouter = {
  path: '/student',
  component: () => import('@/layouts/StudentLayout'),  // 需要创建这个布局组件
  meta: { requiresAuth: true, role: 'STUDENT' },
  children: [
    {
      path: '',  // 默认子路由
      redirect: 'test-records'  // 重定向到体测记录页面
    },
    {
      path: 'test-records',
      name: 'StudentTestRecords',
      component: TestRecords,
      meta: { title: '体测成绩' }
    },
    {
      path: 'sports-standard',
      name: 'SportsStandard',
      component: () => import('@/views/student/SportsStandard'),
      meta: { title: '体测标准' }
    },
    {
      path: 'score-appeal',
      name: 'ScoreAppeal',
      component: () => import('@/views/student/ScoreAppeal'),
      meta: { title: '成绩申诉' }
    },
    {
      path: 'notices',
      name: 'StudentNotices',
      component: () => import('@/views/student/NoticeList'),
      meta: { title: '通知公告' }
    },
    {
      path: 'feedback',
      name: 'StudentFeedback',
      component: () => import('@/views/student/Feedback'),
      meta: { title: '意见反馈' }
    },
    {
      path: 'exemption',
      name: 'StudentExemption',
      component: () => import('@/views/student/Exemption'),
      meta: { title: '免测/重测申请' }
    },
    // 其他学生相关路由...
  ]
}

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
  studentRouter,  // 添加学生路由
  {
    path: '/',
    redirect: to => {
      const token = localStorage.getItem('token')
      const userRole = localStorage.getItem('userRole')
      if (!token) return '/login'
      
      switch (userRole) {
        case 'STUDENT':
          return '/student/test-records'
        case 'TEACHER':
          return '/teacher/dashboard'
        case 'ADMIN':
          return '/admin/dashboard'
        default:
          return '/login'
      }
    }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')
  const userId = localStorage.getItem('userId')
  
  console.log('路由守卫 - 当前状态:', {
    to: to.path,
    from: from.path,
    token,
    userRole,
    userId
  })

  // 如果是访问登录页
  if (to.path === '/login') {
    if (token && userRole) {
      console.log('已登录，重定向到对应首页')
      // 已登录则根据角色重定向到对应的首页
      switch (userRole) {
        case 'STUDENT':
          next('/student/test-records')
          break
        case 'TEACHER':
          next('/teacher/dashboard')
          break
        case 'ADMIN':
          next('/admin/dashboard')
          break
        default:
          next()
      }
    } else {
      console.log('未登录，允许访问登录页')
      next()
    }
  } else {
    // 非登录页面
    if (!token || !userRole) {
      console.log('未登录，重定向到登录页')
      // 未登录则重定向到登录页
      next('/login')
    } else {
      // 检查页面是否需要特定角色
      const requireRole = to.meta.role
      if (requireRole && requireRole !== userRole) {
        console.log('角色不匹配，重定向到对应首页')
        // 角色不匹配，重定向到对应角色的首页
        switch (userRole) {
          case 'STUDENT':
            next('/student/test-records')
            break
          case 'TEACHER':
            next('/teacher/dashboard')
            break
          case 'ADMIN':
            next('/admin/dashboard')
            break
          default:
            next('/login')
        }
      } else {
        console.log('验证通过，允许访问')
        next()
      }
    }
  }
})

export default router