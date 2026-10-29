import request from '@/api/request'

// 取当前登录用户名，作为操作人写入 assignee
function currentUsername() {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')?.username || 'system'
  } catch {
    return 'system'
  }
}

// 获取告警详情
export function getAlertDetail(id) {
  return request({
    url: `/v1/alert/${id}`,
    method: 'get'
  })
}

// 确认告警
export function acknowledgeAlert(id, note) {
  return request({
    url: `/v1/alert/${id}/acknowledge`,
    method: 'post',
    data: { note, assignee: currentUsername() }
  })
}

// 解决告警
export function resolveAlert(id, note) {
  return request({
    url: `/v1/alert/${id}/resolve`,
    method: 'post',
    data: { note, assignee: currentUsername() }
  })
}
