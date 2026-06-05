import { http } from '@/utils/request'

export const systemApi = {
  // ========== 系统参数相关 ==========
  
  getSystemParams: () => http.get('/api/system/params'),
  
  updateSystemParams: (params) => http.post('/api/system/params', params),
  
  getSystemParamByKey: (key, defaultValue = null) => {
    const params = defaultValue ? { defaultValue } : {}
    return http.get(`/api/system/params/${key}`, { params })
  },
  
  // ========== 告警阈值相关 ==========
  
  getAlertThresholds: () => http.get('/api/system/thresholds'),
  
  getEnabledThresholds: () => http.get('/api/system/thresholds/enabled'),
  
  updateThresholds: (thresholds) => http.post('/api/system/thresholds', thresholds),
  
  updateThreshold: (id, threshold) => http.put(`/api/system/thresholds/${id}`, threshold),
  
  // 修复：使用查询参数传递 enabled
  toggleThreshold: (id, enabled) => http.post(`/api/system/thresholds/${id}/toggle?enabled=${enabled}`),
  
  // ========== 系统状态相关 ==========
  
  getSystemHealth: () => http.get('/api/system/health'),
  
  validateData: (data) => http.post('/api/system/validate', data),
  
  resetToDefault: () => http.post('/api/system/reset'),
  
  getSystemStats: () => http.get('/api/system/stats')
}