<template>
  <div class="pm25-trend-chart" :style="{ height: chartHeight }">
    <v-chart :option="chartOption" autoresize />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
} from 'echarts/components';
import VChart from 'vue-echarts';

use([
  CanvasRenderer,
  LineChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
]);

const props = defineProps({
  chartData: {
    type: Array,
    default: () => []
  },
  chartHeight: {
    type: String,
    default: '300px'
  }
});

const chartOption = computed(() => ({
  title: {
    text: 'PM2.5 变化趋势',
    left: 'center',
    top: 0,
    textStyle: { fontSize: 14 }
  },
  tooltip: {
    trigger: 'axis',
    formatter: '{b}<br/>PM2.5: {c} μg/m³'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: props.chartData.map(item => item.time),
    axisLabel: { rotate: 30 }
  },
  yAxis: {
    type: 'value',
    name: 'μg/m³',
    min: 0
  },
  series: [
    {
      name: 'PM2.5',
      type: 'line',
      data: props.chartData.map(item => item.pm25),
      smooth: true,
      lineStyle: { color: '#9b59b6', width: 3 },
      areaStyle: { color: 'rgba(155, 89, 182, 0.1)' }
    }
  ]
}));
</script>

<style scoped>
.pm25-trend-chart {
  width: 100%;
}
</style>