<template>
  <el-card
    class="kpi-card"
    :body-style="{ padding: '20px' }"
  >
    <div class="kpi-content">
      <div class="kpi-title">
        {{ title }}
      </div>
      <div class="kpi-value">
        {{ formattedValue }}
        <span
          v-if="unit"
          class="kpi-unit"
        >{{ unit }}</span>
      </div>
      <div
        class="kpi-trend"
        :class="trendClass"
      >
        <el-icon v-if="trend > 0">
          <ArrowUp />
        </el-icon>
        <el-icon v-else-if="trend < 0">
          <ArrowDown />
        </el-icon>
        <el-icon v-else>
          <Minus />
        </el-icon>
        <span>{{ Math.abs(trend) }}%</span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowUp, ArrowDown, Minus } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [Number, String],
    required: true
  },
  unit: {
    type: String,
    default: ''
  },
  trend: {
    type: Number,
    default: 0
  }
})

// 计算属性
const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    // 如果是数字，格式化显示
    if (props.value >= 1000000) {
      return (props.value / 1000000).toFixed(1) + 'M'
    } else if (props.value >= 1000) {
      return (props.value / 1000).toFixed(1) + 'K'
    } else if (props.value < 1 && props.value > 0) {
      return (props.value * 100).toFixed(2)
    } else {
      return props.value.toLocaleString()
    }
  } else {
    return props.value
  }
})

const trendClass = computed(() => {
  if (props.trend > 0) return 'trend-up'
  if (props.trend < 0) return 'trend-down'
  return 'trend-flat'
})
</script>

<style lang="scss" scoped>
.kpi-card {
  height: 120px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  }

  .kpi-content {
    display: flex;
    flex-direction: column;
    height: 100%;

    .kpi-title {
      font-size: 14px;
      color: #606266;
      margin-bottom: 10px;
    }

    .kpi-value {
      font-size: 24px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 10px;

      .kpi-unit {
        font-size: 14px;
        font-weight: normal;
        margin-left: 4px;
        color: #909399;
      }
    }

    .kpi-trend {
      display: flex;
      align-items: center;
      font-size: 12px;

      .el-icon {
        margin-right: 4px;
      }

      &.trend-up {
        color: #67c23a;
      }

      &.trend-down {
        color: #f56c6c;
      }

      &.trend-flat {
        color: #909399;
      }
    }
  }
}
</style>
