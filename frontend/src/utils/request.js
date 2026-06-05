import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8085',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 显示加载状态（可选）
    if (config.showLoading !== false) {
      // 可以在这里添加全局loading
    }

    // 添加认证token - 直接从localStorage读取
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log(`🔐 携带Token请求: ${config.method?.toUpperCase()} ${config.url}`)
    } else {
      console.log(`⚠️ 无Token请求: ${config.method?.toUpperCase()} ${config.url}`)
    }

    // 记录请求日志（开发环境）
    if (import.meta.env.DEV) {
      console.log(`🚀 [API Request] ${config.method?.toUpperCase()} ${config.url}`, config.data || config.params)
    }

    return config
  },
  (error) => {
    console.error('❌ [Request Error]', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 隐藏加载状态
    if (response.config.showLoading !== false) {
      // 隐藏全局loading
    }

    // 记录响应日志（开发环境）
    if (import.meta.env.DEV) {
      console.log(`✅ [API Response] ${response.config.method?.toUpperCase()} ${response.config.url}`, response.data)
    }

    // 直接返回响应数据
    return response.data
  },
  (error) => {
    // 隐藏加载状态
    if (error.config?.showLoading !== false) {
      // 隐藏全局loading
    }

    console.error('❌ [Response Error]', error)

    // 处理HTTP错误状态码
    if (error.response) {
      const { status, data } = error.response
      const message = data?.message || error.message

      switch (status) {
        case 400:
          ElMessage.error(`请求错误: ${message}`)
          break
        case 401:
          // Token过期或无效，清除本地存储
          localStorage.removeItem('auth_token')
          localStorage.removeItem('auth_user')
          ElMessage.error('登录已过期或未登录，请重新登录')
          // 延迟跳转以避免路由守卫问题
          setTimeout(() => {
            if (window.location.pathname !== '/login') {
              window.location.href = '/login'
            }
          }, 1500)
          break
        case 403:
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          // 修复：将 config 改为 error.config（核心错误点1）
          if (!error.config?.silent) {
            console.warn(`⚠️ 接口不存在: ${error.config.url}`)
          }
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        case 502:
          ElMessage.error('网关错误')
          break
        case 503:
          ElMessage.error('服务不可用')
          break
        default:
          // 修复：将 config 改为 error.config（核心错误点2）
          if (!error.config?.silent) {
            ElMessage.error(`网络错误: ${message}`)
          }
      }
    } else if (error.request) {
      // 请求未收到响应
      if (error.code === 'ECONNABORTED') {
        ElMessage.error('请求超时，请检查网络连接')
      } else {
        ElMessage.error('网络连接异常，请检查网络设置')
      }
    } else {
      // 其他错误
      ElMessage.error(`请求错误: ${error.message}`)
    }

    return Promise.reject(error)
  }
)

// 通用请求方法
export const http = {
  /**
   * GET 请求
   * @param {string} url 请求地址
   * @param {Object} params 查询参数
   * @param {Object} config 请求配置
   */
  get(url, params = {}, config = {}) {
    return request({
      url,
      method: 'GET',
      params,
      ...config
    })
  },

  /**
   * POST 请求
   * @param {string} url 请求地址
   * @param {Object} data 请求体数据
   * @param {Object} config 请求配置
   */
  post(url, data = {}, config = {}) {
    return request({
      url,
      method: 'POST',
      data,
      ...config
    })
  },

  /**
   * PUT 请求
   * @param {string} url 请求地址
   * @param {Object} data 请求体数据
   * @param {Object} config 请求配置
   */
  put(url, data = {}, config = {}) {
    return request({
      url,
      method: 'PUT',
      data,
      ...config
    })
  },

  /**
   * DELETE 请求
   * @param {string} url 请求地址
   * @param {Object} config 请求配置
   */
  delete(url, config = {}) {
    return request({
      url,
      method: 'DELETE',
      ...config
    })
  },

  /**
   * PATCH 请求
   * @param {string} url 请求地址
   * @param {Object} data 请求体数据
   * @param {Object} config 请求配置
   */
  patch(url, data = {}, config = {}) {
    return request({
      url,
      method: 'PATCH',
      data,
      ...config
    })
  }
}

// 直接导出常用方法（为了兼容性）
export const get = (url, params = {}, config = {}) => {
  return http.get(url, params, config)
}

export const post = (url, data = {}, config = {}) => {
  return http.post(url, data, config)
}

export const put = (url, data = {}, config = {}) => {
  return http.put(url, data, config)
}

export const del = (url, config = {}) => {
  return http.delete(url, config)
}

// 导出默认实例
export default request