import request from '@/utils/request'

// 获取测试记录列表
export function getTestRecords(params) {
  return request({
    url: '/api/admin/test-record',
    method: 'get',
    params
  })
}

// 添加测试记录
export function addTestRecord(data) {
  return request({
    url: '/api/admin/test-record',
    method: 'post',
    data
  })
}

// 审核测试记录
export function reviewTestRecord(data) {
  return request({
    url: '/api/admin/test-record/review',
    method: 'put',
    data
  })
}

// 获取历史记录
export function getHistoryRecords(params) {
  return request({
    url: '/api/admin/test-record/history',
    method: 'get',
    params
  })
}

// 导出数据
export function exportRecords(params) {
  return request({
    url: '/api/admin/test-record/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 下载模板
export function downloadTemplate() {
  return request({
    url: '/api/admin/test-record/template',
    method: 'get',
    responseType: 'blob'
  })
}

// 修改审核
export function modifyReview(data) {
  return request({
    url: '/api/admin/test-record/modify',
    method: 'put',
    data
  })
} 