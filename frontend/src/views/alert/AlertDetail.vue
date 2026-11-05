<template>
  <div class="alert-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>告警详情</span>
          <el-button
            type="primary"
            @click="goBack"
          >
            返回
          </el-button>
        </div>
      </template>
      <div
        v-if="alertDetail"
        class="card-content"
      >
        <el-descriptions
          :column="isMobile ? 1 : 2"
          border
        >
          <el-descriptions-item label="告警ID">
            {{ alertDetail.id }}
          </el-descriptions-item>
          <el-descriptions-item label="应用名称">
            {{ alertDetail.appName }}
          </el-descriptions-item>
          <el-descriptions-item label="告警规则">
            {{ alertDetail.ruleName }}
          </el-descriptions-item>
          <el-descriptions-item label="严重程度">
            <el-tag :type="getSeverityType(alertDetail.severity)">
              {{ getSeverityText(alertDetail.severity) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(alertDetail.status)">
              {{ getStatusText(alertDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前值">
            {{ alertDetail.currentValue }}
          </el-descriptions-item>
          <el-descriptions-item label="阈值">
            {{ alertDetail.thresholdValue }}
          </el-descriptions-item>
          <el-descriptions-item label="告警开始时间">
            {{ formatTime(alertDetail.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="告警结束时间">
            {{ alertDetail.endTime ? formatTime(alertDetail.endTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="持续时间">
            {{ alertDetail.duration ? alertDetail.duration + '分钟' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="处理人">
            {{ alertDetail.assignee || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="处理说明">
            {{ alertDetail.processNote || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="处理时间">
            {{ alertDetail.processTime ? formatTime(alertDetail.processTime) : '-' }}
          </el-descriptions-item>
        </el-descriptions>
        <div class="alert-message">
          <h4>告警消息</h4>
          <p>{{ alertDetail.message }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAlertDetail } from '@/api/alert'
import dayjs from 'dayjs'
import { getSeverityType, getSeverityText } from '@/utils/severity'
import { useIsMobile } from '@/composables/useIsMobile'

// 窄屏下描述列表改为单列，避免内容被挤压换行
const isMobile = useIsMobile()

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const alertDetail = ref(null)

// 获取告警详情
const loadAlertDetail = async () => {
  const alertId = route.params.id
  if (!alertId) {
    ElMessage.error('告警ID不能为空')
    return
  }

  loading.value = true
  try {
    const response = await getAlertDetail(alertId)
    alertDetail.value = response.data
  } catch (error) {
    ElMessage.error('获取告警详情失败')
    console.error('获取告警详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 严重程度映射已统一至 @/utils/severity（顶部引入），此处不再重复定义

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    'FIRING': 'danger',
    'RESOLVED': 'success'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'FIRING': '告警中',
    'RESOLVED': '已恢复'
  }
  return texts[status] || status
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 生命周期钩子
onMounted(() => {
  loadAlertDetail()
})
</script>

<style lang="scss" scoped>
.alert-detail {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-content {
    min-height: 400px;
  }

  .alert-message {
    margin-top: 20px;
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 4px;

    h4 {
      margin-top: 0;
      margin-bottom: 10px;
      color: #303133;
    }

    p {
      margin: 0;
      line-height: 1.5;
      color: #606266;
    }
  }
}
</style>
