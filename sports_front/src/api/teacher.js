import request from '@/utils/request'

// 获取班级统计数据
export function getClassStats(params) {
  return request({
    url: '/api/teacher/class-stats',
    method: 'get',
    params
  })
}

// 获取体育项目列表
export function getSportsItems() {
  return request({
    url: '/api/teacher/sports-items',
    method: 'get'
  })
}

// 获取班级列表
export function getClassList() {
  return request({
    url: '/api/teacher/classes',
    method: 'get'
  })
}

// 导出成绩数据
export function exportRecords(params) {
  return request({
    url: '/api/teacher/export-records',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 获取成绩记录列表
export function getTestRecords(params) {
  return request({
    url: '/api/teacher/test-records',
    method: 'get',
    params
  })
}

// 添加成绩记录
export function addTestRecord(data) {
  return request({
    url: '/api/teacher/test-records',
    method: 'post',
    data
  })
}

// 更新成绩记录
export function updateTestRecord(id, data) {
  return request({
    url: `/api/teacher/test-records/${id}`,
    method: 'put',
    data
  })
}

// 删除成绩记录
export function deleteTestRecord(id) {
  return request({
    url: `/api/teacher/test-records/${id}`,
    method: 'delete'
  })
} 