<template>
  <div class="data-management-container">
    <div class="page-header">
      <h1>数据管理</h1>
      <p>管理传感器数据和系统配置</p>
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

      <!-- 数据管理操作 -->
      <el-card style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>传感器数据管理</span>
            <div class="header-actions">
              <el-button type="primary" @click="showAddForm">
                <el-icon><Plus /></el-icon> 新增数据
              </el-button>
              <el-button type="danger" @click="batchDelete" :disabled="selectedRows.length === 0">
                <el-icon><Delete /></el-icon> 批量删除
              </el-button>
              <el-button @click="refreshData" :loading="loading">
                <el-icon><Refresh /></el-icon> 刷新数据
              </el-button>
            </div>
          </div>
        </template>

        <!-- 数据表格 -->
        <el-table
          v-loading="loading"
          :data="sensorData"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          :default-sort="{ prop: 'time', order: 'descending' }"
        >
          <el-table-column type="selection" width="55" />
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
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button size="small" @click="editData(scope.row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button size="small" type="danger" @click="deleteData(scope.row.id)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
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

      <!-- 系统配置管理 -->
      <el-card style="margin-top: 20px;">
        <template #header>
          <span>系统配置管理</span>
        </template>
        <div class="config-section">
          <h3>告警阈值配置</h3>
          <el-form :model="alertConfig" label-width="120px">
            <el-form-item label="温度阈值">
              <el-input-number v-model="alertConfig.temperatureMax" placeholder="最高温度" :min="-20" :max="50" />
              <span style="margin: 0 10px;">~</span>
              <el-input-number v-model="alertConfig.temperatureMin" placeholder="最低温度" :min="-20" :max="50" />
            </el-form-item>
            <el-form-item label="湿度阈值">
              <el-input-number v-model="alertConfig.humidityMax" placeholder="最高湿度" :min="0" :max="100" />
              <span style="margin: 0 10px;">~</span>
              <el-input-number v-model="alertConfig.humidityMin" placeholder="最低湿度" :min="0" :max="100" />
            </el-form-item>
            <el-form-item label="PM2.5阈值">
              <el-input-number v-model="alertConfig.pm25Max" placeholder="最高PM2.5" :min="0" :max="500" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveAlertConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>

    <!-- 新增/编辑数据对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="温度">
          <el-input-number v-model="formData.temperature" placeholder="请输入温度" :min="-20" :max="50" />
        </el-form-item>
        <el-form-item label="湿度">
          <el-input-number v-model="formData.humidity" placeholder="请输入湿度" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="PM2.5">
          <el-input-number v-model="formData.pm25" placeholder="请输入PM2.5" :min="0" :max="500" />
        </el-form-item>
        <el-form-item label="烟雾">
          <el-switch v-model="formData.smoke" />
        </el-form-item>
        <el-form-item label="位置">
          <el-select v-model="formData.location" placeholder="选择位置" style="width: 100%;">
            <el-option label="A区仓库" value="A区仓库" />
            <el-option label="B区仓库" value="B区仓库" />
            <el-option label="C区仓库" value="C区仓库" />
            <el-option label="冷冻库" value="冷冻库" />
            <el-option label="常温库" value="常温库" />
            <el-option label="贵重物品区" value="贵重物品区" />
          </el-select>
        </el-form-item>
        <el-form-item label="传感器类型">
          <el-select v-model="formData.sensorType" placeholder="选择传感器类型" style="width: 100%;">
            <el-option label="温湿度传感器" value="温湿度传感器" />
            <el-option label="PM2.5传感器" value="PM2.5传感器" />
            <el-option label="烟雾传感器" value="烟雾传感器" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveData">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import sensorApi from '@/api/sensorApi'
import { checkDataAbnormal } from '@/utils/alerts'
import { Plus, Delete, Refresh, Edit } from '@element-plus/icons-vue'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const sensorData = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedRows = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增数据')
const formData = ref({
  temperature: '',
  humidity: '',
  pm25: '',
  smoke: false,
  location: '',
  sensorType: ''
})

// 告警配置
const alertConfig = ref({
  temperatureMax: 30,
  temperatureMin: 0,
  humidityMax: 80,
  humidityMin: 20,
  pm25Max: 50
})

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
  loadAlertConfig()
})

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    const data = await sensorApi.getAllData()
    sensorData.value = data
    total.value = data.length
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 加载告警配置
const loadAlertConfig = async () => {
  try {
    // 这里应该从后端获取配置，暂时使用默认值
    // const config = await sensorApi.getAlertConfig()
    // alertConfig.value = config
  } catch (error) {
    console.error('加载告警配置失败:', error)
  }
}

// 保存告警配置
const saveAlertConfig = async () => {
  try {
    // 这里应该调用后端API保存配置
    // await sensorApi.saveAlertConfig(alertConfig.value)
    ElMessage.success('配置保存成功')
  } catch (error) {
    console.error('保存告警配置失败:', error)
    ElMessage.error('保存告警配置失败: ' + error.message)
  }
}

// 刷新数据
const refreshData = () => {
  loadData()
  ElMessage.success('数据已刷新')
}

// 显示新增表单
const showAddForm = () => {
  dialogTitle.value = '新增数据'
  formData.value = {
    temperature: '',
    humidity: '',
    pm25: '',
    smoke: false,
    location: '',
    sensorType: ''
  }
  dialogVisible.value = true
}

// 编辑数据
const editData = (row) => {
  dialogTitle.value = '编辑数据'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 保存数据
const saveData = async () => {
  try {
    if (!formData.value.temperature || !formData.value.humidity || !formData.value.location) {
      ElMessage.warning('请填写必要字段')
      return
    }

    if (formData.value.id) {
      // 编辑
      await sensorApi.updateSensorData(formData.value)
      ElMessage.success('数据更新成功')
    } else {
      // 新增
      await sensorApi.addSensorData(formData.value)
      ElMessage.success('数据添加成功')
    }

    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存数据失败:', error)
    ElMessage.error('保存数据失败: ' + error.message)
  }
}

// 删除数据
const deleteData = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条数据吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await sensorApi.deleteSensorData(id)
    ElMessage.success('数据删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除数据失败:', error)
      ElMessage.error('删除数据失败: ' + error.message)
    }
  }
}

// 批量删除
const batchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的数据')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条数据吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const ids = selectedRows.value.map(row => row.id)
    for (const id of ids) {
      await sensorApi.deleteSensorData(id)
    }

    ElMessage.success('数据删除成功')
    loadData()
    selectedRows.value = []
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除数据失败:', error)
      ElMessage.error('删除数据失败: ' + error.message)
    }
  }
}

// 处理选择变化
const handleSelectionChange = (val) => {
  selectedRows.value = val
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
</script>

<style scoped>
.data-management-container {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}

.config-section {
  margin-top: 20px;
}

.config-section h3 {
  margin: 0 0 20px 0;
  color: #303133;
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