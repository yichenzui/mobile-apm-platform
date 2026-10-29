import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getDashboardSummary, getAppList, getAlertList, getIssueList, getVersionComparison, getPerformanceTrend, getGeoDistribution, getFpsDistribution } from '@/api/dashboard'

export const useDashboardStore = defineStore('dashboard', () => {
  // 状态
  const appList = ref([])
  const kpiData = ref([])
  const trendChartData = ref([])
  const geoChartData = ref([])
  const versionChartData = ref([])
  const fpsChartData = ref([])
  const alertData = ref([])
  const issueData = ref([])

  // 获取应用列表
  // 注意：request.js拦截器已 return response.data（即后端HTTP body）
  // 后端格式: { code: 200, data: [...], message: "ok" }
  // 所以这里的 response = { code, data, message }，需要用 .data 取数据
  const fetchAppList = async () => {
    try {
      const response = await getAppList()
      appList.value = response.data || []
    } catch (error) {
      console.error('获取应用列表失败:', error)
      throw error
    }
  }

  // 获取KPI数据（核心数据，失败需抛出）
  const fetchKpiData = async (appKey, startTime, endTime) => {
    if (!appKey) {
      return
    }
    try {
      const response = await getDashboardSummary({
        appKey,
        startTime,
        endTime,
        granularity: 'day'
      })

      const data = response.data || {}

      kpiData.value = [
        {
          title: '实时在线用户',
          value: data.onlineUsers ?? 0,
          unit: '',
          trend: 5.2
        },
        {
          title: '今日崩溃率',
          value: (data.crashRate ?? 0).toFixed(2),
          unit: '%',
          trend: -2.1
        },
        {
          title: '平均启动时间(p50)',
          value: data.startupTime?.p50 ?? 0,
          unit: 'ms',
          trend: -8.5
        },
        {
          title: '网络成功率',
          // 后端 networkSuccessRate 已是百分比(0-100)，直接展示，勿再 ×100（否则会出现 9799.2%）
          value: (data.networkSuccessRate ?? 0).toFixed(1),
          unit: '%',
          trend: 1.3
        },
        {
          title: '卡顿率',
          value: (data.lagRate ?? 0).toFixed(2),
          unit: '%',
          trend: -3.5
        }
      ]
    } catch (error) {
      console.error('获取KPI数据失败:', error)
      throw error
    }
  }

  // 获取趋势数据
  const fetchTrendData = async (appKey, startTime, endTime, granularity) => {
    if (!appKey) {
      return
    }
    try {
      const response = await getPerformanceTrend({
        appKey,
        startTime,
        endTime,
        granularity
      })
      // 兼容多种返回格式：trendData / data / 直接数组
      const result = response.data
      trendChartData.value = Array.isArray(result)
        ? result
        : (result?.trendData || result?.data || [])
    } catch (error) {
      trendChartData.value = []
    }
  }

  // 获取地理数据
  const fetchGeoData = async (appKey, startTime, endTime, granularity) => {
    if (!appKey) {
      return
    }
    try {
      const response = await getGeoDistribution({
        appKey,
        startTime,
        endTime,
        granularity: granularity || 'day'
      })
      // 兼容多种返回格式
      const result = response.data
      geoChartData.value = Array.isArray(result)
        ? result
        : (result?.regionDistribution || result?.data || [])
    } catch (error) {
      geoChartData.value = []
    }
  }

  // 获取版本数据
  const fetchVersionData = async (appKey, startTime, endTime, granularity) => {
    if (!appKey) {
      return
    }
    try {
      const response = await getVersionComparison({
        appKey,
        startTime,
        endTime,
        granularity: granularity || 'day'
      })
      // 兼容多种返回格式
      const result = response.data
      versionChartData.value = Array.isArray(result)
        ? result
        : (result?.versionDistribution || result?.data || [])
    } catch (error) {
      versionChartData.value = []
    }
  }

  // 获取FPS数据
  const fetchFpsData = async (appKey, startTime, endTime, granularity) => {
    if (!appKey) {
      return
    }
    try {
      const response = await getFpsDistribution({
        appKey,
        startTime,
        endTime,
        granularity: granularity || 'day'
      })
      // 兼容多种返回格式
      const result = response.data
      fpsChartData.value = Array.isArray(result)
        ? result
        : (result?.fpsDistribution || result?.data || [])
    } catch (error) {
      fpsChartData.value = []
    }
  }

  // 获取告警数据（非核心数据，失败不抛出）
  const fetchAlertData = async (appKey) => {
    if (!appKey || appKey.trim() === '') {
      return
    }
    try {
      const response = await getAlertList({ appKey, pageSize: 10 })
      // 兼容多种返回格式：data数组 / records / content / 直接是数组
      let data = response.data
      if (!Array.isArray(data)) {
        data = data?.records || data?.content || data?.list || []
      }
      alertData.value = Array.isArray(data) ? data : []
    } catch (error) {
      alertData.value = []
    }
  }

  // 获取问题数据（非核心数据，失败不抛出）
  const fetchIssueData = async (appKey) => {
    if (!appKey || appKey.trim() === '') {
      return
    }
    try {
      const response = await getIssueList({ appKey, pageSize: 10 })
      // 兼容多种返回格式
      let data = response.data
      if (!Array.isArray(data)) {
        data = data?.records || data?.content || data?.list || []
      }
      issueData.value = Array.isArray(data) ? data : []
    } catch (error) {
      issueData.value = []
    }
  }

  return {
    // 状态
    appList,
    kpiData,
    trendChartData,
    geoChartData,
    versionChartData,
    fpsChartData,
    alertData,
    issueData,

    // 方法
    fetchAppList,
    fetchKpiData,
    fetchTrendData,
    fetchGeoData,
    fetchVersionData,
    fetchFpsData,
    fetchAlertData,
    fetchIssueData
  }
})
