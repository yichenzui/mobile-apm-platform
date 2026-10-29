<template>
  <div class="fps-chart-container">
    <v-chart
      class="chart"
      :option="chartOption"
      :loading="loading"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册ECharts组件
use([
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer
])

// Props
const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

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

  // 准备数据
  const chartData = props.data.map(item => ({
    name: item.range,
    value: item.userCount,
    percentage: item.percentage
  }))

  // 定义颜色
  const colors = ['#67c23a', '#e6a23c', '#f56c6c', '#909399', '#409eff']

  return {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        return `${params.name}<br/>
                用户数: ${params.value}<br/>
                占比: ${params.data.percentage}%`
      }
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      formatter: (name) => {
        const item = chartData.find(item => item.name === name)
        return `${name}: ${item ? item.percentage : 0}%`
      }
    },
    series: [
      {
        name: 'FPS分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
            formatter: (params) => {
              return `${params.name}
${params.data.percentage}%`
            }
          }
        },
        labelLine: {
          show: false
        },
        data: chartData,
        color: colors
      }
    ]
  }
})

// 监听数据变化 - 使用deep+immediate确保loading正确关闭
watch(() => props.data, (newData) => {
  loading.value = false
}, { immediate: true, deep: true })
</script>

<style lang="scss" scoped>
.fps-chart-container {
  width: 100%;
  height: 300px;

  .chart {
    width: 100%;
    height: 100%;
  }
}
</style>
