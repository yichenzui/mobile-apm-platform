<template>
  <div class="performance-analysis-page">
    <div class="page-header">
      <h2><el-icon><DataAnalysis /></el-icon> 性能分析</h2>
      <div class="header-controls">
        <el-button
          type="default"
          size="small"
          @click="$router.push('/dashboard')"
        >
          <el-icon><HomeFilled /></el-icon> 返回首页
        </el-button>
        <el-select
          v-model="selectedApp"
          style="width: 200px"
          placeholder="选择应用"
          @change="loadData"
        >
          <el-option
            v-for="app in appList"
            :key="app.appKey"
            :label="app.name"
            :value="app.appKey"
          />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :shortcuts="dateShortcuts"
          @change="loadData"
        />
        <el-radio-group
          v-model="dimension"
          size="small"
          @change="loadData"
        >
          <el-radio-button value="device">
            按设备
          </el-radio-button>
          <el-radio-button value="network">
            按网络
          </el-radio-button>
          <el-radio-button value="version">
            按版本
          </el-radio-button>
        </el-radio-group>
        <el-button
          :icon="Refresh"
          circle
          @click="loadData"
        />
      </div>
    </div>

    <!-- KPI 汇总行 -->
    <el-row
      :gutter="16"
      class="kpi-row"
    >
      <el-col
        v-for="kpi in kpiCards"
        :key="kpi.title"
        :xs="12"
        :sm="6"
      >
        <div :class="['stat-card', `stat-${kpi.color}`]">
          <div class="stat-icon">
            <el-icon :size="22">
              <component :is="kpi.icon" />
            </el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">{{ kpi.title }}</span>
            <span class="stat-value">{{ kpi.value }}</span>
            <span
              class="stat-trend"
              :style="{ color: kpi.trend > 0 ? '#f56c6c' : '#67c23a' }"
            >
              {{ kpi.trend > 0 ? '↑' : '↓' }} {{ Math.abs(kpi.trend) }}%
            </span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row
      :gutter="16"
      style="margin-top: 16px"
    >
      <!-- 趋势图 -->
      <el-col
        :xs="24"
        :md="16"
      >
        <el-card shadow="hover">
          <template #header>
            <span>性能趋势对比</span>
          </template>
          <div
            ref="trendChartRef"
            style="height: 360px"
          />
        </el-card>
      </el-col>
      <!-- 分布排名 -->
      <el-col
        :xs="24"
        :md="8"
      >
        <el-card shadow="hover">
          <template #header>
            <span>{{ dimensionLabel }}排名</span>
          </template>
          <div
            ref="rankChartRef"
            style="height: 360px"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-row
      :gutter="16"
      style="margin-top: 16px"
    >
      <!-- 详细对比表 -->
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>{{ dimensionLabel }}详细对比</span>
              <el-button
                size="small"
                @click="exportData"
              >
                导出 CSV
              </el-button>
            </div>
          </template>
          <el-table
            v-loading="tableLoading"
            :data="detailTableData"
            border
            stripe
            max-height="400"
          >
            <el-table-column
              :prop="dimension === 'device' ? 'deviceModel' : dimension === 'network' ? 'networkType' : 'version'"
              :label="dimensionLabel"
              width="160"
            />
            <el-table-column
              prop="activeUsers"
              label="活跃用户"
              width="100"
              sortable
            />
            <el-table-column
              prop="crashRate"
              label="崩溃率(%)"
              width="110"
              sortable
            >
              <template #default="{ row }">
                {{ row.crashRate?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="crashCount"
              label="崩溃次数"
              width="100"
              sortable
            />
            <el-table-column
              prop="avgStartupTime"
              label="平均启动(ms)"
              width="130"
              sortable
            >
              <template #default="{ row }">
                {{ row.avgStartupTime?.toFixed(0) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="networkSuccessRate"
              label="网络成功率(%)"
              width="130"
              sortable
            >
              <template #default="{ row }">
                {{ row.networkSuccessRate?.toFixed(1) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="anrCount"
              label="ANR次数"
              width="100"
              sortable
            />
            <el-table-column
              label="健康度"
              width="120"
              sortable
              prop="healthScore"
            >
              <template #default="{ row }">
                <el-progress
                  :percentage="row.healthScore || 0"
                  :color="(row.healthScore || 0) > 80 ? '#67c23a' : (row.healthScore || 0) > 60 ? '#e6a23c' : '#f56c6c'"
                />
              </template>
            </el-table-column>
            <el-table-column
              label="状态"
              width="80"
            >
              <template #default="{ row }">
                <el-tag
                  :type="(row.healthScore || 0) > 80 ? 'success' : (row.healthScore || 0) > 60 ? 'warning' : 'danger'"
                  size="small"
                >
                  {{ (row.healthScore || 0) > 80 ? '健康' : (row.healthScore || 0) > 60 ? '警告' : '危险' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { DataAnalysis, Refresh, HomeFilled } from '@element-plus/icons-vue'
import { getAppList, getPerformanceTrend, getVersionComparison, getDeviceDistribution, getNetworkDistribution } from '@/api/dashboard'

// 选项
const appList = ref([])
const selectedApp = ref('test-app')
const dateRange = ref([new Date(Date.now() - 7 * 86400000), new Date()])
const dimension = ref('version')
const tableLoading = ref(false)
const detailTableData = ref([])

const dateShortcuts = [
  { text: '今天', value: [new Date(), new Date()] },
  { text: '最近7天', value: [new Date(Date.now() - 7 * 86400000), new Date()] },
  { text: '最近30天', value: [new Date(Date.now() - 30 * 86400000), new Date()] }
]

const dimensionLabel = computed(() =>
  dimension.value === 'device' ? '设备型号' : dimension.value === 'network' ? '网络类型' : '应用版本'
)

const kpiCards = ref([
  { title: '平均崩溃率', value: '0.00%', trend: -2.1, icon: 'WarningFilled', color: 'red' },
  { title: '平均启动时间', value: '0ms', trend: -5.3, icon: 'Timer', color: 'blue' },
  { title: '网络成功率', value: '0%', trend: 1.2, icon: 'Connection', color: 'green' },
  { title: '健康设备占比', value: '0%', trend: 3.5, icon: 'Monitor', color: 'purple' }
])

const trendChartRef = ref(null)
const rankChartRef = ref(null)
let trendChart = null
let rankChart = null

// 窗口尺寸变化时重排图表
const handleResize = () => {
  trendChart?.resize()
  rankChart?.resize()
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  try {
    const res = await getAppList()
    appList.value = res.data || []
    if (appList.value.length > 0) selectedApp.value = appList.value[0].appKey
  } catch (e) { console.error(e) }
  await loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  rankChart?.dispose()
})

const loadData = async () => {
  if (!selectedApp.value) return
  tableLoading.value = true
  const [startTime, endTime] = dateRange.value || []
  const params = {
    appKey: selectedApp.value,
    startTime: dayjs(startTime).format('YYYY-MM-DDTHH:mm:ss'),
    endTime: dayjs(endTime).format('YYYY-MM-DDTHH:mm:ss'),
    granularity: 'day'
  }

  try {
    // 加载趋势数据
    const trendRes = await getPerformanceTrend(params)
    const trendData = trendRes.data?.trendData || trendRes.data?.data || trendRes.data || []
    renderTrendChart(Array.isArray(trendData) ? trendData : [])

    // 按所选维度加载对比数据（设备 / 网络 / 版本）
    let compRes
    if (dimension.value === 'device') {
      compRes = await getDeviceDistribution(params)
    } else if (dimension.value === 'network') {
      compRes = await getNetworkDistribution(params)
    } else {
      compRes = await getVersionComparison(params)
    }
    const compData = compRes.data?.deviceDistribution
      || compRes.data?.networkDistribution
      || compRes.data?.versionDistribution
      || compRes.data?.data
      || compRes.data
      || []

    // 处理详细表格数据
    const tableData = Array.isArray(compData) ? compData : []
    detailTableData.value = tableData.map(item => ({
      ...item,
      deviceModel: item.deviceModel || item.version || item.networkType || 'N/A',
      version: item.version || 'N/A',
      networkType: item.networkType || 'N/A',
      // 健康度取整，避免表格中出现 95.03052391759944 这类长小数
      healthScore: Math.round(item.healthScore || Math.max(0, Math.min(100, 100 - (item.crashRate || 0) * 20 - ((item.crashCount || 0) / Math.max(1, item.userCount || item.activeUsers || 1)) * 100)))
    }))

    // 排名图使用补齐后的数据（原始数据没有 healthScore 字段，会导致柱子全为 0）
    renderRankChart(detailTableData.value)

    // 更新KPI卡片
    if (tableData.length > 0) {
      const avgCrashRate = tableData.reduce((s, i) => s + (i.crashRate || 0), 0) / tableData.length
      const avgStartup = tableData.reduce((s, i) => s + (i.avgStartupTime || 0), 0) / tableData.length
      const avgNetRate = tableData.reduce((s, i) => s + (i.networkSuccessRate || 0), 0) / tableData.length
      const healthyCount = tableData.filter(i => (i.healthScore || 0) > 80).length
      kpiCards.value = [
        { ...kpiCards.value[0], value: avgCrashRate.toFixed(2) + '%' },
        { ...kpiCards.value[1], value: avgStartup.toFixed(0) + 'ms' },
        { ...kpiCards.value[2], value: avgNetRate.toFixed(1) + '%' },
        { ...kpiCards.value[3], value: ((healthyCount / tableData.length) * 100).toFixed(1) + '%' }
      ]
    }
  } catch (e) {
    console.error('加载性能分析数据失败:', e)
  } finally {
    tableLoading.value = false
  }
}

const renderTrendChart = (data) => {
  if (!trendChartRef.value) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)
  const xData = data.map(d => d.time || d.date || '')
  // 窄屏下把图例固定到顶部、下移绘图区，避免图例与 Y 轴名称重叠
  const narrow = window.innerWidth <= 768
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['崩溃率', '启动时间', '卡顿率'], top: narrow ? 0 : undefined },
    grid: { left: 50, right: 50, top: narrow ? 64 : 30, bottom: 30 },
    xAxis: { type: 'category', data: xData },
    yAxis: [
      { type: 'value', name: '率(%)' },
      { type: 'value', name: '时间(ms)' }
    ],
    series: [
      { name: '崩溃率', type: 'line', data: data.map(d => parseFloat((d.crashRate || 0).toFixed(2))), smooth: true,
        itemStyle: { color: '#f56c6c' }, areaStyle: { color: 'rgba(245,108,108,0.1)' } },
      { name: '启动时间', type: 'line', yAxisIndex: 1, data: data.map(d => d.avgStartupTime || d.startupTime || 0), smooth: true,
        itemStyle: { color: '#409eff' }, areaStyle: { color: 'rgba(64,158,255,0.1)' } },
      { name: '卡顿率', type: 'line', data: data.map(d => parseFloat((d.lagRate || 0).toFixed(2))), smooth: true,
        itemStyle: { color: '#e6a23c' }, areaStyle: { color: 'rgba(230,162,60,0.1)' } }
    ]
  }
  trendChart.setOption(option, true)
}

const renderRankChart = (data) => {
  if (!rankChartRef.value) return
  if (!rankChart) rankChart = echarts.init(rankChartRef.value)
  const sorted = [...data].sort((a, b) => (b.healthScore || 0) - (a.healthScore || 0)).slice(0, 10)
  const names = sorted.map(d => d.deviceModel || d.version || d.networkType || 'N/A')
  const scores = sorted.map(d => d.healthScore || 0)
  // 窄屏下压缩左侧类目轴宽度，避免柱状图被挤没
  const narrow = window.innerWidth <= 768
  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: narrow ? 74 : 120, right: narrow ? 34 : 20, top: 10, bottom: 20 },
    xAxis: { type: 'value', max: 100 },
    yAxis: { type: 'category', data: names, inverse: true, axisLabel: { width: 100, overflow: 'truncate' } },
    series: [{
      type: 'bar', data: scores,
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0,
        [{ offset: 0, color: '#f56c6c' }, { offset: 0.5, color: '#e6a23c' }, { offset: 1, color: '#67c23a' }])
      },
      label: { show: true, position: 'right', formatter: '{c}' }
    }]
  }
  rankChart.setOption(option, true)
}

const exportData = () => {
  if (detailTableData.value.length === 0) return
  const headers = ['名称', '活跃用户', '崩溃率(%)', '崩溃次数', '平均启动(ms)', '网络成功率(%)', 'ANR次数', '健康度']
  const rows = detailTableData.value.map(r => [
    r[dimension.value === 'device' ? 'deviceModel' : dimension.value === 'network' ? 'networkType' : 'version'] || 'N/A',
    r.activeUsers || 0, (r.crashRate || 0).toFixed(2), r.crashCount || 0,
    (r.avgStartupTime || 0).toFixed(0), (r.networkSuccessRate || 0).toFixed(1), r.anrCount || 0, (r.healthScore || 0)
  ])
  const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = `performance-analysis-${selectedApp.value}.csv`; a.click()
  URL.revokeObjectURL(url)
}
</script>

<style lang="scss" scoped>
.performance-analysis-page { padding: 20px; }
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
  h2 { display: flex; align-items: center; gap: 8px; margin: 0; color: #303133; }
  .header-controls { display: flex; gap: 10px; align-items: center; }
}
.kpi-row { margin-bottom: 0; }
.stat-card {
  display: flex; align-items: center; gap: 14px; padding: 18px 16px; border-radius: 8px; background: #fff;
  border: 1px solid #ebeef5; transition: all 0.3s;
  &:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
  .stat-icon { width: 48px; height: 48px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
  .stat-info { display: flex; flex-direction: column; }
  .stat-label { font-size: 13px; color: #909399; }
  .stat-value { font-size: 22px; font-weight: 700; color: #303133; margin: 2px 0; }
  .stat-trend { font-size: 12px; }
}
.stat-red .stat-icon { background: #fff2f0; color: #f56c6c; }
.stat-blue .stat-icon { background: #e6f7ff; color: #409eff; }
.stat-green .stat-icon { background: #f6ffed; color: #67c23a; }
.stat-purple .stat-icon { background: #f9f0ff; color: #722ed1; }

/* ===== 移动端（手机竖屏） ===== */
@media (max-width: 768px) {
  .performance-analysis-page { padding: 12px; }

  /* 标题与筛选控件改为上下两行，避免标题被挤成竖排文字 */
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;

    h2 { font-size: 16px; }

    .header-controls {
      width: 100%;
      flex-wrap: wrap;
      gap: 8px;

      :deep(.el-select) { width: 100% !important; }
    }
  }

  .stat-card {
    padding: 12px;
    gap: 10px;

    .stat-icon { width: 36px; height: 36px; }
    .stat-label { font-size: 12px; }
    .stat-value { font-size: 18px; }
  }
}
</style>
