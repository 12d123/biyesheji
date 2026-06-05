<template>
  <div style="background: white; padding: 20px; border-radius: 8px; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h3 style="color: #333; margin-bottom: 15px;">简单测试图表</h3>
    <div ref="chartRef" style="width: 100%; height: 400px;"></div>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from 'vue';
import * as echarts from 'echarts';

export default defineComponent({
  name: 'SimpleTestChart',
  setup() {
    const chartRef = ref(null);

    onMounted(() => {
      console.log('🔄 开始初始化简单测试图表...');
      
      if (!chartRef.value) {
        console.error('❌ 图表容器未找到');
        return;
      }

      try {
        console.log('✅ 找到图表容器，开始初始化 ECharts');
        const chart = echarts.init(chartRef.value);
        
        const option = {
          title: {
            text: '测试图表 - ECharts 工作正常！',
            left: 'center',
            textStyle: {
              color: '#409EFF',
              fontSize: 18
            }
          },
          tooltip: {
            trigger: 'axis'
          },
          legend: {
            data: ['温度', '湿度'],
            top: '10%'
          },
          xAxis: {
            type: 'category',
            data: ['10:00', '10:05', '10:10', '10:15', '10:20', '10:25'],
            axisLine: {
              lineStyle: {
                color: '#409EFF'
              }
            }
          },
          yAxis: [{
            type: 'value',
            name: '温度(°C)',
            min: 20,
            max: 30,
            axisLine: {
              lineStyle: {
                color: '#FF6B6B'
              }
            }
          }, {
            type: 'value',
            name: '湿度(%)',
            min: 50,
            max: 90,
            axisLine: {
              lineStyle: {
                color: '#4ECDC4'
              }
            }
          }],
          series: [{
            name: '温度',
            data: [22, 25, 28, 26, 24, 27],
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            itemStyle: {
              color: '#FF6B6B'
            },
            lineStyle: {
              color: '#FF6B6B',
              width: 3
            }
          }, {
            name: '湿度',
            data: [65, 70, 75, 80, 78, 72],
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            yAxisIndex: 1,
            itemStyle: {
              color: '#4ECDC4'
            },
            lineStyle: {
              color: '#4ECDC4',
              width: 3
            }
          }],
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '20%',
            containLabel: true
          }
        };
        
        chart.setOption(option);
        console.log('✅ 测试图表配置设置完成');
        
        // 响应式调整
        const resizeChart = () => chart.resize();
        window.addEventListener('resize', resizeChart);
        
      } catch (error) {
        console.error('❌ 图表初始化失败:', error);
      }
    });

    return {
      chartRef
    };
  }
});
</script>