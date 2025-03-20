export default {
  path: '/student',
  component: () => import('@/layouts/StudentLayout'),
  name: 'StudentManagement',
  meta: { requiresAuth: true, role: 'STUDENT' },
  children: [
    {
      path: 'test-records',
      name: 'StudentTestRecords',
      component: () => import('@/views/student/test-records/index'),
      meta: { title: '体测成绩', icon: 'list' }
    }
  ]
} 