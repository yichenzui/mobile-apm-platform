import request from '@/api/request'

// 发送消息给 DeepSeek AI
export function sendMessage(data) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data,
    timeout: 60000  // AI 响应可能较慢，设60秒超时
  })
}

// 一键智能诊断
export function runDiagnose(data) {
  return request({
    url: '/ai/diagnose',
    method: 'post',
    data,
    timeout: 60000
  })
}
