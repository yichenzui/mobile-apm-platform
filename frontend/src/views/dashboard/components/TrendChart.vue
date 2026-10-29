<template>
  <div class="trend-chart-container">
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
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import dayjs from 'dayjs'

// 注册ECharts组件
use([
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
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
  const xAxisData = props.data.map(item => item.time)
  const seriesData = props.data.map(item => item[props.metric])

  return {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        if (!params || !params.length || !params[0]) return ''
        const param = params[0]
        return `${dayjs(param.name).format('YYYY-MM-DD HH:mm')}<br/>
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
      boundaryGap: false,
      data: xAxisData,
      axisLabel: {
        formatter: (value) => {
          return dayjs(value).format('MM-DD HH:mm')
        }
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
    dataZoom: [
      {
        type: 'inside',
        start: 0,
        end: 100
      },
      {
        start: 0,
        end: 100,
        height: 30,
        bottom: 10
      }
    ],
    series: [
      {
        name: metricConfig.name,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        sampling: 'average',
        itemStyle: {
          color: metricConfig.color
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: metricConfig.color + '40'
              },
              {
                offset: 1,
                color: metricConfig.color + '10'
              }
            ]
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
      color: '#f56c6c'
    },
    startupTime: {
      name: '启动时间',
      unit: 'ms',
      color: '#409eff'
    },
    lagRate: {
      name: '卡顿率',
      unit: '%',
      color: '#e6a23c'
    }
  }

  return configs[metric] || configs.crashRate
}

// 处理图表点击
const handleChartClick = (params) => {
  // 确保传递点击点的数据信息
  const pointData = {
    name: params.name,
    value: params.value,
    seriesName: params.seriesName
  }
  emit('chartClick', pointData)
}

// 监听数据变化 - 使用deep+immediate确保loading正确关闭
watch(() => props.data, (newData) => {
  loading.value = false
}, { immediate: true, deep: true })
</script>

<style lang="scss" scoped>
.trend-chart-container {
  width: 100%;
  height: 300px;

  .chart {
    width: 100%;
    height: 100%;
  }
}
</style>
