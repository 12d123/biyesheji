<template>
  <div class="chart-container">
    <div ref="chartContainer" class="chart" :style="{ height: chartHeight }"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'WarehouseComparisonChart',
  props: {
    chartData: {
      type: Array,
      default: () => []
    },
    chartHeight: {
      type: String,
      default: '350px'
    }
  },
  data() {
    return {
      chartInstance: null,
      isMounted: false,
      retryCount: 0,  // 重试计数器
      maxRetries: 10  // 最大重试次数
    };
  },
  mounted() {
    this.isMounted = true;
    console.log('组件已挂载，准备初始化图表');
    
    // 等待下一个 tick 确保 DOM 已渲染
    this.$nextTick(() => {
      setTimeout(() => {
        this.initializeChart();
      }, 100);
    });
  },
  beforeUnmount() {
    if (this.chartInstance) {
      this.chartInstance.dispose();
      this.chartInstance = null;
    }
  },
  methods: {
    initializeChart() {
      console.log('初始化图表，refs:', this.$refs);

      if (!this.$refs.chartContainer) {
        console.error('图表容器未找到，延迟重试');

        // 添加重试计数器，防止无限循环
        this.retryCount++;
        if (this.retryCount < this.maxRetries) {
          console.log(`重试次数: ${this.retryCount}/${this.maxRetries}`);
          setTimeout(() => this.initializeChart(), 200);
        } else {
          console.error('已达到最大重试次数，停止重试');
        }
        return;
      }

      try {
        // 销毁现有实例
        if (this.chartInstance) {
          this.chartInstance.dispose();
        }

        // 创建新实例
        this.chartInstance = echarts.init(this.$refs.chartContainer);
        console.log('图表实例创建成功');
        
        // 立即更新图表
        this.updateChart();
        
        // 添加窗口调整大小监听
        window.addEventListener('resize', this.handleResize);
        
      } catch (error) {
        console.error('初始化图表失败:', error);
      }
    },

    updateChart() {
      if (!this.chartInstance) {
        console.warn('图表实例未就绪，跳过更新');
        return;
      }

      if (!this.chartData || this.chartData.length === 0) {
        console.log('没有数据，显示空状态');
        this.chartInstance.setOption({
          title: {
            text: '暂无数据',
            left: 'center',
            top: 'center',
            textStyle: {
              color: '#999',
              fontSize: 16
            }
          }
        });
        return;
      }

      console.log('更新图表数据:', this.chartData);

      const option = {
        title: {
          text: '仓库环境对比',
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
          data: ['温度', '湿度'],
          top: '10%'
        },
        grid: {
          left: '50px',
          right: '50px',
          bottom: '50px',
          top: '80px'
        },
        xAxis: {
          type: 'category',
          data: this.chartData.map(item => item.name),
          axisLabel: {
            rotate: 30,
            fontSize: 12
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '温度 (°C)',
            position: 'left'
          },
          {
            type: 'value',
            name: '湿度 (%)',
            position: 'right'
          }
        ],
        series: [
          {
            name: '温度',
            type: 'bar',
            data: this.chartData.map(item => parseFloat(item.temperature) || 0),
            yAxisIndex: 0,
            itemStyle: {
              color: '#5470c6'
            },
            label: {
              show: true,
              position: 'top'
            }
          },
          {
            name: '湿度',
            type: 'bar',
            data: this.chartData.map(item => parseFloat(item.humidity) || 0),
            yAxisIndex: 1,
            itemStyle: {
              color: '#91cc75'
            },
            label: {
              show: true,
              position: 'top'
            }
          }
        ]
      };

      try {
        this.chartInstance.setOption(option, true);
        console.log('图表更新成功');
      } catch (error) {
        console.error('设置图表选项失败:', error);
      }
    },

    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize();
      }
    }
  },

  watch: {
    chartData: {
      handler(newData) {
        console.log('数据变化:', newData);
        if (this.isMounted) {
          this.$nextTick(() => {
            setTimeout(() => {
              if (!this.chartInstance) {
                this.initializeChart();
              } else {
                this.updateChart();
              }
            }, 100);
          });
        }
      },
      deep: true
    }
  }
};
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 100%;
}

.chart {
  width: 100%;
  height: 100%;
  min-height: 300px;
}
</style>