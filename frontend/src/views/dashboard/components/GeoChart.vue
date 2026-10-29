<template>
  <div class="geo-chart-container">
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
import { use, registerMap } from 'echarts/core'
import { MapChart, ScatterChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  VisualMapComponent,
  GeoComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import chinaJson from '@/assets/map/china-simple.json'

// 先注册ECharts组件（必须在registerMap之前！）
use([
  MapChart,
  ScatterChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  VisualMapComponent,
  GeoComponent,
  CanvasRenderer
])

// 再注册地图数据（依赖MapChart已注册）
registerMap('china', chinaJson)

// Props
const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['regionClick'])

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
  const geoData = props.data.map(item => ({
    name: item.province,
    value: item.userCount,
    crashRate: item.crashRate
  }))

  return {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        if (params.componentType === 'series') {
          const crashRateVal = params.data.crashRate
          const crashRateStr = (crashRateVal != null && !isNaN(crashRateVal)) ? crashRateVal.toFixed(2) : '0.00'
          const userCount = (params.value != null && !isNaN(params.value)) ? params.value : 0
          return `${params.name}<br/>
                  用户数: ${userCount}<br/>
                  崩溃率: ${crashRateStr}%`
        }
        return params.name
      }
    },
    visualMap: {
      min: 0,
      max: geoData.length > 0 ? Math.max(...geoData.map(item => item.value)) : 100,
      left: 'left',
      top: 'bottom',
      text: ['高', '低'],
      calculable: true,
      inRange: {
        color: ['#e0f3ff', '#006eff']
      }
    },
    geo: {
      map: 'china',
      roam: true,
      zoom: 1.2,
      label: {
        show: true,
        fontSize: 8
      },
      itemStyle: {
        areaColor: '#f7f9fc',
        borderColor: '#4a6fa5',
        borderWidth: 1.2,
        shadowColor: 'rgba(74,111,165,0.35)',
        shadowBlur: 8,
        shadowOffsetY: 4
      },
      emphasis: {
        itemStyle: {
          areaColor: '#e0f3ff'
        },
        label: {
          show: true,
          fontSize: 10
        }
      }
    },
    series: [
      {
        name: '用户分布',
        type: 'map',
        map: 'china',
        geoIndex: 0,
        itemStyle: {
          areaColor: '#f7f9fc',
          borderColor: '#33507a',
          borderWidth: 1.4
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 10,
            color: '#1f2d3d'
          },
          itemStyle: {
            areaColor: '#a8d4ff',
            borderColor: '#1f4e8c',
            borderWidth: 1.6
          }
        },
        select: {
          itemStyle: {
            areaColor: '#a8d4ff'
          }
        },
        data: geoData
      }
    ]
  }
})

// 处理图表点击
const handleChartClick = (params) => {
  if (params.componentType === 'series') {
    emit('regionClick', {
      name: params.name,
      value: params.value,
      crashRate: params.data.crashRate
    })
  }
}

// 监听数据变化 - 使用deep+immediate确保loading正确关闭
watch(() => props.data, (newData) => {
  loading.value = false
}, { immediate: true, deep: true })
</script>

<style lang="scss" scoped>
.geo-chart-container {
  width: 100%;
  height: 300px;

  .chart {
    width: 100%;
    height: 100%;
  }
}
</style>
