import request from '@/utils/request'
import { useAuthStore } from '@/stores/auth'

// 命名导出所有函数
export const login = (loginData) => {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data: loginData
  })
}

export const validateToken = (token) => {
  return request({
    url: '/api/auth/validate',
    method: 'post',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export const getCurrentUser = (token) => {
  return request({
    url: '/api/auth/me',
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export const refreshToken = (token) => {
  return request({
    url: '/api/auth/refresh',
    method: 'post',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export const logoutApi = (token) => {
  return request({
    url: '/api/auth/logout',
    method: 'post',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export const getRoles = (token) => {
  return request({
    url: '/api/user/roles',
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export const initializeAuth = async () => {
  const authStore = useAuthStore()
  const token = authStore.token
  
  if (!token) {
    return { success: false, message: '未找到认证令牌' }
  }
  
  try {
    // 验证令牌有效性
    const validateResponse = await validateToken(token)
    
    if (validateResponse.data?.valid) {
      // 获取用户信息
      const userResponse = await getCurrentUser(token)
      authStore.setUser(userResponse.data)
      
      return { 
        success: true, 
        user: userResponse.data,
        message: '认证状态恢复成功'
      }
    } else {
      authStore.clearAuth()
      return { success: false, message: '令牌已失效' }
    }
  } catch (error) {
    authStore.clearAuth()
    return { 
      success: false, 
      message: `认证初始化失败: ${error.message}` 
    }
  }
}

// 默认导出所有方法
export default {
  login,
  validateToken,
  getCurrentUser,
  refreshToken,
  logout: logoutApi,
  getRoles,
  initializeAuth
}