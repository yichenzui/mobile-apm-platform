<template>
  <div class="performance-dashboard">
    <!-- 顶部工具栏 -->
    <div class="dashboard-toolbar">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-select
            v-model="selectedApp"
            placeholder="选择应用"
            @change="handleAppChange"
          >
            <el-option
              v-for="app in appList"
              :key="app.appKey"
              :label="app.name"
              :value="app.appKey"
            />
          </el-select>
        </el-col>
        <el-col :span="10">
          <el-radio-group
            v-model="timeRange"
            @change="handleTimeRangeChange"
          >
            <el-radio-button value="today">
              今天
            </el-radio-button>
            <el-radio-button value="7days">
              最近7天
            </el-radio-button>
            <el-radio-button value="30days">
              最近30天
            </el-radio-button>
            <el-radio-button value="custom">
              自定义
            </el-radio-button>
          </el-radio-group>
          <el-date-picker
            v-if="timeRange === 'custom'"
            v-model="customDateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="margin-left: 10px"
            @change="handleCustomDateChange"
          />
        </el-col>
        <el-col
          :span="6"
          class="text-right user-col"
        >
          <el-button
            type="primary"
            :icon="RefreshRight"
            :loading="loading"
            @click="refreshData"
          >
            刷新
          </el-button>
          <el-switch
            v-model="autoRefresh"
            active-text="自动刷新"
            style="margin-left: 10px"
            @change="handleAutoRefreshChange"
          />
          <el-dropdown
            class="user-dropdown"
            trigger="click"
            @command="handleUserCommand"
          >
            <span class="user-entry">
              <span class="mini-avatar">{{ avatarChar }}</span>
              <span class="user-name">{{ currentUsername }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item
                  command="logout"
                  divided
                >
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-col>
      </el-row>
    </div>

    <!-- 快捷入口导航 -->
    <div class="quick-nav-container">
      <div
        class="quick-nav-card"
        @click="router.push('/simulator')"
      >
        <div
          class="nav-icon"
          style="background:#e6f7ff;color:#1890ff;"
        >
          <el-icon :size="22">
            <Iphone />
          </el-icon>
        </div>
        <span class="nav-label">数据模拟器</span>
      </div>
      <div
        class="quick-nav-card"
        @click="router.push('/analysis/performance')"
      >
        <div
          class="nav-icon"
          style="background:#f6ffed;color:#52c41a;"
        >
          <el-icon :size="22">
            <DataAnalysis />
          </el-icon>
        </div>
        <span class="nav-label">性能分析</span>
      </div>
      <div
        class="quick-nav-card"
        @click="router.push('/analysis/region')"
      >
        <div
          class="nav-icon"
          style="background:#f9f0ff;color:#722ed1;"
        >
          <el-icon :size="22">
            <Location />
          </el-icon>
        </div>
        <span class="nav-label">地域分析</span>
      </div>
      <div
        class="quick-nav-card"
        @click="router.push('/analysis/version')"
      >
        <div
          class="nav-icon"
          style="background:#fff7e6;color:#fa8c16;"
        >
          <el-icon :size="22">
            <Histogram />
          </el-icon>
        </div>
        <span class="nav-label">版本对比</span>
      </div>
      <div
        class="quick-nav-card"
        @click="router.push('/alert')"
      >
        <div
          class="nav-icon"
          style="background:#fff2f0;color:#ff4d4f;"
        >
          <el-icon :size="22">
            <Bell />
          </el-icon>
        </div>
        <span class="nav-label">告警中心</span>
      </div>
      <div
        class="quick-nav-card"
        @click="router.push('/issue')"
      >
        <div
          class="nav-icon"
          style="background:#f0f5ff;color:#597ef7;"
        >
          <el-icon :size="22">
            <Warning />
          </el-icon>
        </div>
        <span class="nav-label">问题中心</span>
      </div>
      <div
        class="quick-nav-card"
        @click="router.push('/ai-assistant')"
      >
        <div
          class="nav-icon"
          style="background:linear-gradient(135deg,#e6f7ff,#f9f0ff);color:#409eff;"
        >
          <el-icon :size="22">
            <MagicStick />
          </el-icon>
        </div>
        <span class="nav-label">AI 助手</span>
      </div>
    </div>

    <!-- KPI指标卡片 -->
    <div class="kpi-cards-container">
      <div class="kpi-grid">
        <kpi-card
          v-for="(kpi, index) in kpiData"
          :key="index"
          :title="kpi.title"
          :value="kpi.value"
          :trend="kpi.trend"
          :unit="kpi.unit"
        />
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <el-row :gutter="20">
        <!-- 时间趋势图 -->
        <el-col :span="16">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>性能趋势</span>
                <el-radio-group
                  v-model="trendMetric"
                  size="small"
                >
                  <el-radio-button value="crashRate">
                    崩溃率
                  </el-radio-button>
                  <el-radio-button value="startupTime">
                    启动时间
                  </el-radio-button>
                  <el-radio-button value="lagRate">
                    卡顿率
                  </el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <trend-chart
              :data="trendChartData"
              :metric="trendMetric"
              @chart-click="handleTrendChartClick"
            />
          </el-card>
        </el-col>

        <!-- 地理分布图 -->
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>用户分布</span>
              </div>
            </template>
            <geo-chart
              :data="geoChartData"
              @region-click="handleRegionClick"
            />
          </el-card>
        </el-col>
      </el-row>

      <el-row
        :gutter="20"
        style="margin-top: 20px"
      >
        <!-- 版本对比图 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>版本对比</span>
                <el-radio-group
                  v-model="versionMetric"
                  size="small"
                >
                  <el-radio-button value="crashRate">
                    崩溃率
                  </el-radio-button>
                  <el-radio-button value="startupTime">
                    启动时间
                  </el-radio-button>
                  <el-radio-button value="lagRate">
                    卡顿率
                  </el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <version-chart
              :data="versionChartData"
              :metric="versionMetric"
              @chart-click="handleVersionChartClick"
            />
          </el-card>
        </el-col>

        <!-- FPS分布图 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>FPS分布</span>
              </div>
            </template>
            <fps-chart :data="fpsChartData" />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 实时数据表格 -->
    <div class="data-table-container">
      <el-row :gutter="20">
        <!-- 最新告警 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>最新告警</span>
                <el-button
                  link
                  type="primary"
                  @click="goToAlertCenter"
                >
                  查看全部
                </el-button>
              </div>
            </template>
            <alert-table
              :data="alertData"
              @row-click="handleAlertClick"
            />
          </el-card>
        </el-col>

        <!-- 最新问题 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>最新问题</span>
                <el-button
                  link
                  type="primary"
                  @click="goToIssueCenter"
                >
                  查看全部
                </el-button>
              </div>
            </template>
            <issue-table
              :data="issueData"
              @row-click="handleIssueClick"
            />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- AI 悬浮球 -->
    <FloatBall />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { RefreshRight, Iphone, DataAnalysis, Location, Histogram, Bell, Warning, MagicStick, ArrowDown, UserFilled, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useDashboardStore } from '@/stores/dashboard'
import { useWebSocket } from '@/composables/useWebSocket'
import dayjs from 'dayjs'

// 子组件
import KpiCard from './components/KpiCard.vue'
import TrendChart from './components/TrendChart.vue'
import GeoChart from './components/GeoChart.vue'
import VersionChart from './components/VersionChart.vue'
import FpsChart from './components/FpsChart.vue'
import AlertTable from './components/AlertTable.vue'
import IssueTable from './components/IssueTable.vue'
import FloatBall from '@/components/FloatBall.vue'

// 路由
const router = useRouter()

// 当前登录用户信息
const currentUsername = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('userInfo'))?.username || '用户'
  } catch {
    return '用户'
  }
})
const avatarChar = computed(() => (currentUsername.value || 'U').charAt(0).toUpperCase())

// 用户下拉菜单：个人中心 / 退出登录
const handleUserCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      const { default: request } = await import('@/api/request')
      await request.post('/auth/logout').catch(() => {})
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  }
}

// Store
const dashboardStore = useDashboardStore()

// WebSocket - 实时推送
const { connect: wsConnect, disconnect: wsDisconnect } = useWebSocket()

// 响应式数据
const loading = ref(false)
const autoRefresh = ref(true)
const refreshTimer = ref(null)
const selectedApp = ref('')
const timeRange = ref('7days')
const customDateRange = ref([])
const trendMetric = ref('crashRate')
const versionMetric = ref('crashRate')

// 计算属性
const appList = computed(() => dashboardStore.appList)
const kpiData = computed(() => dashboardStore.kpiData)
const trendChartData = computed(() => dashboardStore.trendChartData)
const geoChartData = computed(() => dashboardStore.geoChartData)
const versionChartData = computed(() => dashboardStore.versionChartData)
const fpsChartData = computed(() => dashboardStore.fpsChartData)
const alertData = computed(() => dashboardStore.alertData)
const issueData = computed(() => dashboardStore.issueData)

// 计算时间范围
const getTimeRange = () => {
  let startTime, endTime

  switch (timeRange.value) {
    case 'today':
      startTime = dayjs().startOf('day').toDate()
      endTime = dayjs().endOf('day').toDate()
      break
    case '7days':
      startTime = dayjs().subtract(7, 'day').startOf('day').toDate()
      endTime = dayjs().endOf('day').toDate()
      break
    case '30days':
      startTime = dayjs().subtract(30, 'day').startOf('day').toDate()
      endTime = dayjs().endOf('day').toDate()
      break
    case 'custom':
      if (customDateRange.value && customDateRange.value.length === 2) {
        startTime = customDateRange.value[0]
        endTime = customDateRange.value[1]
      } else {
        // 默认最近7天
        startTime = dayjs().subtract(7, 'day').startOf('day').toDate()
        endTime = dayjs().endOf('day').toDate()
      }
      break
    default:
      startTime = dayjs().startOf('day').toDate()
      endTime = dayjs().endOf('day').toDate()
  }

  return { startTime, endTime }
}

// 获取粒度
const getGranularity = () => {
  // 注：当前数据库数据均为日粒度，统一使用 'day'
  return 'day'
}

// 加载应用列表
const loadAppList = async () => {
  try {
    await dashboardStore.fetchAppList()
    if (appList.value.length > 0 && !selectedApp.value) {
      selectedApp.value = appList.value[0].appKey
      // 应用列表是异步加载的，这里拿到 appKey 后才能启动自动刷新定时器
      if (autoRefresh.value) {
        startAutoRefresh()
      }
      await loadDashboardData()
    }
  } catch (error) {
    ElMessage.error('加载应用列表失败')
    console.error('加载应用列表失败:', error)
  }
}

// 请求序列号：丢弃过期响应，避免切换应用/时间范围时旧数据覆盖新数据
let reqSeq = 0

// 加载看板数据
const loadDashboardData = async () => {
  if (!selectedApp.value) {
    ElMessage.warning('请先选择应用')
    return
  }

  const seq = ++reqSeq
  loading.value = true
  try {
    const { startTime, endTime } = getTimeRange()
    const granularity = getGranularity()

    // 将Date对象转换为ISO格式字符串 (后端需要 LocalDateTime 格式)
    const startIso = dayjs(startTime).format('YYYY-MM-DDTHH:mm:ss')
    const endIso = dayjs(endTime).format('YYYY-MM-DDTHH:mm:ss')

    // 并行加载所有数据，但单个失败不影响其他
    const results = await Promise.allSettled([
      dashboardStore.fetchKpiData(selectedApp.value, startIso, endIso),
      dashboardStore.fetchTrendData(selectedApp.value, startIso, endIso, granularity),
      dashboardStore.fetchGeoData(selectedApp.value, startIso, endIso, granularity),
      dashboardStore.fetchVersionData(selectedApp.value, startIso, endIso, granularity),
      dashboardStore.fetchFpsData(selectedApp.value, startIso, endIso, granularity),
      dashboardStore.fetchAlertData(selectedApp.value),
      dashboardStore.fetchIssueData(selectedApp.value)
    ])

    // 检查是否有失败请求并打印警告
    const failures = results.filter(r => r.status === 'rejected')
    if (failures.length > 0) {
      console.warn(`看板数据有 ${failures.length} 项加载失败`, failures)
    }

    // 本批请求返回期间已有更新的请求发起 → 结果已过期，按最新条件重新加载一次
    if (seq !== reqSeq) {
      return loadDashboardData()
    }
  } catch (error) {
    ElMessage.error('加载看板数据失败')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  // 防止 appKey 为空时发起无效请求
  if (!selectedApp.value || selectedApp.value.trim() === '') {
    return
  }
  loadDashboardData()
}

// 处理应用变更
const handleAppChange = async (newAppKey) => {
  if (!newAppKey || newAppKey.trim() === '') {
    return
  }
  // 重置所有数据状态，避免旧数据残留
  await loadDashboardData()
  
  // 如果自动刷新已开启，重新启动定时器（使用新的appKey）
  if (autoRefresh.value) {
    startAutoRefresh()
  }
}

// 处理时间范围变更
const handleTimeRangeChange = () => {
  loadDashboardData()
}

// 处理自定义日期变更
const handleCustomDateChange = () => {
  loadDashboardData()
}

// 处理自动刷新变更
const handleAutoRefreshChange = (value) => {
  if (value) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

// 开始自动刷新
const startAutoRefresh = () => {
  stopAutoRefresh()
  // 只有在选择了应用时才启动自动刷新
  if (!selectedApp.value || selectedApp.value.trim() === '') {
    return
  }
  refreshTimer.value = setInterval(() => {
    refreshData()
  }, 30000) // 30秒刷新一次
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}

// 处理趋势图点击
const handleTrendChartClick = (params) => {
  // 跳转到详细分析页面
  router.push({
    path: '/analysis/performance',
    query: {
      appKey: selectedApp.value,
      metric: trendMetric.value,
      startTime: getTimeRange().startTime,
      endTime: getTimeRange().endTime
    }
  })
}

// 处理地区点击
const handleRegionClick = (params) => {
  // 跳转到地区分析页面
  router.push({
    path: '/analysis/region',
    query: {
      appKey: selectedApp.value,
      region: params.name,
      startTime: getTimeRange().startTime,
      endTime: getTimeRange().endTime
    }
  })
}

// 处理版本图点击
const handleVersionChartClick = (params) => {
  // 跳转到版本对比页面
  router.push({
    path: '/analysis/version',
    query: {
      appKey: selectedApp.value,
      version: params.name,
      metric: versionMetric.value,
      startTime: getTimeRange().startTime,
      endTime: getTimeRange().endTime
    }
  })
}

// 处理告警点击
const handleAlertClick = (row) => {
  // 跳转到告警详情页面
  router.push({
    path: `/alert/detail/${row.id}`
  })
}

// 处理问题点击
const handleIssueClick = (row) => {
  // 跳转到问题详情页面
  router.push({
    path: `/issue/detail/${row.id}`
  })
}

// 跳转到告警中心
const goToAlertCenter = () => {
  router.push({
    path: '/alert'
  })
}

// 跳转到问题中心
const goToIssueCenter = () => {
  router.push({
    path: '/issue'
  })
}

// 生命周期钩子
onMounted(() => {
  loadAppList()
  if (autoRefresh.value) {
    startAutoRefresh()
  }
  // 连接 WebSocket 实时推送
  wsConnect()
})

onUnmounted(() => {
  stopAutoRefresh()
  // 断开 WebSocket
  wsDisconnect()
})

// 监听时间范围变化
watch(timeRange, () => {
  if (timeRange.value !== 'custom') {
    customDateRange.value = []
  }
})
</script>

<style lang="scss" scoped>
.performance-dashboard {
  padding: 20px;

  .dashboard-toolbar {
    margin-bottom: 20px;

    .text-right {
      text-align: right;
    }

    .user-col {
      display: flex;
      align-items: center;
      justify-content: flex-end;
    }
    .user-dropdown {
      margin-left: 12px;
      .user-entry {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        cursor: pointer;
        padding: 4px 8px;
        border-radius: 16px;
        transition: background 0.2s;
        &:hover { background: #f5f7fa; }
        .mini-avatar {
          width: 28px;
          height: 28px;
          border-radius: 50%;
          background: linear-gradient(135deg, #409eff, #7c4dff);
          color: #fff;
          font-size: 13px;
          font-weight: bold;
          display: inline-flex;
          align-items: center;
          justify-content: center;
        }
        .user-name {
          font-size: 13px;
          color: #303133;
          max-width: 90px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }

  .quick-nav-container {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    flex-wrap: wrap;
  }
  .quick-nav-card {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 20px;
    border-radius: 10px;
    background: #fff;
    border: 1px solid #ebeef5;
    cursor: pointer;
    transition: all 0.25s;
    &:hover {
      border-color: #409eff;
      box-shadow: 0 4px 16px rgba(64,158,255,.15);
      transform: translateY(-2px);
    }
    .nav-icon {
      width: 40px; height: 40px;
      border-radius: 10px;
      display: flex; align-items: center; justify-content: center;
      flex-shrink: 0;
    }
    .nav-label {
      font-size: 14px; font-weight: 500; color: #303133; white-space: nowrap;
    }
  }

  .kpi-cards-container {
    margin-bottom: 20px;

    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 16px;
    }
  }

  .charts-container {
    margin-bottom: 20px;
  }

  .data-table-container {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
