// 全局变量存储从后端获取的告警阈值配置
let globalThresholds = []

// 默认仓库区域配置（作为fallback）
const defaultWarehouseRanges = {
  'A区仓库': {
    temperature: { min: 18, max: 26 },
    humidity: { min: 40, max: 60 },
    pm25: { min: 0, max: 35 },
    name: 'A区仓库',
    description: '人体最适宜环境'
  },
  'B区仓库': {
    temperature: { min: 18, max: 26 },
    humidity: { min: 40, max: 60 },
    pm25: { min: 0, max: 35 },
    name: 'B区仓库',
    description: '人体最适宜环境'
  },
  'C区仓库': {
    temperature: { min: 18, max: 26 },
    humidity: { min: 40, max: 60 },
    pm25: { min: 0, max: 35 },
    name: 'C区仓库',
    description: '人体最适宜环境'
  },
  '贵重物品区': {
    temperature: { min: 16, max: 20 },
    humidity: { min: 30, max: 45 },
    pm25: { min: 0, max: 25 },
    name: '贵重物品区',
    description: '低温干燥环境'
  },
  '冷冻库': {
    temperature: { min: -25, max: -15 },
    humidity: { min: 20, max: 40 },
    pm25: { min: 0, max: 30 },
    name: '冷冻库',
    description: '冷冻干燥环境'
  },
  '常温库': {
    temperature: { min: 15, max: 25 },
    humidity: { min: 30, max: 50 },
    pm25: { min: 0, max: 35 },
    name: '常温库',
    description: '常温干燥环境'
  }
}

// 更新全局阈值配置
export const updateThresholds = (thresholds) => {
  globalThresholds = thresholds
  console.log('✅ 告警阈值已更新:', globalThresholds)
}

// 获取指定位置的阈值配置
const getThresholdForLocation = (location) => {
  // 首先从全局阈值中查找
  const threshold = globalThresholds.find(t => t.location === location && t.enabled)
  if (threshold) {
    return {
      temperature: { min: threshold.minTemperature, max: threshold.maxTemperature },
      humidity: { min: threshold.minHumidity, max: threshold.maxHumidity },
      pm25: { min: 0, max: 35 }, // 默认PM2.5阈值
      name: location,
      description: threshold.description || '仓库区域'
    }
  }
  // 如果没有找到，使用默认配置
  return defaultWarehouseRanges[location]
}

// 检查数据是否异常（包含温湿度、PM2.5、烟雾）
export const checkDataAbnormal = (data) => {
  const { temperature, humidity, pm25, smoke, location } = data
  const range = getThresholdForLocation(location)

  if (!range) {
    return {
      isAbnormal: true,
      level: 'warning',
      messages: [`未知区域: ${location}`],
      range: null
    }
  }

  const abnormalities = []
  let level = 'normal'

  // 检查温度
  if (temperature < range.temperature.min) {
    abnormalities.push(`温度过低: ${temperature}℃ < ${range.temperature.min}℃`)
    level = 'danger'
  } else if (temperature > range.temperature.max) {
    abnormalities.push(`温度过高: ${temperature}℃ > ${range.temperature.max}℃`)
    level = 'danger'
  }

  // 检查湿度
  if (humidity < range.humidity.min) {
    abnormalities.push(`湿度过低: ${humidity}% < ${range.humidity.min}%`)
    level = abnormalities.length > 0 ? level : 'warning'
  } else if (humidity > range.humidity.max) {
    abnormalities.push(`湿度过高: ${humidity}% > ${range.humidity.max}%`)
    level = abnormalities.length > 0 ? level : 'warning'
  }

  // 检查 PM2.5（如果存在 pm25 字段）
  if (pm25 !== undefined && range.pm25) {
    if (pm25 < range.pm25.min) {
      abnormalities.push(`PM2.5 过低: ${pm25} < ${range.pm25.min} μg/m³`)
      level = abnormalities.length > 0 ? level : 'warning'  // 过低视为警告
    } else if (pm25 > range.pm25.max) {
      abnormalities.push(`PM2.5 过高: ${pm25} > ${range.pm25.max} μg/m³`)
      level = 'danger'  // 过高视为危险
    }
  }

  // 检查烟雾（如果存在 smoke 字段）
  if (smoke !== undefined) {
    const SMOKE_THRESHOLD = 800 // 烟雾浓度阈值：800 ppm
    if (smoke >= SMOKE_THRESHOLD) {
      abnormalities.push(`烟雾浓度过高: ${smoke} ppm >= ${SMOKE_THRESHOLD} ppm`)
      level = 'danger'
    }
  }

  return {
    isAbnormal: abnormalities.length > 0,
    level,
    messages: abnormalities,
    range
  }
}

// 获取告警级别样式
export const getAlertLevelStyle = (level) => {
  switch (level) {
    case 'danger':
      return { type: 'danger', icon: '🚨', color: '#f56c6c', bgColor: '#fef0f0' }
    case 'warning':
      return { type: 'warning', icon: '🔔', color: '#e6a23c', bgColor: '#fdf6ec' }
    default:
      return { type: 'success', icon: '✅', color: '#67c23a', bgColor: '#f0f9eb' }
  }
}

// 生成告警消息（包含 PM2.5 和烟雾信息）
export const generateAlertMessage = (data, checkResult) => {
  if (!checkResult.isAbnormal) {
    return null
  }

  return {
    id: Date.now() + Math.random(),
    timestamp: new Date(),
    location: data.location,
    temperature: data.temperature,
    humidity: data.humidity,
    pm25: data.pm25,          // 新增
    smoke: data.smoke,         // 新增
    level: checkResult.level,
    messages: checkResult.messages,
    range: checkResult.range,
    acknowledged: false
  }
}