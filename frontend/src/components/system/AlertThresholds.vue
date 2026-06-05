<template>
  <div class="alert-thresholds">
    <div class="thresholds-header">
      <el-alert
        title="告警阈值配置"
        description="设置各仓库区域的温度湿度告警阈值，超出阈值将触发告警"
        type="warning"
        :closable="false"
      />
      
      <div class="header-actions">
        <el-button type="primary" size="small" @click="handleAddThreshold">
          ➕ 添加阈值
        </el-button>
        <el-button @click="debugThresholds" type="warning" size="small">
          🐛 调试阈值数据
        </el-button>
      </div>
    </div>
    
    <div v-loading="loading" class="thresholds-content">
      <el-empty v-if="localThresholds.length === 0" description="暂无告警阈值配置" />
      
      <div v-else class="thresholds-list">
        <el-table :data="localThresholds" stripe style="width: 100%">
          <el-table-column prop="location" label="仓库区域" width="120" />
          <el-table-column label="温度阈值(℃)" width="150">
            <template #default="scope">
              {{ scope.row.minTemperature }} ~ {{ scope.row.maxTemperature }}
            </template>
          </el-table-column>
          <el-table-column label="湿度阈值(%)" width="150">
            <template #default="scope">
              {{ scope.row.minHumidity }} ~ {{ scope.row.maxHumidity }}
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.enabled ? 'success' : 'info'" size="small">
                {{ scope.row.enabled ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button size="small" @click="handleEdit(scope.row)">
                编辑
              </el-button>
              <el-button 
                size="small" 
                :type="scope.row.enabled ? 'warning' : 'success'"
                @click="handleToggle(scope.row)"
              >
                {{ scope.row.enabled ? '禁用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    
    <!-- 编辑阈值对话框 -->
    <el-dialog
      v-model="showEditDialog"
      :title="editDialogTitle"
      width="500px"
    >
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="仓库区域">
          <el-select v-model="editForm.location" placeholder="请选择仓库区域">
            <el-option label="A区仓库" value="A区仓库" />
            <el-option label="B区仓库" value="B区仓库" />
            <el-option label="C区仓库" value="C区仓库" />
            <el-option label="冷冻库" value="冷冻库" />
            <el-option label="常温库" value="常温库" />
            <el-option label="贵重物品区" value="贵重物品区" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="最低温度">
          <el-input-number 
            v-model="editForm.minTemperature" 
            :min="-50" 
            :max="50"
            :step="0.1"
            controls-position="right"
          />
          <span style="margin-left: 8px;">℃</span>
        </el-form-item>
        
        <el-form-item label="最高温度">
          <el-input-number 
            v-model="editForm.maxTemperature" 
            :min="-50" 
            :max="50"
            :step="0.1"
            controls-position="right"
          />
          <span style="margin-left: 8px;">℃</span>
        </el-form-item>
        
        <el-form-item label="最低湿度">
          <el-input-number 
            v-model="editForm.minHumidity" 
            :min="0" 
            :max="100"
            :step="0.1"
            controls-position="right"
          />
          <span style="margin-left: 8px;">%</span>
        </el-form-item>
        
        <el-form-item label="最高湿度">
          <el-input-number 
            v-model="editForm.maxHumidity" 
            :min="0" 
            :max="100"
            :step="0.1"
            controls-position="right"
          />
          <span style="margin-left: 8px;">%</span>
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input 
            v-model="editForm.description" 
            type="textarea" 
            :rows="2"
            placeholder="请输入阈值描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveThreshold">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  thresholds: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update', 'toggle'])

// 本地阈值数据，用于前端状态管理
const localThresholds = ref([])

// 监听props变化，更新本地数据
watch(() => props.thresholds, (newThresholds) => {
  localThresholds.value = [...newThresholds]
  console.log('📥 AlertThresholds 接收到阈值数据:', localThresholds.value)
}, { immediate: true, deep: true })

const showEditDialog = ref(false)
const isEditing = ref(false)
const editForm = ref({
  location: '',
  minTemperature: 0,
  maxTemperature: 0,
  minHumidity: 0,
  maxHumidity: 0,
  description: '',
  enabled: true
})

// 计算对话框标题
const editDialogTitle = ref('添加告警阈值')

// 调试方法
const debugThresholds = () => {
  console.log('🐛 调试阈值数据:')
  console.log('props.thresholds:', props.thresholds)
  console.log('localThresholds:', localThresholds.value)
  
  if (localThresholds.value && localThresholds.value.length > 0) {
    localThresholds.value.forEach((threshold, index) => {
      console.log(`阈值 ${index}:`, {
        id: threshold.id,
        location: threshold.location,
        enabled: threshold.enabled,
        完整对象: threshold
      })
    })
  } else {
    console.warn('⚠️ 没有阈值数据')
  }
}

// 添加阈值
const handleAddThreshold = () => {
  isEditing.value = false
  editDialogTitle.value = '添加告警阈值'
  editForm.value = {
    location: '',
    minTemperature: 18,
    maxTemperature: 28,
    minHumidity: 40,
    maxHumidity: 70,
    description: '',
    enabled: true
  }
  showEditDialog.value = true
}

// 编辑阈值
const handleEdit = (threshold) => {
  isEditing.value = true
  editDialogTitle.value = '编辑告警阈值'
  editForm.value = { ...threshold }
  showEditDialog.value = true
}

// 保存阈值
const handleSaveThreshold = () => {
  if (isEditing.value) {
    // 编辑模式 - 更新现有阈值
    const index = localThresholds.value.findIndex(t => t.id === editForm.value.id)
    if (index !== -1) {
      localThresholds.value[index] = { ...editForm.value }
      emit('update', editForm.value.id, editForm.value)
    }
  } else {
    // 添加模式 - 创建新阈值
    const newThreshold = {
      ...editForm.value,
      id: Date.now() // 临时ID
    }
    localThresholds.value.push(newThreshold)
    // 这里应该调用API保存，然后触发更新
    emit('update', newThreshold.id, newThreshold)
  }
  
  ElMessage.success('阈值保存成功')
  showEditDialog.value = false
}

// 切换阈值状态 - 修复：传递整个阈值对象
const handleToggle = (threshold) => {
  console.log('🔍 AlertThresholds - 点击切换阈值:', {
    接收到的参数: threshold,
    参数类型: typeof threshold,
    是否为对象: typeof threshold === 'object',
    是否有ID: threshold && threshold.id !== undefined,
    ID值: threshold ? threshold.id : '无'
  })
  
  // 严格的参数验证
  if (!threshold) {
    console.error('❌ 阈值对象为空')
    ElMessage.error('阈值对象不存在')
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
  
  console.log('✅ 参数验证通过，触发 toggle 事件')
  emit('toggle', threshold)
}
</script>

<style scoped>
.thresholds-header {
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

.thresholds-content {
  min-height: 200px;
}
</style>