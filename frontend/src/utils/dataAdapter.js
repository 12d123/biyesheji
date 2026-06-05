// src/utils/dataAdapter.js
// 数据适配器 - 将后端API数据格式转换为前端需要的格式

// 适配传感器数据
export const adaptSensorData = (apiData) => {
  if (!apiData) return []
  
  // 如果是数组格式的数据
  if (Array.isArray(apiData)) {
    return apiData.map(item => ({
      id: item.id,
      temperature: item.temperature,
      humidity: item.humidity,
      location: item.location,
      sensorType: item.sensorType || 'DHT11',
      time: item.createdTime || item.timestamp,
      createdTime: item.createdTime
    }))
  }
  
  // 如果是ApiResponse格式，包含data字段
  if (apiData.data && Array.isArray(apiData.data)) {
    return apiData.data.map(item => ({
      id: item.id,
      temperature: item.temperature,
      humidity: item.humidity,
      location: item.location,
      sensorType: item.sensorType || 'DHT11',
      time: item.createdTime || item.timestamp,
      createdTime: item.createdTime
    }))
  }
  
  return []
}

// 适配当前数据
export const adaptCurrentData = (apiData) => {
  if (!apiData) return null
  
  if (apiData.data) {
    return {
      temperature: apiData.data.temperature,
      humidity: apiData.data.humidity,
      location: apiData.data.location,
      sensorType: apiData.data.sensorType,
      timestamp: apiData.data.timestamp || apiData.data.createdTime
    }
  }
  
  return apiData
}

// ========== 系统设置数据适配器 ==========

/**
 * 系统参数数据适配
 * 后端返回: ApiResponse<Map<String, Object>>
 */
export const adaptSystemParams = (backendData) => {
  console.log('🔄 适配系统参数数据:', backendData)
  
  if (!backendData) return {}
  
  // 提取数据
  const data = backendData.success !== undefined && backendData.data !== undefined 
    ? backendData.data 
    : (typeof backendData === 'object' && !Array.isArray(backendData) ? backendData : {})
  
  // 转换数值类型参数
  const convertedData = {}
  Object.entries(data).forEach(([key, value]) => {
    if (value !== null && value !== undefined) {
      // 尝试将数值字符串转换为数字
      if (!isNaN(value) && value !== '') {
        convertedData[key] = Number(value)
      } 
      // 处理布尔值字符串
      else if (value === 'true') {
        convertedData[key] = true
      } else if (value === 'false') {
        convertedData[key] = false
      } else {
        convertedData[key] = value
      }
    } else {
      convertedData[key] = value
    }
  })
  
  return convertedData
}

/**
 * 告警阈值数据适配
 * 后端返回: ApiResponse<List<AlertThreshold>>
 */
export const adaptAlertThresholds = (backendData) => {
  console.log('🔄 适配告警阈值数据 - 原始数据:', backendData)
  
  if (!backendData) {
    console.warn('⚠️ 后端返回数据为空')
    return []
  }
  
  // 如果后端返回的是ApiResponse格式
  if (backendData.success !== undefined && backendData.data !== undefined) {
    console.log('📦 处理ApiResponse格式数据:', backendData.data)
    
    if (Array.isArray(backendData.data)) {
      const adaptedData = backendData.data.map((threshold, index) => {
        // 确保阈值数值类型正确
        const adapted = {
          id: threshold.id,
          location: threshold.location,
          minTemperature: threshold.minTemperature !== undefined ? Number(threshold.minTemperature) : null,
          maxTemperature: threshold.maxTemperature !== undefined ? Number(threshold.maxTemperature) : null,
          minHumidity: threshold.minHumidity !== undefined ? Number(threshold.minHumidity) : null,
          maxHumidity: threshold.maxHumidity !== undefined ? Number(threshold.maxHumidity) : null,
          description: threshold.description,
          enabled: threshold.enabled !== undefined ? 
            (typeof threshold.enabled === 'string' ? threshold.enabled === 'true' : threshold.enabled) : 
            true,
          createdTime: threshold.createdTime,
          updatedTime: threshold.updatedTime
        }
        
        console.log(`🔢 转换阈值 ${index}:`, {
          原始ID: threshold.id,
          转换后ID: adapted.id,
          是否有ID: !!adapted.id,
          完整对象: adapted
        })
        
        return adapted
      })
      
      console.log('✅ 阈值数据转换完成:', adaptedData)
      return adaptedData
    }
    
    console.warn('⚠️ 后端返回的data不是数组:', backendData.data)
    return []
  }
  
  // 如果直接返回数组
  if (Array.isArray(backendData)) {
    console.log('📦 处理直接数组格式数据')
    return backendData.map(threshold => ({
      id: threshold.id,
      location: threshold.location,
      minTemperature: threshold.minTemperature !== undefined ? Number(threshold.minTemperature) : null,
      maxTemperature: threshold.maxTemperature !== undefined ? Number(threshold.maxTemperature) : null,
      minHumidity: threshold.minHumidity !== undefined ? Number(threshold.minHumidity) : null,
      maxHumidity: threshold.maxHumidity !== undefined ? Number(threshold.maxHumidity) : null,
      description: threshold.description,
      enabled: threshold.enabled !== undefined ? 
        (typeof threshold.enabled === 'string' ? threshold.enabled === 'true' : threshold.enabled) : 
        true,
      createdTime: threshold.createdTime,
      updatedTime: threshold.updatedTime
    }))
  }
  
  console.warn('⚠️ 无法识别的数据格式:', backendData)
  return []
}

/**
 * 系统健康状态适配
 * 后端返回: ApiResponse<Map<String, Object>>
 */
export const adaptSystemHealth = (backendData) => {
  console.log('🔄 适配系统健康数据:', backendData)
  
  if (!backendData) return { status: 'UNKNOWN' }
  
  // 如果后端返回的是ApiResponse格式
  if (backendData.success !== undefined && backendData.data !== undefined) {
    return backendData.data
  }
  
  // 如果直接返回数据对象
  if (typeof backendData === 'object' && !Array.isArray(backendData)) {
    return backendData
  }
  
  return { status: 'UNKNOWN' }
}

/**
 * 系统统计数据适配
 * 后端返回: ApiResponse<Map<String, Object>>
 */
export const adaptSystemStats = (backendData) => {
  console.log('🔄 适配系统统计数据:', backendData)
  
  if (!backendData) return {}
  
  // 如果后端返回的是ApiResponse格式
  if (backendData.success !== undefined && backendData.data !== undefined) {
    return backendData.data
  }
  
  // 如果直接返回数据对象
  if (typeof backendData === 'object' && !Array.isArray(backendData)) {
    return backendData
  }
  
  return {}
}

/**
 * API响应通用适配
 */
export const adaptApiResponse = (backendData) => {
  if (!backendData) {
    return { success: false, message: '无响应数据' }
  }
  
  // 如果已经是标准格式，直接返回
  if (backendData.success !== undefined) {
    return backendData
  }
  
  // 否则包装为标准格式
  return {
    success: true,
    message: '操作成功',
    data: backendData,
    timestamp: new Date().toISOString()
  }
}

// ========== 原有传感器数据适配器 ==========

// 计算仓库统计 - 修复版本
export const calculateWarehouseStats = (data) => {
  if (!data || !Array.isArray(data) || data.length === 0) {
    return []
  }
  
  const stats = {}
  
  // 定义仓库名称映射
  const warehouseNameMapping = {
    'A区仓库': 'A区仓库',
    'B区仓库': 'B区仓库',
    'C区仓库': 'C区仓库',
    '冷冻库': 'D区仓库',
    '常温库': 'E区仓库', 
    '贵重物品区': 'F区仓库'
  }
  
  data.forEach(item => {
    if (!item.location) return
    
    // 使用映射后的仓库名称
    const warehouseName = warehouseNameMapping[item.location] || item.location
    
    if (!stats[warehouseName]) {
      stats[warehouseName] = {
        name: warehouseName,  // 这里改为 name 属性
        temperature: 0,
        humidity: 0,
        count: 0
      }
    }
    
    const stat = stats[warehouseName]
    stat.temperature += parseFloat(item.temperature) || 0
    stat.humidity += parseFloat(item.humidity) || 0
    stat.count += 1
  })
  
  // 计算平均值
  Object.keys(stats).forEach(warehouseName => {
    const stat = stats[warehouseName]
    if (stat.count > 0) {
      stat.temperature = parseFloat((stat.temperature / stat.count).toFixed(1))
      stat.humidity = parseFloat((stat.humidity / stat.count).toFixed(1))
    }
  })
  
  // 返回数组并按名称排序
  return Object.values(stats).sort((a, b) => a.name.localeCompare(b.name))
}

// 过滤温度数据用于图表
export const filterTemperatureData = (data) => {
  if (!data || !Array.isArray(data)) return []
  
  return data.slice(0, 20).map(item => ({
    time: formatTime(item.time || item.createdTime),
    temperature: item.temperature,
    location: item.location
  })).reverse() // 反转以便时间顺序正确
}

// 格式化时间
export const formatTime = (timestamp) => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 获取默认数据（空数据）
export const getDefaultData = () => {
  return []
}