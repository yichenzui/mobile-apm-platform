import request from '@/api/request'

// 获取看板汇总数据
export function getDashboardSummary(params) {
  return request({
    url: '/v1/dashboard/summary',
    method: 'get',
    params
  })
}

// 获取应用列表
export function getAppList() {
  return request({
    url: '/v1/application/list',
    method: 'get'
  })
}

// 获取告警列表
export function getAlertList(params) {
  return request({
    url: '/v1/alert/list',
    method: 'get',
    params
  })
}

// 获取问题列表
export function getIssueList(params) {
  return request({
    url: '/v1/issue/list',
    method: 'get',
    params
  })
}

// 获取性能趋势数据
export function getPerformanceTrend(params) {
  return request({
    url: '/v1/performance/trend',
    method: 'get',
    params
  })
}

// 获取地理分布数据
export function getGeoDistribution(params) {
  return request({
    url: '/v1/performance/geo',
    method: 'get',
    params
  })
}

// 获取版本对比数据
export function getVersionComparison(params) {
  return request({
    url: '/v1/performance/version',
    method: 'get',
    params
  })
}

// 获取FPS分布数据
export function getFpsDistribution(params) {
  return request({
    url: '/v1/performance/fps',
    method: 'get',
    params
  })
}

// 获取设备维度数据
export function getDeviceDistribution(params) {
  return request({
    url: '/v1/performance/device',
    method: 'get',
    params
  })
}

// 获取网络类型维度数据
export function getNetworkDistribution(params) {
  return request({
    url: '/v1/performance/network',
    method: 'get',
    params
  })
}
