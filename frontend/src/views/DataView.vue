<template>
  <div class="data-view-container">
    <div class="page-header">
      <h1>数据查看</h1>
      <p>查看传感器数据和统计信息</p>
    </div>

    <div class="content">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card>
            <template #header>
              <span>权限信息</span>
            </template>
            <el-alert
              :title="`当前用户: ${authStore.user?.username || '未登录'} (${authStore.user?.role || '未知'})`"
              :description="getRoleDescription(authStore.user?.role)"
              :type="getAlertType(authStore.user?.role)"
              show-icon
              :closable="false"
            />
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>数据统计</span>
            </template>
            <div class="stats">
              <div class="stat-item">
                <span class="label">总数据量:</span>
                <span class="value">{{ stats.total }}</span>
              </div>
              <div class="stat-item">
                <span class="label">今日新增:</span>
                <span class="value">{{ stats.today }}</span>
              </div>
              <div class="stat-item">
                <span class="label">异常数据:</span>
                <span class="value">{{ stats.abnormal }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>快速操作</span>
            </template>
            <div class="actions">
              <el-button type="primary" @click="refreshData" :loading="loading">
                刷新数据
              </el-button>
              <el-button v-if="authStore.hasPermission('OPERATOR')" type="success" @click="exportData">
                导出数据
              </el-button>
              <el-button v-if="authStore.hasPermission('OPERATOR')" type="warning" @click="showAlertData">
                查看告警
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 数据筛选 -->
      <el-card style="margin-top: 20px;">
        <template #header>
          <span>数据筛选</span>
        </template>
        <el-form :model="filterForm" inline>
          <el-form-item label="位置">
            <el-select v-model="filterForm.location" placeholder="选择位置" style="width: 150px;">
              <el-option label="全部" value="" />
              <el-option label="A区仓库" value="A区仓库" />
              <el-option label="B区仓库" value="B区仓库" />
              <el-option label="C区仓库" value="C区仓库" />
              <el-option label="冷冻库" value="冷冻库" />
              <el-option label="常温库" value="常温库" />
              <el-option label="贵重物品区" value="贵重物品区" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 250px;"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="applyFilter">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 数据表格 -->
      <el-card style="margin-top: 20px;">
        <template #header>
          <span>传感器数据</span>
        </template>
        <el-table
          :data="sensorData"
          style="width: 100%"
          v-loading="loading"
          :default-sort="{ prop: 'time', order: 'descending' }"
        >
          <el-table-column prop="id" label="ID" width="80" sortable />
          <el-table-column prop="temperature" label="温度" width="100" sortable>
            <template #default="scope">
              <span :class="getTemperatureClass(scope.row.temperature, scope.row.location)">
                {{ scope.row.temperature }}℃
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="humidity" label="湿度" width="100" sortable>
            <template #default="scope">
              <span :class="getHumidityClass(scope.row.humidity, scope.row.location)">
                {{ scope.row.humidity }}%
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="pm25" label="PM2.5" width="100" sortable>
            <template #default="scope">
              <span :class="getPm25Class(scope.row.pm25, scope.row.location)">
                {{ scope.row.pm25 }} μg/m³
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="smoke" label="烟雾" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.smoke ? 'danger' : 'success'" size="small">
                {{ scope.row.smoke ? '有烟雾' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="位置" width="120" />
          <el-table-column prop="sensorType" label="传感器类型" width="120" />
          <el-table-column prop="time" label="记录时间" width="180" sortable />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag 
                :type="getDataStatus(scope.row.temperature, scope.row.humidity, scope.row.location).type"
                size="small"
              >
                {{ getDataStatus(scope.row.temperature, scope.row.humidity, scope.row.location).text }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination" style="margin-top: 20px;">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>

      <!-- 数据图表 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>温度变化趋势</span>
            </template>
            <div style="height: 300px;">
              <TemperatureChart :chartData="temperatureData" />
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>湿度变化趋势</span>
            </template>
            <div style="height: 300px;">
              <HumidityChart :chartData="humidityData" />
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="24">
          <el-card>
            <template #header>
              <span>PM2.5 变化趋势</span>
            </template>
            <div style="height: 300px;">
              <PM25TrendChart :chartData="pm25Data" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import sensorApi from '@/api/sensorApi'
import webSocketService from '@/api/websocket'
import { checkDataAbnormal } from '@/utils/alerts'
import TemperatureChart from '@/components/TemperatureChart.vue'
import HumidityChart from '@/components/HumidityChart.vue'
import PM25TrendChart from '@/components/PM25TrendChart.vue'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const sensorData = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 筛选条件
const filterForm = ref({
  location: '',
  dateRange: []
})

// 统计数据
const stats = ref({
  total: 0,
  today: 0,
  abnormal: 0
})

// 图表数据
const temperatureData = ref([])
const humidityData = ref([])
const pm25Data = ref([])

// 模拟仓库范围数据
const warehouseRanges = {
  'A区仓库': { 
    name: 'A区仓库', 
    temperature: { min: 18, max: 25 }, 
    humidity: { min: 40, max: 65 },
    pm25: { min: 0, max: 35 },
    smoke: false,
    description: '普通物品存储区' 
  },
  'B区仓库': { 
    name: 'B区仓库', 
    temperature: { min: 20, max: 28 }, 
    humidity: { min: 45, max: 70 },
    pm25: { min: 0, max: 35 },
    smoke: false,
    description: '电子产品存储区' 
  },
  'C区仓库': { 
    name: 'C区仓库', 
    temperature: { min: 15, max: 22 }, 
    humidity: { min: 35, max: 60 },
    pm25: { min: 0, max: 35 },
    smoke: false,
    description: '精密仪器存储区' 
  },
  '冷冻库': { 
    name: '冷冻库', 
    temperature: { min: -10, max: 5 }, 
    humidity: { min: 30, max: 50 },
    pm25: { min: 0, max: 35 },
    smoke: false,
    description: '冷冻食品存储区' 
  },
  '常温库': { 
    name: '常温库', 
    temperature: { min: 18, max: 25 }, 
    humidity: { min: 45, max: 65 },
    pm25: { min: 0, max: 35 },
    smoke: false,
    description: '常温物品存储区' 
  },
  '贵重物品区': { 
    name: '贵重物品区', 
    temperature: { min: 20, max: 24 }, 
    humidity: { min: 40, max: 55 },
    pm25: { min: 0, max: 35 },
    smoke: false,
    description: '贵重物品存储区' 
  }
}

// 生命周期
onMounted(() => {
  loadData()
  initWebSocket()
})

onUnmounted(() => {
  webSocketService.disconnect()
})

// 初始化WebSocket连接
const initWebSocket = async () => {
  try {
    await webSocketService.connect()
    ElMessage.success('WebSocket连接成功')
    
    // 注册传感器数据回调
    webSocketService.on('onSensorData', handleSensorData)
    
    // 注册连接状态回调
    webSocketService.on('onConnect', () => {
      ElMessage.info('WebSocket重新连接成功')
    })
    
    webSocketService.on('onDisconnect', () => {
      ElMessage.warning('WebSocket连接已断开')
    })
    
  } catch (error) {
    console.error('WebSocket连接失败:', error)
    ElMessage.error('WebSocket连接失败，请刷新页面重试')
  }
}

// 处理传感器数据
const handleSensorData = (data) => {
  try {
    // 添加新数据到列表开头
    sensorData.value.unshift(data)
    
    // 限制数据量，只保留最近100条
    if (sensorData.value.length > 100) {
      sensorData.value = sensorData.value.slice(0, 100)
    }
    
    total.value = sensorData.value.length
    
    // 重新计算统计数据
    calculateStats(sensorData.value)
    
    // 重新生成图表数据
    generateChartData(sensorData.value)
    
    ElMessage.success('收到新的传感器数据')
    
  } catch (error) {
    console.error('处理传感器数据失败:', error)
  }
}

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    
    // 获取所有数据
    const data = await sensorApi.getAllData()
    sensorData.value = data
    total.value = data.length
    
    // 计算统计数据
    calculateStats(data)
    
    // 生成图表数据
    generateChartData(data)
    
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 计算统计数据
const calculateStats = (data) => {
  const today = new Date().toDateString()
  let todayCount = 0
  let abnormalCount = 0
  
  data.forEach(item => {
    // 计算今日数据
    if (item.time) {
      const itemDate = new Date(item.time).toDateString()
      if (itemDate === today) {
        todayCount++
      }
    }
    
    // 计算异常数据
    const alert = checkDataAbnormal(item)
    if (alert.isAbnormal) {
      abnormalCount++
    }
  })
  
  stats.value = {
    total: data.length,
    today: todayCount,
    abnormal: abnormalCount
  }
}

// 生成图表数据
const generateChartData = (data) => {
  // 取最近20条数据
  const recentData = data.slice(0, 20).reverse()
  
  temperatureData.value = recentData.map(item => ({
    time: formatTime(item.time),
    temperature: item.temperature
  }))
  
  humidityData.value = recentData.map(item => ({
    time: formatTime(item.time),
    humidity: item.humidity
  }))
  
  pm25Data.value = recentData.map(item => ({
    time: formatTime(item.time),
    pm25: item.pm25
  }))
}

// 刷新数据
const refreshData = () => {
  loadData()
  ElMessage.success('数据已刷新')
}

// 导出数据
const exportData = () => {
  ElMessage.info('导出功能开发中...')
}

// 查看告警数据
const showAlertData = async () => {
  try {
    const data = await sensorApi.getAbnormalData()
    if (data.length === 0) {
      ElMessage.info('暂无告警数据')
    } else {
      ElMessageBox.alert(
        `当前有 ${data.length} 条告警数据`,
        '告警信息',
        {
          confirmButtonText: '确定'
        }
      )
    }
  } catch (error) {
    ElMessage.error('获取告警数据失败: ' + error.message)
  }
}

// 应用筛选
const applyFilter = () => {
  ElMessage.info('筛选功能开发中...')
}

// 重置筛选
const resetFilter = () => {
  filterForm.value = {
    location: '',
    dateRange: []
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  loadData()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadData()
}

// 辅助方法
const getRoleDescription = (role) => {
  const descriptions = {
    'VIEWER': '查看员 - 仅数据查看权限',
    'OPERATOR': '操作员 - 数据录入和查看权限',
    'ADMIN': '管理员 - 用户管理和数据管理权限',
    'SUPER_ADMIN': '超级管理员 - 系统所有权限'
  }
  return descriptions[role] || '未知角色'
}

const getAlertType = (role) => {
  const types = {
    'VIEWER': 'info',
    'OPERATOR': 'success', 
    'ADMIN': 'warning',
    'SUPER_ADMIN': 'error'
  }
  return types[role] || 'info'
}

const getTemperatureClass = (temperature, location) => {
  if (!warehouseRanges[location]) return 'temp-unknown'
  
  const range = warehouseRanges[location]
  if (temperature < range.temperature.min) return 'temp-low'
  if (temperature > range.temperature.max) return 'temp-high'
  return 'temp-normal'
}

const getHumidityClass = (humidity, location) => {
  if (!warehouseRanges[location]) return 'humidity-unknown'
  
  const range = warehouseRanges[location]
  if (humidity < range.humidity.min) return 'humidity-low'
  if (humidity > range.humidity.max) return 'humidity-high'
  return 'humidity-normal'
}

const getPm25Class = (pm25, location) => {
  if (!warehouseRanges[location]) return 'pm25-unknown'
  
  const range = warehouseRanges[location]
  if (pm25 < range.pm25.min) return 'pm25-low'
  if (pm25 > range.pm25.max) return 'pm25-high'
  return 'pm25-normal'
}

const getDataStatus = (temperature, humidity, location) => {
  const checkResult = checkDataAbnormal({ temperature, humidity, location })
  if (checkResult.isAbnormal) {
    return { type: 'danger', text: '异常' }
  }
  return { type: 'success', text: '正常' }
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  try {
    const date = new Date(timestamp)
    return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
  } catch (e) {
    return timestamp
  }
}
</script>

<style scoped>
.data-view-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
}

.page-header p {
  margin: 5px 0 0 0;
  color: #909399;
}

.stats {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.stat-item:last-child {
  border-bottom: none;
}

.label {
  color: #606266;
}

.value {
  font-weight: bold;
  color: #409eff;
}

.actions {
  display: flex;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}

/* 温度样式 */
.temp-normal {
  color: #67c23a;
}

.temp-low {
  color: #409eff;
}

.temp-high {
  color: #f56c6c;
}

/* 湿度样式 */
.humidity-normal {
  color: #67c23a;
}

.humidity-low {
  color: #e6a23c;
}

.humidity-high {
  color: #f56c6c;
}

/* PM2.5样式 */
.pm25-normal {
  color: #67c23a;
}

.pm25-low {
  color: #409eff;
}

.pm25-high {
  color: #f56c6c;
}
</style>