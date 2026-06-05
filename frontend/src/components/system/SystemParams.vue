<!-- src/components/system/SystemParams.vue -->
<template>
  <div class="system-params">
    <div class="params-header">
      <el-alert
        title="系统参数配置"
        description="管理系统运行参数和配置选项"
        type="info"
        :closable="false"
      />
      
      <div class="header-actions">
        <el-button type="primary" size="small" @click="handleSave" :loading="saving">
          💾 保存参数
        </el-button>
        <el-button size="small" @click="handleReset">
          🔄 重置默认
        </el-button>
      </div>
    </div>
    
    <div v-loading="loading" class="params-content">
      <el-empty v-if="Object.keys(localParams).length === 0" description="暂无系统参数配置" />
      
      <div v-else class="params-form">
        <el-form label-width="180px">
          <!-- 数据管理参数 -->
          <el-divider content-position="left">📊 数据管理</el-divider>
          
          <el-form-item label="数据保留天数">
            <el-input-number 
              v-model="localParams['data.retention.days']" 
              :min="1" 
              :max="365"
              controls-position="right"
            />
            <span class="param-description">设置传感器数据保留的天数</span>
          </el-form-item>
          
          <el-form-item label="自动刷新间隔(秒)">
            <el-input-number 
              v-model="localParams['auto.refresh.interval']" 
              :min="5" 
              :max="300"
              controls-position="right"
            />
            <span class="param-description">前端数据自动刷新的时间间隔</span>
          </el-form-item>
          
          <el-form-item label="最大历史记录数">
            <el-input-number 
              v-model="localParams['max.history.records']" 
              :min="100" 
              :max="10000"
              controls-position="right"
            />
            <span class="param-description">历史数据查询的最大记录数</span>
          </el-form-item>
          
          <!-- 告警设置 -->
          <el-divider content-position="left">🚨 告警设置</el-divider>
          
          <el-form-item label="启用告警通知">
            <el-switch 
              v-model="localParams['alert.notification.enabled']" 
              :active-value="true"
              :inactive-value="false"
            />
            <span class="param-description">是否启用邮件或短信告警通知</span>
          </el-form-item>
          
          <!-- 模拟数据 -->
          <el-divider content-position="left">🧪 模拟数据</el-divider>
          
          <el-form-item label="启用模拟数据生成">
            <el-switch 
              v-model="localParams['mock.data.generation']" 
              :active-value="true"
              :inactive-value="false"
            />
            <span class="param-description">是否启用模拟数据自动生成</span>
          </el-form-item>
          
          <!-- 用户设置 -->
          <el-divider content-position="left">👥 用户设置</el-divider>
          
          <el-form-item label="默认用户角色">
            <el-select v-model="localParams['user.default.role']" placeholder="请选择默认角色">
              <el-option label="管理员" value="ADMIN" />
              <el-option label="操作员" value="OPERATOR" />
              <el-option label="查看者" value="VIEWER" />
            </el-select>
            <span class="param-description">新用户的默认角色</span>
          </el-form-item>
          
          <el-form-item label="自动注销时间(分钟)">
            <el-input-number 
              v-model="localParams['user.auto.logout.minutes']" 
              :min="5" 
              :max="480"
              controls-position="right"
            />
            <span class="param-description">用户无操作自动注销的时间</span>
          </el-form-item>
          
          <el-form-item label="会话超时时间(分钟)">
            <el-input-number 
              v-model="localParams['user.session.timeout']" 
              :min="30" 
              :max="1440"
              controls-position="right"
            />
            <span class="param-description">用户会话超时时间</span>
          </el-form-item>
          
          <!-- 系统设置 -->
          <el-divider content-position="left">⚙️ 系统设置</el-divider>
          
          <el-form-item label="系统健康检查">
            <el-switch 
              v-model="localParams['system.health.check']" 
              :active-value="true"
              :inactive-value="false"
            />
            <span class="param-description">是否启用系统健康检查</span>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  params: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  },
  saving: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update'])

// 本地参数数据，用于前端状态管理
const localParams = ref({})

// 监听props变化，更新本地数据
watch(() => props.params, (newParams) => {
  console.log('📥 SystemParams 接收到参数数据:', newParams)
  
  // 确保所有参数都有默认值
  localParams.value = {
    // 默认值
    'data.retention.days': '30',
    'auto.refresh.interval': '30',
    'max.history.records': '1000',
    'alert.notification.enabled': 'true',
    'mock.data.generation': 'false',
    'user.default.role': 'VIEWER',
    'user.auto.logout.minutes': '30',
    'user.session.timeout': '120',
    'system.health.check': 'true',
    // 覆盖为实际值
    ...newParams
  }
  
  // 确保布尔值转换为布尔类型
  Object.keys(localParams.value).forEach(key => {
    if (localParams.value[key] === 'true') localParams.value[key] = true
    if (localParams.value[key] === 'false') localParams.value[key] = false
  })
}, { immediate: true, deep: true })

// 保存参数
const handleSave = () => {
  console.log('💾 保存系统参数:', localParams.value)
  
  // 将布尔值转换回字符串格式（后端期望的格式）
  const paramsToSave = { ...localParams.value }
  Object.keys(paramsToSave).forEach(key => {
    if (typeof paramsToSave[key] === 'boolean') {
      paramsToSave[key] = paramsToSave[key].toString()
    }
  })
  
  emit('update', paramsToSave)
}

// 重置为默认值
const handleReset = () => {
  localParams.value = {
    'data.retention.days': '30',
    'auto.refresh.interval': '30',
    'max.history.records': '1000',
    'alert.notification.enabled': 'true',
    'mock.data.generation': 'false',
    'user.default.role': 'VIEWER',
    'user.auto.logout.minutes': '30',
    'user.session.timeout': '120',
    'system.health.check': 'true'
  }
  ElMessage.info('已重置为默认参数')
}
</script>

<style scoped>
.params-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-actions {
  margin-left: 20px;
  display: flex;
  gap: 10px;
}

.params-content {
  min-height: 200px;
}

.params-form {
  max-width: 800px;
}

.param-description {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}

.el-divider {
  margin: 30px 0;
}

.el-form-item {
  margin-bottom: 22px;
}
</style>