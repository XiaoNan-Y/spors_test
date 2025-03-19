import StudentLayout from '@/layouts/StudentLayout.vue'

export default {
  path: '/student',
  component: StudentLayout,
  meta: { requiresAuth: true, role: 'STUDENT' },
  children: [
    {
      path: 'dashboard',
      name: 'StudentDashboard',
      component: () => import('@/views/student/Dashboard.vue'),
      meta: { title: '首页' }
    },
    {
      path: 'test-records',
      name: 'StudentTestRecords',
      component: () => import('@/views/student/TestRecords.vue'),
      meta: { title: '测试记录' }
    },
    {
      path: 'exemption',
      name: 'StudentExemption',
      component: () => import('@/views/student/Exemption.vue'),
      meta: { title: '免测/重测申请' }
    },
    {
      path: 'notices',
      name: 'StudentNotices',
      component: () => import('@/views/student/Notices.vue'),
      meta: { title: '通知公告' }
    },
    {
      path: 'profile',
      name: 'StudentProfile',
      component: () => import('@/views/student/Profile.vue'),
      meta: { title: '个人信息' }
    }
  ]
} 