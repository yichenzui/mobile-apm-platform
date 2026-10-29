<template>
  <div class="simulator-page">
    <div class="page-header">
      <div class="header-title">
        <h2><el-icon><Iphone /></el-icon> 移动端数据采集模拟器</h2>
        <p class="subtitle">
          模拟移动设备发起各类性能事件上报，用于测试和演示
        </p>
      </div>
      <el-button
        type="default"
        @click="$router.push('/dashboard')"
      >
        <el-icon><HomeFilled /></el-icon> 返回首页
      </el-button>
    </div>

    <div class="simulator-grid">
      <!-- 左侧：设备选择 + 参数配置 -->
      <el-card
        class="config-card"
        shadow="hover"
      >
        <template #header>
          <span><el-icon><Setting /></el-icon> 设备配置</span>
        </template>
        <el-form
          label-width="100px"
          size="default"
        >
          <el-form-item label="目标应用">
            <el-select
              v-model="targetApp"
              style="width: 100%"
              placeholder="选择应用"
            >
              <el-option
                v-for="app in appList"
                :key="app.appKey"
                :label="app.name"
                :value="app.appKey"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="设备ID">
            <el-input
              v-model="deviceId"
              placeholder="自动生成"
              :disabled="autoDeviceId"
            >
              <template #append>
                <el-button @click="autoDeviceId = !autoDeviceId">
                  {{ autoDeviceId ? '固定' : '自动' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="设备型号">
            <el-select
              v-model="deviceModel"
              style="width: 100%"
              placeholder="选择设备"
            >
              <el-option
                v-for="m in deviceModels"
                :key="m.value"
                :label="m.label"
                :value="m.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="系统版本">
            <el-select
              v-model="osVersion"
              style="width: 100%"
            >
              <el-option
                v-for="v in osVersions"
                :key="v"
                :label="v"
                :value="v"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="网络类型">
            <el-select
              v-model="networkType"
              style="width: 100%"
            >
              <el-option
                label="WiFi"
                value="WIFI"
              />
              <el-option
                label="4G"
                value="4G"
              />
              <el-option
                label="5G"
                value="5G"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="应用版本">
            <el-input
              v-model="appVersion"
              placeholder="如 2.3.1"
            />
          </el-form-item>
          <el-form-item label="地域">
            <el-select
              v-model="province"
              style="width: 100%"
              filterable
              placeholder="选择省份"
            >
              <el-option
                v-for="p in provinces"
                :key="p"
                :label="p"
                :value="p"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 右侧：事件触发面板 -->
      <el-card
        class="events-card"
        shadow="hover"
      >
        <template #header>
          <span><el-icon><Pointer /></el-icon> 性能事件模拟</span>
        </template>

        <div class="event-grid">
          <!-- APP_START -->
          <div
            class="event-item"
            @click="triggerEvent('APP_START')"
          >
            <div
              class="event-icon"
              style="background: #e6f7ff; color: #1890ff;"
            >
              <el-icon :size="28">
                <VideoPlay />
              </el-icon>
            </div>
            <div class="event-info">
              <strong>应用启动</strong>
              <span class="event-desc">模拟冷启动 / 热启动</span>
            </div>
            <el-tag
              size="small"
              type="info"
            >
              耗时 800-5000ms
            </el-tag>
          </div>

          <!-- APP_CRASH -->
          <div
            class="event-item"
            @click="triggerEvent('APP_CRASH')"
          >
            <div
              class="event-icon"
              style="background: #fff2f0; color: #ff4d4f;"
            >
              <el-icon :size="28">
                <WarningFilled />
              </el-icon>
            </div>
            <div class="event-info">
              <strong>应用崩溃</strong>
              <span class="event-desc">模拟NullPointer / OOM等异常</span>
            </div>
            <el-tag
              size="small"
              type="danger"
            >
              严重
            </el-tag>
          </div>

          <!-- ANR -->
          <div
            class="event-item"
            @click="triggerEvent('ANR')"
          >
            <div
              class="event-icon"
              style="background: #fff7e6; color: #fa8c16;"
            >
              <el-icon :size="28">
                <Clock />
              </el-icon>
            </div>
            <div class="event-info">
              <strong>ANR 无响应</strong>
              <span class="event-desc">模拟主线程阻塞5秒+</span>
            </div>
            <el-tag
              size="small"
              type="warning"
            >
              高
            </el-tag>
          </div>

          <!-- NETWORK -->
          <div
            class="event-item"
            @click="triggerEvent('NETWORK')"
          >
            <div
              class="event-icon"
              style="background: #f6ffed; color: #52c41a;"
            >
              <el-icon :size="28">
                <Connection />
              </el-icon>
            </div>
            <div class="event-info">
              <strong>网络请求</strong>
              <span class="event-desc">模拟API调用耗时/失败</span>
            </div>
            <el-tag
              size="small"
              type="success"
            >
              正常/异常
            </el-tag>
          </div>

          <!-- LOW_FPS -->
          <div
            class="event-item"
            @click="triggerEvent('LOW_FPS')"
          >
            <div
              class="event-icon"
              style="background: #f9f0ff; color: #722ed1;"
            >
              <el-icon :size="28">
                <TrendCharts />
              </el-icon>
            </div>
            <div class="event-info">
              <strong>丢帧 / 卡顿</strong>
              <span class="event-desc">模拟低FPS场景</span>
            </div>
            <el-tag
              size="small"
              type="warning"
            >
              中
            </el-tag>
          </div>

          <!-- MEMORY -->
          <div
            class="event-item"
            @click="triggerEvent('MEMORY')"
          >
            <div
              class="event-icon"
              style="background: #fff1f0; color: #cf1322;"
            >
              <el-icon :size="28">
                <Odometer />
              </el-icon>
            </div>
            <div class="event-info">
              <strong>内存告警</strong>
              <span class="event-desc">模拟内存占用过高</span>
            </div>
            <el-tag
              size="small"
              type="danger"
            >
              高
            </el-tag>
          </div>
        </div>

        <el-divider />

        <div class="batch-section">
          <div class="batch-controls">
            <span>批量模拟：</span>
            <el-input-number
              v-model="batchCount"
              :min="1"
              :max="50"
              size="small"
            />
            <span>条随机事件，间隔</span>
            <el-input-number
              v-model="batchInterval"
              :min="100"
              :max="5000"
              :step="100"
              size="small"
            />
            <span>ms</span>
            <el-button
              type="primary"
              :icon="VideoPlay"
              :loading="batchRunning"
              size="small"
              @click="startBatch"
            >
              {{ batchRunning ? '运行中...' : '批量执行' }}
            </el-button>
            <el-button
              v-if="batchRunning"
              type="danger"
              size="small"
              @click="stopBatch"
            >
              停止
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 上报日志 -->
    <el-card
      class="log-card"
      shadow="hover"
    >
      <template #header>
        <div class="log-header">
          <span><el-icon><Document /></el-icon> 上报日志</span>
          <div class="log-actions">
            <el-tag size="small">
              成功 {{ successCount }} / 失败 {{ failCount }}
            </el-tag>
            <el-button
              text
              size="small"
              @click="clearLogs"
            >
              清空
            </el-button>
          </div>
        </div>
      </template>
      <div
        ref="logContainer"
        class="log-list"
      >
        <div
          v-for="(log, idx) in logs"
          :key="idx"
          :class="['log-item', log.success ? 'log-success' : 'log-error']"
        >
          <span class="log-time">{{ log.time }}</span>
          <el-tag
            :type="log.success ? 'success' : 'danger'"
            size="small"
          >
            {{ log.eventType }}
          </el-tag>
          <span class="log-msg">{{ log.message }}</span>
        </div>
        <el-empty
          v-if="logs.length === 0"
          description="点击上方事件开始模拟..."
          :image-size="80"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Iphone, Setting, Pointer, VideoPlay, WarningFilled, Clock,
  Connection, TrendCharts, Odometer, Document, HomeFilled
} from '@element-plus/icons-vue'
import { getAppList } from '@/api/dashboard'
import request from '@/api/request'

// 应用列表
const appList = ref([])
const targetApp = ref('test-app')

// 设备配置
const autoDeviceId = ref(true)
const deviceId = ref('')
const deviceModel = ref('iPhone 15 Pro')
const osVersion = ref('iOS 17.0')
const networkType = ref('WIFI')
const appVersion = ref('2.3.1')
const province = ref('广东')

// 批量模拟
const batchCount = ref(10)
const batchInterval = ref(500)
const batchRunning = ref(false)
let batchTimer = null

// 日志
const logs = ref([])
const logContainer = ref(null)
const successCount = ref(0)
const failCount = ref(0)

// 设备型号列表
const deviceModels = [
  { label: 'iPhone 15 Pro Max', value: 'iPhone 15 Pro Max' },
  { label: 'iPhone 15 Pro', value: 'iPhone 15 Pro' },
  { label: 'iPhone 14 Pro', value: 'iPhone 14 Pro' },
  { label: 'iPhone 13', value: 'iPhone 13' },
  { label: 'Samsung Galaxy S24', value: 'Samsung Galaxy S24' },
  { label: 'Xiaomi 14 Pro', value: 'Xiaomi 14 Pro' },
  { label: 'Huawei Mate 60 Pro', value: 'Huawei Mate 60 Pro' },
  { label: 'OPPO Find X7', value: 'OPPO Find X7' }
]

const osVersions = ['iOS 17.4', 'iOS 17.0', 'iOS 16.6', 'Android 14', 'Android 13', 'Android 12', 'HarmonyOS 4.0']

const provinces = [
  '广东', '北京', '上海', '浙江', '江苏', '四川', '湖北', '湖南',
  '福建', '山东', '河南', '安徽', '辽宁', '重庆', '天津',
  '河北', '山西', '内蒙古', '吉林', '黑龙江', '江西',
  '广西', '海南', '贵州', '云南', '甘肃', '青海', '宁夏',
  '新疆', '西藏', '台湾', '香港', '澳门'
]

// 生成随机设备ID
const generateDeviceId = () => {
  const chars = 'ABCDEF0123456789'
  let id = ''
  for (let i = 0; i < 8; i++) id += chars.charAt(Math.floor(Math.random() * chars.length))
  return `DEV-${id}`
}

// 加载应用列表
onMounted(async () => {
  try {
    const res = await getAppList()
    appList.value = res.data || []
    if (appList.value.length > 0) targetApp.value = appList.value[0].appKey
  } catch (e) {
    console.error('加载应用列表失败:', e)
  }
  if (autoDeviceId.value) deviceId.value = generateDeviceId()
})

// 触发单个事件
const triggerEvent = async (eventType) => {
  if (autoDeviceId.value) deviceId.value = generateDeviceId()
  const startTime = Date.now()
  const eventData = generateEventData(eventType)
  const result = await sendReport(eventData)

  const log = {
    time: new Date().toLocaleTimeString(),
    eventType,
    message: result.success
      ? `上报成功 (${eventData.length}项, 耗时${Date.now() - startTime}ms)`
      : `上报失败: ${result.error}`,
    success: result.success
  }
  logs.value.unshift(log)
  if (logs.value.length > 100) logs.value.pop()
  if (result.success) successCount.value++
  else failCount.value++
  scrollLog()
}

// 生成事件数据（格式对齐后端 PerformanceDataReportDTO.PerformanceDataItem：
//   顶层只放 eventType/eventTime/deviceId/sessionId，
//   具体事件字段进 data JsonNode，上下文信息进 extras JsonNode）
const generateEventData = (eventType) => {
  const base = {
    eventType,
    eventTime: new Date().toISOString(),
    deviceId: deviceId.value,
    sessionId: `SESS-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
    data: {},
    extras: {
      version: appVersion.value,
      os: osVersion.value,
      network: networkType.value,
      deviceModel: deviceModel.value,
      province: province.value,
      city: '市级区域',
      appKey: targetApp.value
    }
  }

  switch (eventType) {
    case 'APP_START':
      // 字段名必须是 startupTime：后端统计启动耗时读的是 $.startupTime
      base.data = {
        startupTime: Math.floor(Math.random() * 4200) + 800,
        duration: Math.floor(Math.random() * 4200) + 800,
        startupType: Math.random() > 0.3 ? 'COLD' : 'WARM'
      }
      break
    case 'APP_CRASH':
      base.data = {
        crashType: ['NullPointerException', 'OutOfMemoryError', 'IndexOutOfBoundsException'][Math.floor(Math.random() * 3)],
        stackTrace: 'at com.example.app.MainActivity.onCreate(MainActivity.java:42)',
        memoryUsage: Math.floor(Math.random() * 400) + 100
      }
      break
    case 'ANR':
      base.data = {
        blockDuration: Math.floor(Math.random() * 8000) + 5000,
        blockThread: 'main'
      }
      break
    case 'NETWORK': {
      // 用块级作用域包裹：case 内直接声明变量会污染整个 switch 作用域
      const isFail = Math.random() < 0.2
      base.data = {
        duration: Math.floor(Math.random() * 5000) + 200,
        success: !isFail,
        statusCode: isFail ? 500 : 200,
        url: '/api/users/profile'
      }
      break
    }
    case 'LOW_FPS':
      base.data = {
        fps: Math.floor(Math.random() * 20) + 10,
        dropDuration: Math.floor(Math.random() * 3000) + 1000
      }
      break
    case 'MEMORY':
      base.data = {
        usedMemoryMB: Math.floor(Math.random() * 200) + 300,
        totalMemoryMB: 512,
        threshold: 80
      }
      break
    default:
      base.data = {}
  }

  return [base]
}

// 发送上报请求
const sendReport = async (data) => {
  try {
    await request({
      url: '/v1/performance/report',
      method: 'post',
      data: { appKey: targetApp.value, data }
    })
    return { success: true }
  } catch (e) {
    return { success: false, error: e.message || '网络错误' }
  }
}

// 批量模拟
const startBatch = async () => {
  batchRunning.value = true
  let count = 0

  // 串行执行：等待上一条上报完成再发下一条，避免瞬时并发打满后端
  const runBatch = async () => {
    if (count >= batchCount.value || !batchRunning.value) {
      batchRunning.value = false
      if (batchTimer) { clearTimeout(batchTimer); batchTimer = null }
      ElMessage.success(`批量模拟完成，共发送 ${count} 条事件`)
      return
    }
    // 权重: 启动40%/网络25%/卡顿15%/崩溃10%/ANR5%/内存5%
    const weights = ['APP_START', 'APP_START', 'APP_START', 'APP_START', 'NETWORK', 'NETWORK', 'NETWORK', 'LOW_FPS', 'LOW_FPS', 'APP_CRASH', 'ANR', 'MEMORY']
    const eventType = weights[Math.floor(Math.random() * weights.length)]
    try {
      await triggerEvent(eventType)
    } catch (e) {
      console.error('批量模拟单条事件异常:', e)
    }
    count++
    batchTimer = setTimeout(runBatch, batchInterval.value)
  }
  runBatch()
}

const stopBatch = () => {
  batchRunning.value = false
  if (batchTimer) { clearTimeout(batchTimer); batchTimer = null }
  ElMessage.info('已停止批量模拟')
}

// 离开页面时必须停掉定时器链，否则会继续向后端上报并造成内存泄漏
onBeforeUnmount(() => {
  batchRunning.value = false
  if (batchTimer) { clearTimeout(batchTimer); batchTimer = null }
})

const clearLogs = () => {
  logs.value = []
  successCount.value = 0
  failCount.value = 0
}

const scrollLog = () => {
  nextTick(() => {
    if (logContainer.value) logContainer.value.scrollTop = 0
  })
}
</script>

<style lang="scss" scoped>
.simulator-page {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  h2 {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
    color: #303133;
  }
  .subtitle { color: #909399; font-size: 14px; margin: 0; }
}
.simulator-grid {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.config-card, .events-card { height: fit-content; }

.event-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.event-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { border-color: #409eff; background: #ecf5ff; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(64,158,255,.12); }
  .event-icon {
    width: 48px; height: 48px; border-radius: 10px;
    display: flex; align-items: center; justify-content: center;
    flex-shrink: 0;
  }
  .event-info {
    flex: 1;
    strong { display: block; font-size: 14px; color: #303133; }
    .event-desc { font-size: 12px; color: #909399; }
  }
}

.batch-section {
  .batch-controls {
    display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
    font-size: 13px; color: #606266;
  }
}

.log-card {
  .log-header {
    display: flex; justify-content: space-between; align-items: center;
    .log-actions { display: flex; align-items: center; gap: 12px; }
  }
  .log-list {
    max-height: 400px; overflow-y: auto;
    .log-item {
      display: flex; align-items: center; gap: 10px;
      padding: 6px 0; border-bottom: 1px solid #f5f5f5;
      font-size: 13px;
      .log-time { color: #909399; font-family: monospace; min-width: 80px; }
      .log-msg { color: #606266; }
    }
    .log-error .log-msg { color: #f56c6c; }
  }
}
</style>
