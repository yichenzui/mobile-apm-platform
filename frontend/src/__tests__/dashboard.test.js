import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useDashboardStore } from '@/stores/dashboard'

// Mock API 模块
vi.mock('@/api/dashboard', () => ({
  getDashboardSummary: vi.fn(() => Promise.resolve({
    data: {
      onlineUsers: 100,
      crashRate: 0.015,
      startupTime: { p50: 1200, p90: 2500, p99: 4000 },
      lagRate: 0.03,
      networkSuccessRate: 98.0,
      regionDistribution: [],
      trendData: [],
      versionDistribution: [],
      fpsDistribution: []
    }
  })),
  getAppList: vi.fn(() => Promise.resolve({
    data: [{ appKey: 'test-app', name: '测试应用', platform: 'ANDROID' }]
  })),
  getAlertList: vi.fn(() => Promise.resolve({ data: [] })),
  getIssueList: vi.fn(() => Promise.resolve({ data: [] })),
  getPerformanceTrend: vi.fn(() => Promise.resolve({ data: [] })),
  getGeoDistribution: vi.fn(() => Promise.resolve({ data: [] })),
  getVersionComparison: vi.fn(() => Promise.resolve({ data: [] })),
  getFpsDistribution: vi.fn(() => Promise.resolve({ data: [] }))
}))

describe('DashboardStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('应该正确初始化空状态', () => {
    const store = useDashboardStore()
    expect(store.appList).toEqual([])
    expect(store.kpiData).toEqual([])
    expect(store.trendChartData).toEqual([])
    expect(store.geoChartData).toEqual([])
  })

  it('应该能加载应用列表', async () => {
    const store = useDashboardStore()
    await store.fetchAppList()
    expect(store.appList.length).toBeGreaterThan(0)
    expect(store.appList[0].appKey).toBe('test-app')
  })

  it('应该能加载并格式化KPI数据', async () => {
    const store = useDashboardStore()
    await store.fetchKpiData('test-app', '2026-06-10T00:00:00', '2026-06-17T00:00:00')

    expect(store.kpiData.length).toBe(5)
    expect(store.kpiData[0].title).toBe('实时在线用户')
    expect(store.kpiData[0].value).toBe(100)
    expect(store.kpiData[1].title).toBe('今日崩溃率')
    expect(store.kpiData[2].title).toBe('平均启动时间(p50)')
    expect(store.kpiData[3].title).toBe('网络成功率')
    // 回归：后端已返回百分比(0-100)，前端不得再 ×100（曾导致显示 9799.2%）
    expect(store.kpiData[3].value).toBe('98.0')
  })

  it('应该在没有appKey时不发起请求', async () => {
    const store = useDashboardStore()
    await store.fetchKpiData('', '', '')
    expect(store.kpiData).toEqual([])
  })

  it('应该优雅处理API失败', async () => {
    const store = useDashboardStore()
    // 告警数据失败不应抛异常
    await store.fetchAlertData('')
    expect(store.alertData).toEqual([])

    // 问题数据失败不应抛异常
    await store.fetchIssueData('')
    expect(store.issueData).toEqual([])
  })
})
