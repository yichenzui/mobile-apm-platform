<template>
  <div class="issue-center">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>问题中心</span>
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
              v-model="selectedType"
              placeholder="问题类型"
              style="width: 150px; margin-right: 10px;"
            >
              <el-option
                label="全部"
                value=""
              />
              <el-option
                label="崩溃"
                value="CRASH"
              />
              <el-option
                label="ANR"
                value="ANR"
              />
              <el-option
                label="性能"
                value="PERFORMANCE"
              />
              <el-option
                label="UI"
                value="UI"
              />
            </el-select>
            <el-select
              v-model="selectedSeverity"
              placeholder="严重程度"
              style="width: 150px; margin-right: 10px;"
              clearable
            >
              <el-option
                label="全部"
                value=""
              />
              <el-option
                label="严重"
                :value="4"
              />
              <el-option
                label="高"
                :value="3"
              />
              <el-option
                label="中"
                :value="2"
              />
              <el-option
                label="低"
                :value="1"
              />
            </el-select>
            <el-select
              v-model="selectedStatus"
              placeholder="状态"
              style="width: 150px; margin-right: 10px;"
              clearable
            >
              <el-option
                label="全部"
                value=""
              />
              <el-option
                label="待处理"
                :value="0"
              />
              <el-option
                label="处理中"
                :value="1"
              />
              <el-option
                label="已解决"
                :value="2"
              />
              <el-option
                label="已关闭"
                :value="3"
              />
            </el-select>
            <el-button
              type="primary"
              @click="loadIssueList"
            >
              查询
            </el-button>
          </div>
        </div>
      </template>
      <div class="card-content">
        <el-table
          v-loading="loading"
          :data="issueList"
          style="width: 100%"
          @row-click="handleRowClick"
        >
          <el-table-column
            prop="type"
            label="问题类型"
            width="100"
          >
            <template #default="scope">
              <el-tag :type="getTypeColor(scope.row.type)">
                {{ getTypeText(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="title"
            label="问题标题"
            show-overflow-tooltip
          />
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
            prop="occurrenceCount"
            label="发生次数"
            width="100"
          />
          <el-table-column
            prop="affectedUserCount"
            label="影响用户数"
            width="120"
          />
          <el-table-column
            prop="createTime"
            label="创建时间"
            width="160"
          >
            <template #default="scope">
              {{ formatTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="150"
          >
            <template #default="scope">
              <el-button
                v-if="scope.row.status === 0 || scope.row.status === 1"
                type="primary"
                size="small"
                @click.stop="handleProcess(scope.row)"
              >
                处理
              </el-button>
              <el-button
                v-if="scope.row.status === 0 || scope.row.status === 1"
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
import { getIssueList, getAppList } from '@/api/dashboard'
import { processIssue, resolveIssue } from '@/api/issue'
import dayjs from 'dayjs'
import { getSeverityType, getSeverityText } from '@/utils/severity'

// 路由
const router = useRouter()

// 响应式数据
const loading = ref(false)
const issueList = ref([])
const appList = ref([])
const selectedApp = ref('')
const selectedType = ref('')
const selectedSeverity = ref('')
const selectedStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 获取应用列表
const loadAppList = async () => {
  try {
    // request.js拦截器已解包: response = { code, data, message }
    // 后端返回格式: { code: 200, data: [...], message: "ok" }
    const response = await getAppList()
    const appsData = Array.isArray(response.data) ? response.data : (response.data?.data || [])
    appList.value = appsData
    if (appList.value.length > 0 && !selectedApp.value) {
      selectedApp.value = appList.value[0].appKey
      loadIssueList()
    }
  } catch (error) {
    ElMessage.error('加载应用列表失败')
    console.error('加载应用列表失败:', error)
  }
}

// 加载问题列表
const loadIssueList = async () => {
  if (!selectedApp.value) {
    ElMessage.warning('请先选择应用')
    return
  }

  loading.value = true
  try {
    const response = await getIssueList({
      appKey: selectedApp.value,
      type: selectedType.value,
      // 注意：0 是合法值（若后端如此），且不能用真值判断，否则"低(1)"等选项会被吞掉
      severity: (selectedSeverity.value === '' || selectedSeverity.value === null || selectedSeverity.value === undefined)
        ? null : Number(selectedSeverity.value),
      status: selectedStatus.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })

    // 校验响应有效性
    if (!response || response.data == null) {
      ElMessage.error('获取问题数据异常')
      return
    }
    
    // 兼容多种返回格式: 直接数组 / { data, total } / { records, total }
    let issuesData = response.data
    if (!Array.isArray(issuesData)) {
      issuesData = issuesData?.records || issuesData?.content || issuesData?.list || issuesData?.data || []
    }
    issueList.value = Array.isArray(issuesData) ? issuesData : []
    total.value = response.data?.total || response.total || issueList.value.length
  } catch (error) {
    ElMessage.error('加载问题列表失败')
    console.error('加载问题列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理行点击
const handleRowClick = (row) => {
  router.push({
    path: `/issue/detail/${row.id}`
  })
}

// 处理问题
const handleProcess = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入处理说明', '处理问题', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '处理说明不能为空'
    })
    await processIssue(row.id, value)
    ElMessage.success('问题已处理')
    loadIssueList()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '处理问题失败')
      console.error('处理问题失败:', error)
    }
  }
}

// 解决问题
const handleResolve = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入解决方案', '解决问题', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '解决方案不能为空'
    })
    await resolveIssue(row.id, value)
    ElMessage.success('问题已解决')
    loadIssueList()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '解决问题失败')
      console.error('解决问题失败:', error)
    }
  }
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadIssueList()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadIssueList()
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

// 获取状态类型（status 为 Integer: 0=待处理, 1=处理中, 2=已解决, 3=已关闭）
const getStatusType = (status) => {
  const types = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: ''
  }
  const st = Number(status)
  return types[st] || 'info'
}

// 获取状态文本（后端：0=待处理, 1=处理中, 2=已解决, 3=已忽略）
const getStatusText = (status) => {
  const texts = {
    0: '待处理',
    1: '处理中',
    2: '已解决',
    3: '已忽略'
  }
  const st = Number(status)
  return texts[st] !== undefined ? texts[st] : String(status)
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
.issue-center {
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
