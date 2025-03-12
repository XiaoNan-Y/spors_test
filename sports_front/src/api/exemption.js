import request from '@/utils/request'

// 获取申请列表
export function getExemptionList(params) {
  return request({
    url: '/api/exemptions',
    method: 'get',
    params
  })
}

// 教师审核
export function teacherReview(data) {
  return request({
    url: '/api/teacher/exemptions/review',
    method: 'put',
    data
  })
}

// 管理员审核
export function adminReview(data) {
  return request({
    url: '/api/admin/exemptions/review',
    method: 'put',
    data
  })
}

// 学生提交申请
export function submitExemption(data) {
  return request({
    url: '/api/student/exemptions',
    method: 'post',
    data
  })
} 