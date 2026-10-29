<template>
  <div class="issue-table-container">
    <el-table
      :data="tableData"
      style="width: 100%"
      :show-header="true"
      :height="300"
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
        prop="createTime"
        label="创建时间"
        width="160"
      >
        <template #default="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import dayjs from 'dayjs'
import { getSeverityType, getSeverityText } from '@/utils/severity'

// Props
const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['rowClick'])

// 计算属性
const tableData = computed(() => {
  // 只显示前10条数据
  return props.data.slice(0, 10)
})

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

// 后端 status 为数字：0-待处理，1-处理中，2-已解决，3-已忽略
const getStatusType = (status) => {
  const types = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'info',
    'OPEN': 'info',
    'IN_PROGRESS': 'warning',
    'RESOLVED': 'success',
    'CLOSED': 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    0: '待处理',
    1: '处理中',
    2: '已解决',
    3: '已忽略',
    'OPEN': '待处理',
    'IN_PROGRESS': '处理中',
    'RESOLVED': '已解决',
    'CLOSED': '已关闭'
  }
  return texts[status] ?? status
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 处理行点击
const handleRowClick = (row) => {
  emit('rowClick', row)
}
</script>

<style lang="scss" scoped>
.issue-table-container {
  width: 100%;

  :deep(.el-table__row) {
    cursor: pointer;

    &:hover {
      background-color: #f5f7fa;
    }
  }
}
</style>
