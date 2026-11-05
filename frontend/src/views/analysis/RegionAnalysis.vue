<template>
  <div class="region-analysis-page">
    <div class="page-header">
      <h2><el-icon><Location /></el-icon> 地域分析</h2>
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
          start-placeholder="开始"
          end-placeholder="结束"
          :shortcuts="dateShortcuts"
          @change="loadData"
        />
        <el-button
          :icon="Refresh"
          circle
          @click="loadData"
        />
      </div>
    </div>

    <el-row :gutter="16">
      <!-- 中国地图 -->
      <el-col
        :xs="24"
        :md="16"
      >
        <el-card shadow="hover">
          <template #header>
            <span>中国用户分布热力图</span>
            <el-tooltip content="颜色深浅代表用户密度">
              <el-icon style="margin-left: 6px; color: #909399;">
                <QuestionFilled />
              </el-icon>
            </el-tooltip>
          </template>
          <div
            ref="mapChartRef"
            class="chart-box map-box"
            style="height: 520px"
          />
        </el-card>
      </el-col>

      <!-- 省份排行 -->
      <el-col
        :xs="24"
        :md="8"
      >
        <el-card
          shadow="hover"
          style="margin-bottom: 16px;"
        >
          <template #header>
            <span>省份排行 Top10</span>
          </template>
          <div
            ref="provinceChartRef"
            style="height: 260px"
          />
        </el-card>
        <el-card shadow="hover">
          <template #header>
            <span>崩溃率最高地区</span>
          </template>
          <div class="crash-rank-list">
            <div
              v-for="(item, idx) in crashRankData"
              :key="idx"
              class="crash-rank-item"
            >
              <span
                class="rank-num"
                :class="'rank-' + (idx + 1)"
              >{{ idx + 1 }}</span>
              <span class="rank-name">{{ item.province }}</span>
              <span class="rank-value">{{ item.crashRate?.toFixed(2) }}%</span>
              <el-progress
                :percentage="Math.min(100, (item.crashRate || 0) * 5)"
                :color="item.crashRate > 3 ? '#f56c6c' : '#e6a23c'"
                :show-text="false"
                style="flex:1"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 省份明细表 -->
    <el-card
      shadow="hover"
      style="margin-top: 16px;"
    >
      <template #header>
        <div style="display: flex; justify-content: space-between;">
          <span>省份详细数据</span>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索省份..."
            size="small"
            style="width: 200px"
            clearable
          />
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="filteredTableData"
        border
        stripe
        max-height="400"
      >
        <el-table-column
          prop="province"
          label="省份"
          width="120"
          sortable
          fixed
        />
        <el-table-column
          prop="userCount"
          label="用户数"
          width="100"
          sortable
        />
        <el-table-column
          prop="percentage"
          label="占比(%)"
          width="100"
          sortable
        >
          <template #default="{ row }">
            {{ row.percentage?.toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="crashRate"
          label="崩溃率(%)"
          width="110"
          sortable
        >
          <template #default="{ row }">
            {{ (row.crashRate || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="crashCount"
          label="崩溃次数"
          width="100"
          sortable
        />
        <el-table-column
          prop="anrCount"
          label="ANR次数"
          width="100"
          sortable
        />
        <el-table-column
          prop="avgStartupTime"
          label="平均启动(ms)"
          width="130"
          sortable
        />
        <el-table-column
          prop="networkSuccessRate"
          label="网络成功率(%)"
          width="130"
          sortable
        >
          <template #default="{ row }">
            {{ (row.networkSuccessRate || 0).toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column
          label="质量等级"
          width="100"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.crashRate < 1 ? 'success' : row.crashRate < 3 ? 'warning' : 'danger'"
              size="small"
            >
              {{ row.crashRate < 1 ? '优秀' : row.crashRate < 3 ? '一般' : '较差' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { Location, Refresh, QuestionFilled, HomeFilled } from '@element-plus/icons-vue'
import { getAppList, getGeoDistribution } from '@/api/dashboard'
import chinaJson from '@/assets/map/china-simple.json'

const appList = ref([])
const selectedApp = ref('test-app')
const dateRange = ref([new Date(Date.now() - 7 * 86400000), new Date()])
const loading = ref(false)
const searchKeyword = ref('')
const tableData = ref([])
const crashRankData = ref([])

const dateShortcuts = [
  { text: '最近7天', value: [new Date(Date.now() - 7 * 86400000), new Date()] },
  { text: '最近30天', value: [new Date(Date.now() - 30 * 86400000), new Date()] }
]

const filteredTableData = computed(() => {
  if (!searchKeyword.value) return tableData.value
  return tableData.value.filter(r => (r.province || '').includes(searchKeyword.value))
})

const mapChartRef = ref(null)
const provinceChartRef = ref(null)
let mapChart = null
let provinceChart = null

// 窗口尺寸变化时重排图表，否则缩放后画布会留白/被裁切
const handleResize = () => {
  mapChart?.resize()
  provinceChart?.resize()
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
  mapChart?.dispose(); provinceChart?.dispose()
})

const loadData = async () => {
  if (!selectedApp.value) return
  loading.value = true
  const [startTime, endTime] = dateRange.value || []
  const params = { appKey: selectedApp.value, startTime: dayjs(startTime).format('YYYY-MM-DDTHH:mm:ss'), endTime: dayjs(endTime).format('YYYY-MM-DDTHH:mm:ss'), granularity: 'day' }
  try {
    const res = await getGeoDistribution(params)
    const data = res.data?.regionDistribution || res.data?.data || res.data || []
    const arr = Array.isArray(data) ? data : []
    tableData.value = arr

    // 崩溃排行
    crashRankData.value = [...arr].sort((a, b) => (b.crashRate || 0) - (a.crashRate || 0)).slice(0, 10)

    // 渲染地图
    renderMapChart(arr)
    // 渲染省份排行
    renderProvinceChart(arr)
  } catch (e) {
    console.error('加载地域数据失败:', e)
  } finally {
    loading.value = false
  }
}

const renderMapChart = (data) => {
  if (!mapChartRef.value) return
  if (!mapChart) mapChart = echarts.init(mapChartRef.value)
  try {
    echarts.registerMap('china', chinaJson)
    const mapData = data.map(d => ({
      // 后端省份名已与地图区域名完全一致，直接使用
      name: d.province,
      value: d.userCount || 0,
      crashRate: d.crashRate
    }))
    mapChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: (p) => `${p.name}<br/>用户数: ${p.value}<br/>崩溃率: ${(p.data?.crashRate || 0).toFixed(2)}%`
      },
      visualMap: { min: 0, max: Math.max(...mapData.map(d => d.value), 1), left: 'left', top: 'bottom',
        text: ['高', '低'], calculable: true, inRange: { color: ['#e6f7ff', '#1890ff', '#003a8c'] } },
      series: [{
        type: 'map', map: 'china', roam: true, data: mapData,
        label: { show: true, fontSize: 10, color: '#666' },
        itemStyle: {
          areaColor: '#f7f9fc',
          borderColor: '#33507a',
          borderWidth: 1.2,
          shadowColor: 'rgba(51,80,122,0.35)',
          shadowBlur: 10,
          shadowOffsetY: 5
        },
        emphasis: { label: { show: true, fontSize: 14 }, itemStyle: { areaColor: '#ffd666', borderColor: '#1f4e8c', borderWidth: 1.5 } }
      }]
    }, true)
  } catch (e) {
    console.error('加载地图失败:', e)
    mapChart.setOption({
      title: { text: '地图加载失败', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } }
    }, true)
  }
}

const renderProvinceChart = (data) => {
  if (!provinceChartRef.value) return
  if (!provinceChart) provinceChart = echarts.init(provinceChartRef.value)
  const sorted = [...data].sort((a, b) => (b.userCount || 0) - (a.userCount || 0)).slice(0, 10)
  // 窄屏下压缩左侧类目轴宽度
  const narrow = window.innerWidth <= 768
  provinceChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: narrow ? 66 : 90, right: narrow ? 44 : 40, top: 10, bottom: 20 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: sorted.map(d => d.province).reverse(), axisLabel: { fontSize: 11 } },
    series: [{
      type: 'bar', data: sorted.map(d => d.userCount || 0).reverse(),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0,
        [{ offset: 0, color: '#409eff' }, { offset: 1, color: '#36cfc9' }]) },
      label: { show: true, position: 'right', fontSize: 11 }
    }]
  }, true)
}
</script>

<style lang="scss" scoped>
.region-analysis-page { padding: 20px; }
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
  h2 { display: flex; align-items: center; gap: 8px; margin: 0; }
  .header-controls { display: flex; gap: 10px; align-items: center; }
}
.crash-rank-list {
  .crash-rank-item {
    display: flex; align-items: center; gap: 8px; padding: 6px 0;
    .rank-num { width: 22px; height: 22px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold; color: #fff; background: #909399; flex-shrink: 0; }
    .rank-1 { background: #f56c6c; }
    .rank-2 { background: #e6a23c; }
    .rank-3 { background: #409eff; }
    /* 省份名不允许逐字换行（窄屏下曾出现"广/东"竖排） */
    .rank-name { flex: 1; min-width: 48px; font-size: 13px; white-space: nowrap; }
    .rank-value { min-width: 50px; font-size: 13px; color: #f56c6c; font-weight: 600; text-align: right; }
  }
}

/* ===== 移动端（手机竖屏） ===== */
@media (max-width: 768px) {
  .region-analysis-page { padding: 12px; }

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

  /* 地图在窄屏下降低高度，避免占据整屏 */
  .map-box { height: 360px !important; }
}
</style>
