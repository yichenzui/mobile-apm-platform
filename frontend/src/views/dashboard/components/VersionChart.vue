<template>
  <div class="version-chart-container">
    <v-chart
      class="chart"
      :option="chartOption"
      :loading="loading"
      @click="handleChartClick"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册ECharts组件
use([
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CanvasRenderer
])

// Props
const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  metric: {
    type: String,
    default: 'crashRate'
  }
})

// Emits
const emit = defineEmits(['chartClick'])

// 响应式数据
const loading = ref(true)

// 计算图表配置
const chartOption = computed(() => {
  // 如果没有数据，返回空配置
  if (!props.data || props.data.length === 0) {
    return {
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'middle',
        textStyle: {
          color: '#909399'
        }
      }
    }
  }

  // 根据指标类型配置图表
  const metricConfig = getMetricConfig(props.metric)

  // 准备数据
  const xAxisData = props.data.map(item => item.version)
  const seriesData = props.data.map(item => item[props.metric])

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        if (!params || !params.length || !params[0]) return ''
        const param = params[0]
        return `版本: ${param.name}<br/>
                ${metricConfig.name}: ${param.value}${metricConfig.unit}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisLabel: {
        interval: 0,
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: `${metricConfig.name}(${metricConfig.unit})`,
      axisLabel: {
        formatter: (value) => {
          return `${value}${metricConfig.unit}`
        }
      }
    },
    series: [
      {
        name: metricConfig.name,
        type: 'bar',
        barWidth: '60%',
        itemStyle: {
          color: (params) => {
            // 最新版本使用特殊颜色
            if (params.dataIndex === xAxisData.length - 1) {
              return metricConfig.highlightColor || metricConfig.color
            }
            return metricConfig.color
          }
        },
        data: seriesData
      }
    ]
  }
})

// 获取指标配置
const getMetricConfig = (metric) => {
  const configs = {
    crashRate: {
      name: '崩溃率',
      unit: '%',
      color: '#f56c6c',
      highlightColor: '#ff7875'
    },
    startupTime: {
      name: '启动时间',
      unit: 'ms',
      color: '#409eff',
      highlightColor: '#66b1ff'
    },
    lagRate: {
      name: '卡顿率',
      unit: '%',
      color: '#e6a23c',
      highlightColor: '#ebb563'
    }
  }

  return configs[metric] || configs.crashRate
}

// 处理图表点击
const handleChartClick = (params) => {
  emit('chartClick', {
    name: params.name,
    value: params.value,
    dataIndex: params.dataIndex
  })
}

// 监听数据变化 - 使用deep+immediate确保loading正确关闭
watch(() => props.data, (newData) => {
  loading.value = false
}, { immediate: true, deep: true })
</script>

<style lang="scss" scoped>
.version-chart-container {
  width: 100%;
  height: 300px;

  .chart {
    width: 100%;
    height: 100%;
  }
}
</style>
