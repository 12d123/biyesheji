<template>
  <div class="chart-container">
    <div ref="chart" class="chart" :style="{ height: chartHeight }"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'TemperatureChart',
  props: {
    chartData: {
      type: Array,
      required: true
    },
    chartHeight: {
      type: String,
      default: '400px'
    }
  },
  data() {
    return {
      chartInstance: null
    };
  },
  mounted() {
    this.initChart();
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    if (this.chartInstance) {
      this.chartInstance.dispose();
    }
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    initChart() {
      this.chartInstance = echarts.init(this.$refs.chart);
      this.updateChart();
    },
    updateChart() {
      if (!this.chartInstance) return;

      // 处理数据，确保格式正确
      const processedData = this.processChartData();
      
      const option = {
        title: {
          text: '温度变化趋势',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['温度'],
          top: '10%'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          top: '20%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: processedData.times,
          axisLabel: {
            interval: 0,
            rotate: 30,
            margin: 10,
            textStyle: {
              fontSize: 10
            }
          },
          axisTick: {
            alignWithLabel: true
          }
        },
        yAxis: {
          type: 'value',
          name: '温度 (°C)',
          nameTextStyle: {
            padding: [0, 0, 0, -40]
          }
        },
        series: [
          {
            name: '温度',
            type: 'line',
            data: processedData.temperatures,
            itemStyle: {
              color: '#5470c6'
            },
            lineStyle: {
              color: '#5470c6',
              width: 3
            },
            symbol: 'circle',
            symbolSize: 6,
            label: {
              show: true,
              position: 'top',
              fontSize: 10
            },
            smooth: true
          }
        ]
      };

      this.chartInstance.setOption(option, true);
    },
    processChartData() {
      // 处理数据格式，确保兼容性
      if (!this.chartData || this.chartData.length === 0) {
        return { times: [], temperatures: [] };
      }

      const times = [];
      const temperatures = [];

      this.chartData.forEach(item => {
        if (item && item.time && item.temperature !== undefined) {
          times.push(item.time);
          temperatures.push(item.temperature);
        }
      });

      return { times, temperatures };
    },
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize();
      }
    }
  },
  watch: {
    chartData: {
      handler() {
        this.updateChart();
      },
      deep: true
    }
  }
};
</script>

<style scoped>
.chart-container {
  width: 100%;
  padding: 10px;
  background: #fff;
  border-radius: 8px;
}

.chart {
  width: 100%;
  min-height: 300px;
}
</style>