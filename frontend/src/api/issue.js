import request from '@/api/request'

// 取当前登录用户名，作为操作人写入 assignee
function currentUsername() {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')?.username || 'system'
  } catch {
    return 'system'
  }
}

// 获取问题详情
export function getIssueDetail(id) {
  return request({
    url: `/v1/issue/${id}`,
    method: 'get'
  })
}

// 处理问题
export function processIssue(id, note) {
  return request({
    url: `/v1/issue/${id}/process`,
    method: 'post',
    data: { note, assignee: currentUsername() }
  })
}

// 解决问题
export function resolveIssue(id, note) {
  return request({
    url: `/v1/issue/${id}/resolve`,
    method: 'post',
    data: { note, assignee: currentUsername() }
  })
}
