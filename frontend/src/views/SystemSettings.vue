<template>
  <div class="system-settings">
    <!-- 页面标题和操作 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <h1>⚙️ 系统设置</h1>
          <p>管理系统参数、告警阈值和系统状态</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="refreshAllData" :loading="refreshing">
            🔄 刷新数据
          </el-button>
          <el-button @click="testAllApis" :loading="testing">
            🧪 测试API连接
          </el-button>
          <el-button @click="backToHome">
            📊 返回首页
          </el-button>
        </div>
      </div>
      
      <!-- 系统状态指示器 -->
      <div class="system-status-indicators">
        <el-tag v-if="systemHealth.status === 'healthy' || systemHealth.overallHealth === '健康'" type="success">
          ✅ 系统运行正常
        </el-tag>
        <el-tag v-else-if="systemHealth.status === 'UNKNOWN'" type="warning">
          ⚠️ 系统状态未知
        </el-tag>
        <el-tag v-else type="danger">
          ❌ 系统异常
        </el-tag>
        <span class="status-info">最后更新: {{ lastUpdateTimeFormatted }}</span>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="settings-content">
      <el-tabs v-model="activeTab" type="border-card" class="settings-tabs">
        <!-- 系统参数选项卡 -->
        <el-tab-pane label="📋 系统参数" name="params">
          <SystemParams 
            :params="systemParams" 
            :loading="loading.params"
            :saving="loading.saveParams"
            @update="handleUpdateParams"
          />
        </el-tab-pane>

        <!-- 告警阈值选项卡 -->
        <el-tab-pane label="🚨 告警阈值" name="thresholds">
          <AlertThresholds 
            :thresholds="alertThresholds" 
            :loading="loading.thresholds"
            :saving="loading.saveThresholds"
            @update="handleUpdateThresholds"
            @toggle="handleToggleThreshold"
            @save="handleSaveThresholds"
          />
        </el-tab-pane>

        <!-- 系统状态选项卡 -->
        <el-tab-pane label="📊 系统状态" name="status">
          <SystemStatus 
            :health="systemHealth"
            :stats="systemStats"
            :loading="loading.status"
            @validate="handleValidateData"
            @reset="handleResetSystem"
          />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemApi } from '@/api/systemApi'
import { 
  adaptSystemParams, 
  adaptAlertThresholds, 
  adaptSystemHealth, 
  adaptSystemStats 
} from '@/utils/dataAdapter'
import { updateThresholds } from '@/utils/alerts'
import SystemParams from '@/components/system/SystemParams.vue'
import AlertThresholds from '@/components/system/AlertThresholds.vue'
import SystemStatus from '@/components/system/SystemStatus.vue'

const router = useRouter()

// 响应式数据
const activeTab = ref('params')
const refreshing = ref(false)
const testing = ref(false)
const loading = ref({
  params: false,
  thresholds: false,
  status: false,
  saveParams: false,
  saveThresholds: false
})

// 系统数据
const systemParams = ref({})
const alertThresholds = ref([])
const systemHealth = ref({})
const systemStats = ref({})
const lastUpdateTime = ref('')

// 计算属性
const lastUpdateTimeFormatted = computed(() => {
  if (!lastUpdateTime.value) return '暂无数据'
  return new Date(lastUpdateTime.value).toLocaleString('zh-CN')
})

// 生命周期
onMounted(() => {
  console.log('🚀 系统设置页面已加载')
  refreshAllData()
})

// 刷新所有数据
const refreshAllData = async () => {
  try {
    refreshing.value = true
    console.log('🔄 开始刷新所有系统数据...')
    await Promise.all([
      loadSystemParams(),
      loadAlertThresholds(),
      loadSystemStatus()
    ])
    lastUpdateTime.value = new Date().toISOString()
    ElMessage.success('系统数据刷新成功')
  } catch (error) {
    console.error('❌ 数据刷新失败:', error)
    ElMessage.error('数据刷新失败: ' + (error.response?.data?.message || error.message))
  } finally {
    refreshing.value = false
  }
}

// 加载系统参数
const loadSystemParams = async () => {
  loading.value.params = true
  try {
    console.log('📋 加载系统参数...')
    const response = await systemApi.getSystemParams()
    systemParams.value = adaptSystemParams(response)
    console.log('✅ 系统参数加载成功:', systemParams.value)
  } catch (error) {
    console.error('❌ 加载系统参数失败:', error)
    ElMessage.error('加载系统参数失败: ' + (error.response?.data?.message || error.message))
    // 使用默认参数作为降级方案
    systemParams.value = {
      'data.retention.days': 30,
      'auto.refresh.interval': 30,
      'max.history.records': 1000,
      'alert.notification.enabled': true
    }
  } finally {
    loading.value.params = false
  }
}

const loadAlertThresholds = async () => {
  loading.value.thresholds = true
  try {
    console.log('🚨 加载告警阈值...')
    const response = await systemApi.getAlertThresholds()
    console.log('📊 原始响应:', response)
    
    alertThresholds.value = adaptAlertThresholds(response)
    updateThresholds(alertThresholds.value)
    console.log('✅ 告警阈值加载成功:', alertThresholds.value)
    
    // 检查每个阈值是否有ID
    if (alertThresholds.value.length > 0) {
      alertThresholds.value.forEach((threshold, index) => {
        console.log(`📋 阈值 ${index}:`, {
          id: threshold.id,
          location: threshold.location,
          enabled: threshold.enabled,
          完整对象: threshold
        })
      })
    } else {
      console.warn('⚠️ 没有加载到阈值数据')
    }
  } catch (error) {
    console.error('❌ 加载告警阈值失败:', error)
    ElMessage.error('加载告警阈值失败: ' + (error.response?.data?.message || error.message))
    // 使用默认阈值作为降级方案
    alertThresholds.value = [
      {
        id: 1,
        location: 'A区仓库',
        minTemperature: 18,
        maxTemperature: 25,
        minHumidity: 40,
        maxHumidity: 60,
        description: 'A区仓库温湿度阈值',
        enabled: true
      }
    ]
  } finally {
    loading.value.thresholds = false
  }
}

// 加载系统状态
const loadSystemStatus = async () => {
  loading.value.status = true
  try {
    console.log('📊 加载系统状态...')
    const [healthRes, statsRes] = await Promise.all([
      systemApi.getSystemHealth(),
      systemApi.getSystemStats()
    ])
    systemHealth.value = adaptSystemHealth(healthRes)
    systemStats.value = adaptSystemStats(statsRes)
    console.log('✅ 系统状态加载成功:', { 
      health: systemHealth.value, 
      stats: systemStats.value 
    })
  } catch (error) {
    console.error('❌ 加载系统状态失败:', error)
    ElMessage.error('加载系统状态失败: ' + (error.response?.data?.message || error.message))
    // 使用默认状态作为降级方案
    systemHealth.value = { 
      status: 'healthy',
      overallHealth: '健康',
      timestamp: new Date().toISOString()
    }
    systemStats.value = { 
      systemParamsCount: 0,
      thresholdStats: { total: 0, enabled: 0 }
    }
  } finally {
    loading.value.status = false
  }
}

// 更新系统参数
const handleUpdateParams = async (params) => {
  loading.value.saveParams = true
  try {
    console.log('💾 更新系统参数:', params)
    const response = await systemApi.updateSystemParams(params)
    console.log('✅ 系统参数更新成功:', response)
    ElMessage.success(response.message || '系统参数更新成功')
    
    // 重新加载参数以确保数据一致
    await loadSystemParams()
  } catch (error) {
    console.error('❌ 更新系统参数失败:', error)
    ElMessage.error('更新系统参数失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value.saveParams = false
  }
}

// 批量更新告警阈值
const handleSaveThresholds = async (thresholds) => {
  loading.value.saveThresholds = true
  try {
    console.log('💾 批量更新告警阈值:', thresholds)
    const response = await systemApi.updateThresholds(thresholds)
    console.log('✅ 告警阈值批量更新成功:', response)
    ElMessage.success(response.message || '告警阈值更新成功')
    
    // 更新本地数据
    alertThresholds.value = adaptAlertThresholds(response)
    updateThresholds(alertThresholds.value)
  } catch (error) {
    console.error('❌ 批量更新告警阈值失败:', error)
    ElMessage.error('批量更新告警阈值失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value.saveThresholds = false
  }
}

// 更新单个阈值（用于内联编辑）
const handleUpdateThresholds = async (id, threshold) => {
  try {
    console.log('📝 更新单个阈值:', { id, threshold })
    const response = await systemApi.updateThreshold(id, threshold)
    console.log('✅ 单个阈值更新成功:', response)
    ElMessage.success('阈值更新成功')
    
    // 重新加载阈值列表以确保数据一致
    await loadAlertThresholds()
    // 重新加载后会自动调用updateThresholds
  } catch (error) {
    console.error('❌ 更新单个阈值失败:', error)
    ElMessage.error('更新阈值失败: ' + (error.response?.data?.message || error.message))
  }
}

// 切换阈值状态 - 修复：添加严格的参数验证
const handleToggleThreshold = async (threshold) => {
  try {
    console.log('🔄 SystemSettings - handleToggleThreshold 接收到的参数:', {
      参数: threshold,
      类型: typeof threshold,
      是否为对象: typeof threshold === 'object',
      是否有ID: threshold && threshold.id !== undefined,
      ID值: threshold ? threshold.id : '无',
      完整对象: threshold
    })
    
    // 严格的参数验证
    if (!threshold) {
      console.error('❌ 参数为空')
      ElMessage.error('参数错误：阈值为空')
      return
    }
    
    if (typeof threshold !== 'object') {
      console.error('❌ 参数不是对象:', threshold)
      ElMessage.error('参数错误：期望阈值对象')
      return
    }
    
    if (!threshold.id) {
      console.error('❌ 阈值对象缺少ID:', threshold)
      ElMessage.error('阈值ID不存在，无法切换状态')
      return
    }
    
    const newEnabledState = !threshold.enabled
    console.log('🔄 切换阈值状态:', threshold.id, '从', threshold.enabled, '到', newEnabledState)
    
    const response = await systemApi.toggleThreshold(threshold.id, newEnabledState)
    console.log('✅ 阈值状态切换成功:', response)
    
    // 更新本地状态
    threshold.enabled = newEnabledState
    ElMessage.success(response.message || (newEnabledState ? '已启用阈值' : '已禁用阈值'))
    
  } catch (error) {
    console.error('❌ 切换阈值状态失败:', error)
    
    // 更详细的错误信息
    let errorMessage = '切换阈值状态失败'
    if (error.response) {
      // 服务器响应了错误状态码
      errorMessage += `: ${error.response.status} - ${error.response.data?.message || error.response.statusText}`
    } else if (error.request) {
      // 请求已发出但没有收到响应
      errorMessage += ': 无法连接到服务器'
    } else {
      // 其他错误
      errorMessage += `: ${error.message}`
    }
    
    ElMessage.error(errorMessage)
  }
}

// 数据验证
const handleValidateData = async (testData) => {
  try {
    console.log('🔬 数据验证:', testData)
    const response = await systemApi.validateData(testData || {
      location: 'A区仓库',
      temperature: 22.5,
      humidity: 55.0
    })
    console.log('✅ 数据验证完成:', response)
    
    if (response.data && response.data.valid) {
      ElMessage.success('数据验证通过: ' + (response.message || '所有数据正常'))
    } else {
      ElMessage.warning('数据验证警告: ' + (response.message || '数据超出阈值范围'))
    }
    
    return response.data
  } catch (error) {
    console.error('❌ 数据验证失败:', error)
    ElMessage.error('数据验证失败: ' + (error.response?.data?.message || error.message))
  }
}

// 重置系统
const handleResetSystem = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将重置所有系统设置为默认值，是否继续？',
      '警告',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    console.log('🔄 重置系统配置...')
    const response = await systemApi.resetToDefault()
    console.log('✅ 系统配置重置成功:', response)
    ElMessage.success(response.message || '系统已重置为默认配置')
    
    // 重新加载所有数据
    await refreshAllData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('❌ 重置系统失败:', error)
      ElMessage.error('重置系统失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

// API连接测试
const testAllApis = async () => {
  testing.value = true
  try {
    console.log('🧪 开始测试所有系统设置API...')
    
    const apis = [
      { 
        name: '获取系统参数', 
        call: () => systemApi.getSystemParams(),
        expected: 'object'
      },
      { 
        name: '获取告警阈值', 
        call: () => systemApi.getAlertThresholds(),
        expected: 'array' 
      },
      { 
        name: '获取系统健康', 
        call: () => systemApi.getSystemHealth(),
        expected: 'object'
      },
      { 
        name: '获取系统统计', 
        call: () => systemApi.getSystemStats(),
        expected: 'object'
      },
      { 
        name: '获取启用的阈值', 
        call: () => systemApi.getEnabledThresholds(),
        expected: 'array'
      }
    ]
    
    const results = []
    
    for (const api of apis) {
      try {
        console.log(`🔍 测试: ${api.name}`)
        const response = await api.call()
        console.log(`✅ ${api.name} 成功:`, response)
        
        let isValid = false
        const data = response?.data || response
        
        if (api.expected === 'array') {
          isValid = Array.isArray(data)
        } else if (api.expected === 'object') {
          isValid = typeof data === 'object' && data !== null
        }
        
        results.push({
          name: api.name,
          status: 'success',
          data: data,
          isValid
        })
        
        ElMessage.success(`${api.name} 连接成功`)
      } catch (error) {
        console.error(`❌ ${api.name} 失败:`, error)
        results.push({
          name: api.name,
          status: 'error',
          error: error.response?.data?.message || error.message,
          statusCode: error.response?.status
        })
        
        ElMessage.error(`${api.name} 连接失败: ${error.response?.data?.message || error.message}`)
      }
    }
    
    console.log('📊 API测试结果:', results)
    return results
  } finally {
    testing.value = false
  }
}

// 返回首页
const backToHome = () => {
  router.push('/')
}
</script>

<style scoped>
.system-settings {
  padding: 20px;
  background: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  background: white;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.header-title h1 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.header-title p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.system-status-indicators {
  display: flex;
  align-items: center;
  gap: 15px;
}

.status-info {
  color: #909399;
  font-size: 14px;
}

.settings-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.settings-tabs {
  border: none;
}

.settings-tabs :deep(.el-tabs__header) {
  background: #f5f7fa;
  border-radius: 8px 8px 0 0;
  margin: 0;
}

.settings-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
}

.settings-tabs :deep(.el-tabs__content) {
  padding: 20px;
}
</style>