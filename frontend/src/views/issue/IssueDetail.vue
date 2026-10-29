<template>
  <div class="issue-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>问题详情</span>
          <el-button
            type="primary"
            @click="goBack"
          >
            返回
          </el-button>
        </div>
      </template>
      <div
        v-if="issueDetail"
        class="card-content"
      >
        <el-descriptions
          :column="2"
          border
        >
          <el-descriptions-item label="问题ID">
            {{ issueDetail.id }}
          </el-descriptions-item>
          <el-descriptions-item label="应用名称">
            {{ issueDetail.appName }}
          </el-descriptions-item>
          <el-descriptions-item label="问题标题">
            {{ issueDetail.title }}
          </el-descriptions-item>
          <el-descriptions-item label="问题类型">
            <el-tag :type="getTypeColor(issueDetail.type)">
              {{ getTypeText(issueDetail.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="严重程度">
            <el-tag :type="getSeverityType(issueDetail.severity)">
              {{ getSeverityText(issueDetail.severity) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(issueDetail.status)">
              {{ getStatusText(issueDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发生次数">
            {{ issueDetail.occurrenceCount }}
          </el-descriptions-item>
          <el-descriptions-item label="影响用户数">
            {{ issueDetail.affectedUserCount }}
          </el-descriptions-item>
          <el-descriptions-item label="首次发生时间">
            {{ formatTime(issueDetail.firstOccurrenceTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="最后发生时间">
            {{ formatTime(issueDetail.lastOccurrenceTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="处理人">
            {{ issueDetail.assignee || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="处理说明">
            {{ issueDetail.processNote || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(issueDetail.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatTime(issueDetail.updateTime) }}
          </el-descriptions-item>
        </el-descriptions>
        <div class="issue-description">
          <h4>问题描述</h4>
          <p>{{ issueDetail.description }}</p>
        </div>
        <div
          v-if="issueDetail.stackTrace"
          class="issue-stack-trace"
        >
          <h4>堆栈信息</h4>
          <el-input
            v-model="issueDetail.stackTrace"
            type="textarea"
            :rows="10"
            readonly
          />
        </div>
        <div
          v-if="issueDetail.deviceInfo"
          class="issue-device-info"
        >
          <h4>设备信息</h4>
          <el-input
            v-model="issueDetail.deviceInfo"
            type="textarea"
            :rows="5"
            readonly
          />
        </div>
        <div
          v-if="issueDetail.status === 0 || issueDetail.status === 1"
          class="issue-actions"
        >
          <el-button
            type="primary"
            @click="handleProcess"
          >
            处理
          </el-button>
          <el-button
            type="success"
            @click="handleResolve"
          >
            解决
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getIssueDetail, processIssue, resolveIssue } from '@/api/issue'
import dayjs from 'dayjs'
import { getSeverityType, getSeverityText } from '@/utils/severity'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const issueDetail = ref(null)

// 获取问题详情
const loadIssueDetail = async () => {
  const issueId = route.params.id
  if (!issueId) {
    ElMessage.error('问题ID不能为空')
    return
  }

  loading.value = true
  try {
    const response = await getIssueDetail(issueId)
    issueDetail.value = response.data
  } catch (error) {
    ElMessage.error('获取问题详情失败')
    console.error('获取问题详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理问题
const handleProcess = async () => {
  if (!issueDetail.value?.id) {
    ElMessage.error('问题数据不完整，请稍后重试')
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入处理说明', '处理问题', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '处理说明不能为空'
    })
    await processIssue(issueDetail.value.id, value)
    ElMessage.success('问题已处理')
    await loadIssueDetail()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error('处理问题失败')
    }
  }
}

// 解决问题
const handleResolve = async () => {
  if (!issueDetail.value?.id) {
    ElMessage.error('问题数据不完整，请稍后重试')
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入解决方案', '解决问题', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '解决方案不能为空'
    })
    await resolveIssue(issueDetail.value.id, value)
    ElMessage.success('问题已解决')
    await loadIssueDetail()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error('解决问题失败')
    }
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 获取问题类型颜色
const getTypeColor = (type) => {
  const colors = {
    'CRASH': 'danger',
    'ANR': 'warning',
    'PERFORMANCE': 'primary',
    'UI': 'info'
  }
  return colors[type] || 'info'
}

// 获取问题类型文本
const getTypeText = (type) => {
  const texts = {
    'CRASH': '崩溃',
    'ANR': 'ANR',
    'PERFORMANCE': '性能',
    'UI': 'UI'
  }
  return texts[type] || type
}

// 严重程度映射已统一至 @/utils/severity（顶部引入），此处不再重复定义
// 注：后端 issue.severity 为 1=低 2=中 3=高 4=严重，早期此处误按 0-3 解析已修正

// 获取状态类型（status 为 Integer: 0=待处理, 1=处理中, 2=已解决, 3=已关闭）
const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'success', 3: '' }
  const st = Number(status)
  return types[st] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = { 0: '待处理', 1: '处理中', 2: '已解决', 3: '已关闭' }
  const st = Number(status)
  return texts[st] !== undefined ? texts[st] : String(status)
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 生命周期钩子
onMounted(() => {
  loadIssueDetail()
})
</script>

<style lang="scss" scoped>
.issue-detail {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-content {
    min-height: 400px;
  }

  .issue-description, .issue-stack-trace, .issue-device-info {
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

  .issue-actions {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
