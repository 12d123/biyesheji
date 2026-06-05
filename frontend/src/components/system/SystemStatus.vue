<template>
  <div class="system-status">
    <el-row :gutter="20">
      <!-- 系统健康状态 -->
      <el-col :span="12">
        <el-card class="status-card">
          <template #header>
            <div class="card-header">
              <span>🩺 系统健康状态</span>
            </div>
          </template>
          
          <div v-loading="loading" class="health-status">
            <div v-if="health.overallHealth || health.status" class="status-item">
              <el-tag :type="(health.overallHealth === '健康' || health.status === 'healthy') ? 'success' : 'danger'" size="large">
                {{ (health.overallHealth === '健康' || health.status === 'healthy') ? '✅ 健康' : '❌ 异常' }}
              </el-tag>
            </div>
            
            <div v-if="health.configService" class="status-detail">
              <h4>配置服务状态</h4>
              <el-tag :type="health.configService === '正常' ? 'success' : 'danger'">
                {{ health.configService }}
              </el-tag>
            </div>
            
            <div v-if="health.thresholdService" class="status-detail">
              <h4>阈值服务状态</h4>
              <el-tag :type="health.thresholdService === '正常' ? 'success' : 'danger'">
                {{ health.thresholdService }}
              </el-tag>
            </div>
            
            <div v-if="health.systemParamsCount" class="status-detail">
              <h4>系统参数数量</h4>
              <el-tag type="info">{{ health.systemParamsCount }} 个</el-tag>
            </div>
            
            <div v-if="health.thresholdStats" class="status-detail">
              <h4>阈值配置统计</h4>
              <el-tag type="info">
                总计: {{ health.thresholdStats.total || 0 }}, 启用: {{ health.thresholdStats.enabled || 0 }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 系统统计信息 -->
      <el-col :span="12">
        <el-card class="stats-card">
          <template #header>
            <div class="card-header">
              <span>📈 系统统计</span>
            </div>
          </template>
          
          <div v-loading="loading" class="stats-content">
            <div v-if="stats" class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ stats.systemParamsCount || 0 }}</div>
                <div class="stat-label">系统参数</div>
              </div>
              
              <div class="stat-item">
                <div class="stat-value">{{ stats.thresholdStats?.total || 0 }}</div>
                <div class="stat-label">阈值配置</div>
              </div>
              
              <div class="stat-item">
                <div class="stat-value">{{ stats.thresholdStats?.enabled || 0 }}</div>
                <div class="stat-label">启用阈值</div>
              </div>
              
              <div class="stat-item">
                <div class="stat-value">{{ (stats.configServiceHealthy && stats.thresholdServiceHealthy) ? '✅' : '❌' }}</div>
                <div class="stat-label">服务状态</div>
              </div>
            </div>
            
            <el-empty v-else description="暂无统计信息" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统操作 -->
    <el-card class="operations-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>🔧 系统操作</span>
        </div>
      </template>
      
      <div class="operations-content">
        <el-button type="primary" @click="handleValidate">
          🔍 数据验证
        </el-button>
        
        <el-button type="warning" @click="handleReset">
          🔄 重置系统
        </el-button>
        
        <el-button type="info" @click="handleExportLogs">
          📤 导出日志
        </el-button>
        
        <el-button type="success" @click="handleBackup">
          💾 立即备份
        </el-button>
      </div>
      
      <el-divider />
      
      <!-- 系统信息 -->
      <div class="system-info">
        <h4>系统信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="启动时间">
            {{ formatTime(stats.startupTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="运行时长">
            {{ stats.uptime || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="系统版本">
            {{ stats.version || '1.0.0' }}
          </el-descriptions-item>
          <el-descriptions-item label="最后维护">
            {{ formatTime(stats.lastMaintenance) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const props = defineProps({
  health: {
    type: Object,
    default: () => ({})
  },
  stats: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['validate', 'reset'])

// 格式化字节大小
const formatBytes = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 获取磁盘颜色
const getDiskColor = (ratio) => {
  if (ratio > 0.3) return '#67c23a'
  if (ratio > 0.1) return '#e6a23c'
  return '#f56c6c'
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return '未知'
  try {
    return new Date(timestamp).toLocaleString()
  } catch {
    return timestamp
  }
}

// 数据验证
const handleValidate = () => {
  emit('validate')
}

// 重置系统
const handleReset = () => {
  emit('reset')
}

// 导出日志
const handleExportLogs = () => {
  ElMessage.info('导出日志功能开发中...')
}

// 立即备份
const handleBackup = () => {
  ElMessage.info('立即备份功能开发中...')
}
</script>

<style scoped>
.status-card, .stats-card {
  height: 100%;
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.health-status {
  min-height: 200px;
}

.status-item {
  text-align: center;
  margin-bottom: 20px;
}

.status-detail {
  margin-bottom: 15px;
}

.status-detail h4 {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
}

.stats-content {
  min-height: 200px;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.operations-content {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.system-info h4 {
  margin: 0 0 15px 0;
  color: #303133;
}
</style>