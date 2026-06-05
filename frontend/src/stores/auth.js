import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { logoutApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref(localStorage.getItem('auth_token') || '')
  const user = ref(JSON.parse(localStorage.getItem('auth_user') || 'null'))
  const isAuthenticated = computed(() => !!token.value)
  
  // 计算属性：用户角色和权限
  const userRole = computed(() => user.value?.role || '')
  const userName = computed(() => user.value?.fullName || user.value?.username || '')
  
  // 权限检查方法
  const hasRole = (roles) => {
    if (!user.value || !user.value.role) return false
    if (Array.isArray(roles)) {
      return roles.includes(user.value.role)
    }
    return user.value.role === roles
  }
  
  // 权限层级检查
  const hasPermission = (requiredRole) => {
    const userRole = user.value?.role
    if (!userRole) return false
    
    // 角色权限层级：SUPER_ADMIN > ADMIN > OPERATOR > VIEWER
    const roleHierarchy = {
      'SUPER_ADMIN': 4,
      'ADMIN': 3,
      'OPERATOR': 2,
      'VIEWER': 1
    }
    
    const userLevel = roleHierarchy[userRole] || 0
    const requiredLevel = roleHierarchy[requiredRole] || 0
    
    return userLevel >= requiredLevel
  }
  
  // 检查是否管理员
  const isAdmin = computed(() => hasRole(['ADMIN', 'SUPER_ADMIN']))
  
  // 检查是否操作员以上权限
  const isOperator = computed(() => hasPermission('OPERATOR'))
  
  // 检查是否查看员以上权限
  const isViewer = computed(() => hasPermission('VIEWER'))

  // Actions
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('auth_token', newToken)
  }

  const setUser = (userData) => {
    user.value = userData
    localStorage.setItem('auth_user', JSON.stringify(userData))
  }

  const clearAuth = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
    localStorage.removeItem('remembered_username')
    localStorage.removeItem('remembered_password')
  }

  const logout = async () => {
    try {
      // 调用后端注销接口
      if (token.value) {
        await logoutApi(token.value)
      }
    } catch (error) {
      console.error('注销接口调用失败:', error)
    } finally {
      clearAuth()
    }
  }

  // 初始化认证状态
  const initialize = () => {
    const savedToken = localStorage.getItem('auth_token')
    const savedUser = localStorage.getItem('auth_user')
    
    if (savedToken) {
      token.value = savedToken
    }
    
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch (e) {
        console.error('解析用户信息失败:', e)
        localStorage.removeItem('auth_user')
      }
    }
  }

  // 获取认证头
  const getAuthHeader = () => {
    return token.value ? { 'Authorization': `Bearer ${token.value}` } : {}
  }

  return {
    token,
    user,
    isAuthenticated,
    userRole,
    userName,
    hasRole,
    hasPermission,
    isAdmin,
    isOperator,
    isViewer,
    setToken,
    setUser,
    clearAuth,
    logout,
    initialize,
    getAuthHeader
  }
})