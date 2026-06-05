// 数据计算工具函数

// 计算仓库统计数据
export function calculateWarehouseStats(historyData) {
  const stats = {}
  
  historyData.forEach(item => {
    const location = item.location
    if (!stats[location]) {
      stats[location] = { temperatureSum: 0, humiditySum: 0, count: 0 }
    }
    stats[location].temperatureSum += item.temperature
    stats[location].humiditySum += item.humidity
    stats[location].count++
  })
  
  return Object.keys(stats).map(location => ({
    name: location,
    temperature: parseFloat((stats[location].temperatureSum / stats[location].count).toFixed(1)),
    humidity: parseFloat((stats[location].humiditySum / stats[location].count).toFixed(1))
  }))
}

// 过滤温度数据
export function filterTemperatureData(historyData) {
  const aZoneData = historyData
    .filter(item => item.location === 'A区仓库')
    .slice(0, 7)
    .map(item => ({
      time: item.time.split(' ')[1]?.substring(0, 5) || '00:00',
      temperature: item.temperature
    }))
    .reverse()
  
  return aZoneData.length > 0 ? aZoneData : [
    { time: '10:00', temperature: 25 },
    { time: '11:00', temperature: 26 },
    { time: '12:00', temperature: 24 }
  ]
}

// 获取当前数据（最新一条）
export function getCurrentData(historyData) {
  if (historyData.length === 0) {
    return { temperature: 0, humidity: 0, location: '暂无数据' }
  }
  const latest = historyData[0]
  return {
    temperature: latest.temperature,
    humidity: latest.humidity,
    location: latest.location
  }
}

// 计算统计信息
export function calculateStats(historyData) {
  const today = new Date().toDateString()
  const todayCount = historyData.filter(item => {
    const itemDate = new Date(item.time).toDateString()
    return itemDate === today
  }).length
  
  return {
    total: historyData.length,
    today: todayCount
  }
}