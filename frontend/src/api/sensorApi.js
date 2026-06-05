import { http } from '@/utils/request'

// API 方法
const sensorApi = {
  // 健康检查 - 使用正确的API路径
  async healthCheck() {
    try {
      const response = await http.get('/api/test/hello')
      return {
        status: 'connected',
        message: '后端服务连接正常',
        data: response
      }
    } catch (error) {
      // 如果/test/hello不存在，尝试其他健康检查端点
      try {
        // 尝试获取传感器数据作为健康检查
        await http.get('/api/sensor/current', { silent: true })
        return {
          status: 'connected',
          message: '后端服务连接正常',
          data: null
        }
      } catch (fallbackError) {
        return {
          status: 'error',
          message: '后端服务连接失败: ' + (error.message || fallbackError.message)
        }
      }
    }
  },

  // 获取当前传感器数据
  async getCurrentData() {
    try {
      const response = await http.get('/api/sensor/current')
      return response
    } catch (error) {
      throw new Error(`获取当前数据失败: ${error.message}`)
    }
  },

  // 获取历史数据
  async getHistoryData() {
    try {
      const response = await http.get('/api/sensor/history')
      return response
    } catch (error) {
      throw new Error(`获取历史数据失败: ${error.message}`)
    }
  },

  // 获取今日数据
  async getTodayData() {
    try {
      const response = await http.get('/api/sensor/today')
      return response
    } catch (error) {
      throw new Error(`获取今日数据失败: ${error.message}`)
    }
  },

  // 获取统计信息
  async getStats() {
    try {
      const response = await http.get('/api/sensor/stats')
      return response
    } catch (error) {
      throw new Error(`获取统计信息失败: ${error.message}`)
    }
  },

  // 获取所有数据
  async getAllData() {
    try {
      console.log('🔍 开始获取所有传感器数据...')
      const response = await http.get('/api/sensor/all')
      console.log('📦 原始API响应:', response)
      
      // 处理API响应格式
      let dataArray = []
      
      if (response && response.data && Array.isArray(response.data)) {
        // 格式: {data: Array, count: number, message: string, status: string}
        console.log('✅ 数据格式: 标准API响应，数据长度:', response.data.length)
        dataArray = response.data
      } else if (Array.isArray(response)) {
        // 格式: 直接返回数组
        console.log('✅ 数据格式: 直接数组，长度:', response.length)
        dataArray = response
      } else if (response && response.data) {
        // 格式: {data: object} - 单个对象包装成数组
        console.log('✅ 数据格式: 单个对象，包装成数组')
        dataArray = [response.data]
      } else {
        console.log('⚠️ 数据格式: 未知格式，返回空数组')
        dataArray = []
      }
      
      console.log('📊 提取后的数据:', dataArray)
      console.log('📊 数据条数:', dataArray.length)
      return dataArray
      
    } catch (error) {
      console.error('❌ 获取所有数据失败:', error)
      throw new Error(`获取所有数据失败: ${error.message}`)
    }
  },

  // 获取异常数据
  async getAbnormalData() {
    try {
      const response = await http.get('/api/sensor/abnormal')
      return response
    } catch (error) {
      throw new Error(`获取异常数据失败: ${error.message}`)
    }
  },

  // 手动添加传感器数据（核心修复：字段名映射）
  async addSensorData(sensorData) {
    try {
      // 修复字段名不匹配问题：将warehouseLocation转换为location
      const submitData = {
        // 优先使用location字段，如果传入的是warehouseLocation则兼容
        location: sensorData.location || sensorData.warehouseLocation,
        temperature: sensorData.temperature,
        humidity: sensorData.humidity,
        pm25: sensorData.pm25,
        smoke: sensorData.smoke,
        sensorType: sensorData.sensorType
      }
      
      console.log('📤 提交给后端的标准化数据:', submitData) // 调试日志
      const response = await http.post('/api/sensor/data', submitData)
      return response
    } catch (error) {
      console.error('❌ 添加传感器数据失败:', error)
      throw new Error(`添加传感器数据失败: ${error.message}`)
    }
  },

  // 生成模拟数据
  async generateMockData() {
    try {
      const response = await http.post('/api/sensor/mock')
      return response
    } catch (error) {
      throw new Error(`生成模拟数据失败: ${error.message}`)
    }
  },

  // 批量生成模拟数据
  async generateBatchMockData(count = 5) {
    try {
      const response = await http.post(`/api/sensor/mock/batch?count=${count}`)
      return response
    } catch (error) {
      throw new Error(`批量生成模拟数据失败: ${error.message}`)
    }
  },

  // 清空数据（谨慎使用）
  async clearAllData() {
    try {
      const response = await http.delete('/api/sensor/clear')
      return response
    } catch (error) {
      throw new Error(`清空数据失败: ${error.message}`)
    }
  },

  // 备选清空方法（POST方式）
  async clearAllDataPost() {
    try {
      const response = await http.post('/api/sensor/clear')
      return response
    } catch (error) {
      throw new Error(`清空数据失败: ${error.message}`)
    }
  },

  // 删除单条数据
  async deleteData(id) {
    try {
      const response = await http.delete(`/api/sensor/data/${id}`)
      return response
    } catch (error) {
      throw new Error(`删除数据失败: ${error.message}`)
    }
  },

  // 批量删除数据
  async deleteBatchData(ids) {
    try {
      const response = await http.post('/api/sensor/delete-batch', { ids })
      return response
    } catch (error) {
      throw new Error(`批量删除数据失败: ${error.message}`)
    }
  },

  // ========== 新增风扇控制接口 ==========

  // 设置风扇状态（true=开启，false=关闭）
  async setFanState(state) {
    try {
      const response = await http.post('/api/fan/control', { state })
      return response
    } catch (error) {
      throw new Error(`设置风扇状态失败: ${error.message}`)
    }
  },

  // 设置风扇模式（auto=自动，manual=手动）
  async setFanMode(modeData) {
    try {
      const response = await http.post('/api/fan/mode', modeData)
      return response
    } catch (error) {
      throw new Error(`设置风扇模式失败: ${error.message}`)
    }
  }
}

export default sensorApi