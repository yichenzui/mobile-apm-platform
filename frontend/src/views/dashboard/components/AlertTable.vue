<template>
  <div class="alert-table-container">
    <el-table
      :data="tableData"
      style="width: 100%"
      :show-header="true"
      :height="300"
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

// 处理行点击
const handleRowClick = (row) => {
  emit('rowClick', row)
}
</script>

<style lang="scss" scoped>
.alert-table-container {
  width: 100%;

  :deep(.el-table__row) {
    cursor: pointer;

    &:hover {
      background-color: #f5f7fa;
    }
  }
}
</style>
