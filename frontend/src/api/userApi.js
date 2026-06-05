import { http } from '@/utils/request'

// 分页查询用户（调用后端分页接口 /api/users/list）
export const getUserList = (params) => {
  return http.get('/api/users/list', params)
}

// 获取用户统计
export const getUserStats = () => {
  return http.get('/api/users/stats')
}

// 创建用户
export const createUser = (data) => {
  return http.post('/api/users', data)
}

// 更新用户
export const updateUser = (id, data) => {
  return http.put(`/api/users/${id}`, data)
}

// 删除用户
export const deleteUser = (id) => {
  return http.delete(`/api/users/${id}`)
}

// 批量删除用户
export const batchDeleteUsers = (ids) => {
  return http.post('/api/users/batch-delete', ids)
}

// 更新用户状态
export const updateUserStatus = (id, data) => {
  return http.patch(`/api/users/${id}/status`, data)
}

// 批量更新用户状态
export const batchUpdateUserStatus = (ids, enabled) => {
  return http.post('/api/users/batch-status', { ids, enabled })
}

// 修改密码
export const changePassword = (id, data) => {
  return http.post(`/api/users/${id}/change-password`, data)
}

// 重置密码为默认值
export const resetPassword = (id) => {
  return http.post(`/api/users/${id}/reset-password`)
}

export default {
  getUserList,
  getUserStats,
  createUser,
  updateUser,
  deleteUser,
  batchDeleteUsers,
  updateUserStatus,
  batchUpdateUserStatus,
  changePassword,
  resetPassword
}