<template>
  <div class="alert-center">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>告警中心</span>
          <div class="header-actions">
            <el-button
              type="default"
              size="small"
              style="margin-right: 10px;"
              @click="router.push('/dashboard')"
            >
              <el-icon><HomeFilled /></el-icon> 返回主页
            </el-button>
            <el-select
              v-model="selectedApp"
              placeholder="选择应用"
              style="width: 200px; margin-right: 10px;"
            >
              <el-option
                v-for="app in appList"
                :key="app.appKey"
                :label="app.name"
                :value="app.appKey"
              />
            </el-select>
            <el-select
              v-model="selectedSeverity"
              placeholder="严重程度"
              style="width: 150px; margin-right: 10px;"
            >
              <el-option
                label="全部"
                value=""
              />
              <el-option
                label="严重"
                value="CRITICAL"
              />
              <el-option
                label="高"
                value="HIGH"
              />
              <el-option
                label="中"
                value="MEDIUM"
              />
              <el-option
                label="低"
                value="LOW"
              />
            </el-select>
            <el-select
              v-model="selectedStatus"
              placeholder="状态"
              style="width: 150px; margin-right: 10px;"
            >
              <el-option
                label="全部"
                value=""
              />
              <el-option
                label="告警中"
                value="FIRING"
              />
              <el-option
                label="已恢复"
                value="RESOLVED"
              />
            </el-select>
            <el-button
              type="primary"
              @click="loadAlertList"
            >
              查询
            </el-button>
          </div>
        </div>
      </template>
      <div class="card-content">
        <el-table
          v-loading="loading"
          :data="alertList"
          style="width: 100%"
          @row-click="handleRowClick"
        >
          <el-table-column
            prop="severity"
            label="严重程度"
            width="100"
          >
            <template #default="scope">
              <el-tag :type="getSeverityType(scope.row.severity)">
                {{ getSeverityText(scope.row.severity) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="ruleName"
            label="告警规则"
            show-overflow-tooltip
          />
          <el-table-column
            prop="message"
            label="告警信息"
            show-overflow-tooltip
          />
          <el-table-column
            prop="startTime"
            label="开始时间"
            width="160"
          >
            <template #default="scope">
              {{ formatTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="endTime"
            label="结束时间"
            width="160"
          >
            <template #default="scope">
              {{ scope.row.endTime ? formatTime(scope.row.endTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            label="状态"
            width="100"
          >
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="150"
          >
            <template #default="scope">
              <el-button
                v-if="scope.row.status === 'FIRING'"
                type="primary"
                size="small"
                @click.stop="handleAcknowledge(scope.row)"
              >
                确认
              </el-button>
              <el-button
                v-if="scope.row.status === 'FIRING' || scope.row.status === 'ACKNOWLEDGED'"
                type="success"
                size="small"
                @click.stop="handleResolve(scope.row)"
              >
                解决
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { HomeFilled } from '@element-plus/icons-vue'
import { getAlertList, getAppList } from '@/api/dashboard'
import { acknowledgeAlert, resolveAlert } from '@/api/alert'
import dayjs from 'dayjs'
import { getSeverityType, getSeverityText } from '@/utils/severity'

// 路由
const router = useRouter()

// 响应式数据
const loading = ref(false)
const alertList = ref([])
const appList = ref([])
const selectedApp = ref('')
const selectedSeverity = ref('')
const selectedStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 获取应用列表
const loadAppList = async () => {
  try {
    // request.js拦截器已解包: response = { code, data, message }
    const response = await getAppList()
    const appsData = Array.isArray(response.data) ? response.data : (response.data?.data || [])
    appList.value = appsData
    if (appList.value.length > 0 && !selectedApp.value) {
      selectedApp.value = appList.value[0].appKey
      loadAlertList()
    }
  } catch (error) {
    ElMessage.error('加载应用列表失败')
    console.error('加载应用列表失败:', error)
  }
}

// 加载告警列表
const loadAlertList = async () => {
  if (!selectedApp.value) {
    ElMessage.warning('请先选择应用')
    return
  }

  loading.value = true
  try {
    const response = await getAlertList({
      appKey: selectedApp.value,
      severity: selectedSeverity.value,
      status: selectedStatus.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })

    // 校验响应有效性
    if (!response || response.data == null) {
      ElMessage.error('获取告警数据异常')
      return
    }

    // 兼容多种返回格式: 直接数组 / { data, total } / { records, total }
    let alertsData = response.data
    if (!Array.isArray(alertsData)) {
      alertsData = alertsData?.records || alertsData?.content || alertsData?.list || alertsData?.data || []
    }
    alertList.value = Array.isArray(alertsData) ? alertsData : []
    total.value = response.data?.total || response.total || alertList.value.length
  } catch (error) {
    ElMessage.error('加载告警列表失败')
    console.error('加载告警列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理行点击
const handleRowClick = (row) => {
  router.push({
    path: `/alert/detail/${row.id}`
  })
}

// 确认告警
const handleAcknowledge = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入处理说明', '确认告警', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '处理说明不能为空'
    })
    await acknowledgeAlert(row.id, value)
    ElMessage.success('告警已确认')
    loadAlertList()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '确认告警失败')
      console.error('确认告警失败:', error)
    }
  }
}

// 解决告警
const handleResolve = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入解决方案', '解决告警', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '解决方案不能为空'
    })
    await resolveAlert(row.id, value)
    ElMessage.success('告警已解决')
    loadAlertList()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '解决告警失败')
      console.error('解决告警失败:', error)
    }
  }
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadAlertList()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadAlertList()
}

// 严重程度映射已统一至 @/utils/severity（顶部引入），此处不再重复定义

// 获取状态类型（后端：FIRING-告警中, ACKNOWLEDGED-已确认, RESOLVED-已恢复, CLOSED-已关闭）
const getStatusType = (status) => {
  const types = {
    'FIRING': 'danger',
    'ACKNOWLEDGED': 'warning',
    'RESOLVED': 'success',
    'CLOSED': 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'FIRING': '告警中',
    'ACKNOWLEDGED': '已确认',
    'RESOLVED': '已恢复',
    'CLOSED': '已关闭'
  }
  return texts[status] || status
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 生命周期钩子
onMounted(() => {
  loadAppList()
})
</script>

<style lang="scss" scoped>
.alert-center {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-actions {
      display: flex;
      align-items: center;
    }
  }

  .card-content {
    min-height: 400px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style>
