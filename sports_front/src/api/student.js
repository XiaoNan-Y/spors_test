import request from '@/utils/request'

// 获取学生体测记录
export function getTestRecords(params) {
  return request({
    url: '/api/student/test-records',
    method: 'get',
    params
  })
}

// 获取体测记录详情
export function getTestRecordDetail(id) {
  return request({
    url: `/api/student/test-records/${id}`,
    method: 'get'
  })
} 