<template>
  <div class="user-management-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <div class="header-left">
        <h1>👥 用户管理</h1>
        <p>管理系统用户和权限分配</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加用户
        </el-button>
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon total">👥</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total || 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon admin">👑</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.admin || 0 }}</div>
              <div class="stat-label">管理员</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon operator">🛠️</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.operator || 0 }}</div>
              <div class="stat-label">操作员</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon viewer">👁️</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.viewer || 0 }}</div>
              <div class="stat-label">查看员</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索和筛选区域 -->
    <el-card class="search-section">
      <div class="search-content">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户名、姓名、邮箱"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select 
              v-model="filterRole" 
              placeholder="全部角色"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部角色" value="" />
              <el-option label="查看员" value="VIEWER" />
              <el-option label="操作员" value="OPERATOR" />
              <el-option label="管理员" value="ADMIN" />
              <el-option label="超级管理员" value="SUPER_ADMIN" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select 
              v-model="filterStatus" 
              placeholder="全部状态"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部状态" value="" />
              <el-option label="已启用" value="true" />
              <el-option label="已禁用" value="false" />
            </el-select>
          </el-col>
          <el-col :span="10" class="search-actions">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
            <el-tag type="info">共 {{ totalUsers }} 个用户</el-tag>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 用户表格 -->
    <el-card class="table-section">
      <template #header>
        <div class="table-header">
          <span>用户列表</span>
          <div class="batch-actions">
            <el-button 
              size="small" 
              :disabled="selectedUsers.length === 0"
              @click="handleBatchEnable"
            >
              <el-icon><Check /></el-icon>
              批量启用
            </el-button>
            <el-button 
              size="small" 
              :disabled="selectedUsers.length === 0"
              @click="handleBatchDisable"
            >
              <el-icon><Close /></el-icon>
              批量禁用
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              :disabled="selectedUsers.length === 0"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="username" label="用户名" width="150">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :icon="UserFilled" />
              <div class="user-info">
                <div class="username">{{ row.username }}</div>
                <div class="email" v-if="row.email">{{ row.email }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="fullName" label="姓名" width="120" />
        
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)" size="small">
              {{ getRoleDisplayName(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              :loading="row.statusLoading"
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="(value) => handleStatusChange(row.id, value)"
            />
            <span class="status-text" :class="row.enabled ? 'enabled' : 'disabled'">
              {{ row.enabled ? '已启用' : '已禁用' }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="lastLoginTime" label="最后登录" width="180">
          <template #default="{ row }">
            {{ row.lastLoginTime ? formatDate(row.lastLoginTime) : '从未登录' }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button 
                type="primary" 
                size="small" 
                @click="handleEdit(row)"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              
              <el-button 
                type="warning" 
                size="small" 
                @click="handleResetPassword(row)"
                v-if="!(authStore.userRole !== 'SUPER_ADMIN' && row.role === 'SUPER_ADMIN')"
              >
                <el-icon><Key /></el-icon>
                重置密码
              </el-button>
              
              <el-button 
                type="danger" 
                size="small" 
                @click="handleDelete(row)"
                :disabled="row.username === 'admin'"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalUsers"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="showUserDialog"
      :title="dialogTitle"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="userForm.username"
                placeholder="请输入用户名"
                :disabled="isEditMode"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="fullName">
              <el-input
                v-model="userForm.fullName"
                placeholder="请输入真实姓名"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="userForm.email"
                placeholder="请输入邮箱"
                type="email"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="userForm.phone"
                placeholder="请输入手机号"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select
                v-model="userForm.role"
                placeholder="请选择角色"
                style="width: 100%"
                :disabled="authStore.userRole !== 'SUPER_ADMIN' && userForm.role === 'SUPER_ADMIN'"
              >
                <el-option label="查看员" value="VIEWER" />
                <el-option label="操作员" value="OPERATOR" />
                <el-option label="管理员" value="ADMIN" />
                <el-option 
                  label="超级管理员" 
                  value="SUPER_ADMIN"
                  :disabled="authStore.userRole !== 'SUPER_ADMIN'"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账户状态" prop="enabled">
              <el-switch
                v-model="userForm.enabled"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="密码" prop="password" v-if="!isEditMode">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword" v-if="!isEditMode">
          <el-input
            v-model="userForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="showResetPasswordDialog"
      title="重置密码"
      width="500px"
    >
      <div v-if="!customResetPassword">
        <p class="reset-tip">确认将用户密码重置为默认密码 <span class="default-pwd">123456</span> 吗？</p>
      </div>
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordFormRules"
        label-width="100px"
        v-if="customResetPassword"
      >
        <el-form-item label="新密码" prop="password">
          <el-input
            v-model="passwordForm.password"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showResetPasswordDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="customResetPassword ? handlePasswordSubmit : handleDefaultResetPassword"
            :loading="passwordSubmitting"
          >
            确定
          </el-button>
          <el-button 
            type="text" 
            @click="customResetPassword = !customResetPassword"
            v-if="!customResetPassword"
          >
            自定义密码
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Refresh, Search, Check, Close, Delete,
  UserFilled, Edit, Key
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import userApi from '@/api/userApi'

// 状态管理
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const userList = ref([])
const selectedUsers = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalUsers = ref(0)

// 搜索条件
const searchKeyword = ref('')
const filterRole = ref('')
const filterStatus = ref('')

// 统计信息
const stats = reactive({
  total: 0,
  admin: 0,
  operator: 0,
  viewer: 0
})

// 对话框状态
const showUserDialog = ref(false)
const showResetPasswordDialog = ref(false)
const isEditMode = ref(false)
const submitting = ref(false)
const passwordSubmitting = ref(false)
const customResetPassword = ref(false) // 是否自定义重置密码

// 表单引用
const userFormRef = ref()
const passwordFormRef = ref()

// 表单数据
const userForm = reactive({
  id: null,
  username: '',
  fullName: '',
  email: '',
  phone: '',
  role: 'VIEWER',
  enabled: true,
  password: '',
  confirmPassword: ''
})

const passwordForm = reactive({
  userId: null,
  password: '',
  confirmPassword: ''
})

// 表单验证规则
const userFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
  ],
  fullName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: false, type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== userForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const passwordFormRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return isEditMode.value ? '编辑用户' : '添加用户'
})

// 方法
const getRoleDisplayName = (role) => {
  const roleNames = {
    'VIEWER': '查看员',
    'OPERATOR': '操作员',
    'ADMIN': '管理员',
    'SUPER_ADMIN': '超级管理员'
  }
  return roleNames[role] || role
}

const getRoleTagType = (role) => {
  const types = {
    'VIEWER': 'info',
    'OPERATOR': 'success',
    'ADMIN': 'warning',
    'SUPER_ADMIN': 'danger'
  }
  return types[role] || 'info'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  try {
    // 兼容ISO格式日期字符串
    const date = new Date(dateString.replace(/T/, ' ').replace(/\.\d+/, ''))
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return dateString
  }
}

// 加载用户数据（适配后端返回格式：success/message/data）
const loadUserData = async () => {
  try {
    loading.value = true
    
    // 构造查询参数
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      username: searchKeyword.value.trim() || undefined,
      role: filterRole.value || undefined,
      enabled: filterStatus.value ? filterStatus.value === 'true' : undefined
    }
    
    // 调用真实API
    const response = await userApi.getUserList(params)
    
    // 适配后端返回格式（success: true/false, message, data）
    if (response.success === true) {
      // 兼容分页接口返回格式（data.records/data.total）
      if (response.data.records) {
        userList.value = response.data.records || []
        totalUsers.value = response.data.total || 0
      } else {
        // 兼容全量接口返回格式（data为数组）
        userList.value = response.data || []
        totalUsers.value = userList.value.length
      }
      // 加载统计数据
      loadUserStats()
      ElMessage.success('用户数据加载成功')
    } else {
      ElMessage.error('加载用户数据失败: ' + (response.message || '未知错误'))
    }
    
  } catch (error) {
    console.error('加载用户数据失败:', error)
    ElMessage.error('加载用户数据失败: ' + (error.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

// 加载用户统计数据（适配后端返回格式）
const loadUserStats = async () => {
  try {
    const response = await userApi.getUserStats()
    if (response.success === true) {
      Object.assign(stats, response.data)
    } else {
      // 降级：前端计算统计
      updateStats()
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 降级：前端计算统计
    updateStats()
  }
}

// 更新统计信息（前端降级）
const updateStats = () => {
  stats.total = userList.value.length
  stats.admin = userList.value.filter(u => u.role === 'ADMIN').length
  stats.operator = userList.value.filter(u => u.role === 'OPERATOR').length
  stats.viewer = userList.value.filter(u => u.role === 'VIEWER').length
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadUserData()
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  filterRole.value = ''
  filterStatus.value = ''
  handleSearch()
}

// 刷新数据
const refreshData = () => {
  loadUserData()
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection.map(user => user.id)
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  loadUserData()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadUserData()
}

// 显示添加对话框
const showAddDialog = () => {
  isEditMode.value = false
  resetUserForm()
  showUserDialog.value = true
}

// 重置用户表单
const resetUserForm = () => {
  Object.assign(userForm, {
    id: null,
    username: '',
    fullName: '',
    email: '',
    phone: '',
    role: 'VIEWER',
    enabled: true,
    password: '',
    confirmPassword: ''
  })
  if (userFormRef.value) {
    userFormRef.value.clearValidate()
  }
}

// 编辑用户
const handleEdit = (user) => {
  isEditMode.value = true
  Object.assign(userForm, {
    id: user.id,
    username: user.username,
    fullName: user.fullName,
    email: user.email,
    phone: user.phone,
    role: user.role,
    enabled: user.enabled,
    password: '',
    confirmPassword: ''
  })
  showUserDialog.value = true
}

// 删除用户（适配后端返回格式）
const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可恢复。`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用真实删除API
    const response = await userApi.deleteUser(user.id)
    if (response.success === true) {
      ElMessage.success('用户删除成功')
      loadUserData()
    } else {
      ElMessage.error('删除失败: ' + (response.message || '未知错误'))
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除用户失败: ' + (error.message || '操作取消'))
    }
  }
}

// 批量删除（适配后端返回格式）
const handleBatchDelete = async () => {
  if (selectedUsers.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedUsers.value.length} 个用户吗？此操作不可恢复。`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用批量删除API
    const response = await userApi.batchDeleteUsers(selectedUsers.value)
    if (response.success === true) {
      ElMessage.success('批量删除成功')
      selectedUsers.value = []
      loadUserData()
    } else {
      ElMessage.error('批量删除失败: ' + (response.message || '未知错误'))
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败: ' + (error.message || '操作取消'))
    }
  }
}

// 状态切换（适配后端返回格式）
const handleStatusChange = async (userId, enabled) => {
  const user = userList.value.find(u => u.id === userId)
  if (!user) return
  
  user.statusLoading = true
  
  try {
    // 调用状态更新API
    const response = await userApi.updateUserStatus(userId, { enabled })
    if (response.success === true) {
      ElMessage.success(`用户已${enabled ? '启用' : '禁用'}`)
      user.enabled = enabled
    } else {
      ElMessage.error('状态修改失败: ' + (response.message || '未知错误'))
      // 恢复原始状态
      user.enabled = !enabled
    }
  } catch (error) {
    ElMessage.error('状态修改失败: ' + (error.message || '网络异常'))
    // 恢复原始状态
    user.enabled = !enabled
  } finally {
    user.statusLoading = false
  }
}

// 批量启用（适配后端返回格式）
const handleBatchEnable = async () => {
  if (selectedUsers.value.length === 0) return
  
  try {
    // 调用批量更新状态API
    const response = await userApi.batchUpdateUserStatus(selectedUsers.value, true)
    if (response.success === true) {
      ElMessage.success('批量启用成功')
      selectedUsers.value = []
      loadUserData()
    } else {
      ElMessage.error('批量启用失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('批量启用失败: ' + (error.message || '网络异常'))
  }
}

// 批量禁用（适配后端返回格式）
const handleBatchDisable = async () => {
  if (selectedUsers.value.length === 0) return
  
  try {
    // 调用批量更新状态API
    const response = await userApi.batchUpdateUserStatus(selectedUsers.value, false)
    if (response.success === true) {
      ElMessage.success('批量禁用成功')
      selectedUsers.value = []
      loadUserData()
    } else {
      ElMessage.error('批量禁用失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('批量禁用失败: ' + (error.message || '网络异常'))
  }
}

// 重置密码（弹窗初始化）
const handleResetPassword = (user) => {
  passwordForm.userId = user.id
  passwordForm.password = ''
  passwordForm.confirmPassword = ''
  customResetPassword.value = false // 默认重置为默认密码
  
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
  
  showResetPasswordDialog.value = true
}

// 默认密码重置（适配后端返回格式）
const handleDefaultResetPassword = async () => {
  try {
    passwordSubmitting.value = true
    
    // 调用后端重置密码为默认值的API
    const response = await userApi.resetPassword(passwordForm.userId)
    if (response.success === true) {
      ElMessage.success('密码重置成功，默认密码：123456')
      showResetPasswordDialog.value = false
    } else {
      ElMessage.error('密码重置失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('密码重置失败: ' + (error.message || '网络异常'))
  } finally {
    passwordSubmitting.value = false
  }
}

// 自定义密码重置（适配后端返回格式）
const handlePasswordSubmit = async () => {
  if (!passwordFormRef.value) return
  
  try {
    const valid = await passwordFormRef.value.validate()
    if (!valid) return
    
    passwordSubmitting.value = true
    
    // 调用修改密码API
    const response = await userApi.changePassword(passwordForm.userId, {
      password: passwordForm.password
    })
    
    if (response.success === true) {
      ElMessage.success('密码重置成功')
      showResetPasswordDialog.value = false
    } else {
      ElMessage.error('密码重置失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('密码重置失败: ' + (error.message || '网络异常'))
  } finally {
    passwordSubmitting.value = false
  }
}

// 提交用户表单（适配后端返回格式）
const handleSubmit = async () => {
  if (!userFormRef.value) return
  
  try {
    const valid = await userFormRef.value.validate()
    if (!valid) return
    
    submitting.value = true
    let response
    
    if (isEditMode.value) {
      // 编辑用户：传递部分字段（避免覆盖密码）
      response = await userApi.updateUser(userForm.id, {
        fullName: userForm.fullName,
        email: userForm.email,
        phone: userForm.phone,
        role: userForm.role,
        enabled: userForm.enabled
      })
    } else {
      // 添加用户：传递完整字段
      response = await userApi.createUser({
        username: userForm.username,
        password: userForm.password,
        fullName: userForm.fullName,
        email: userForm.email,
        phone: userForm.phone,
        role: userForm.role,
        enabled: userForm.enabled
      })
    }
    
    if (response.success === true) {
      ElMessage.success(isEditMode.value ? '用户更新成功' : '用户创建成功')
      showUserDialog.value = false
      loadUserData()
    } else {
      ElMessage.error('操作失败: ' + (response.message || '未知错误'))
    }
    
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.message || '网络异常'))
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
const handleDialogClose = () => {
  showUserDialog.value = false
  if (userFormRef.value) {
    userFormRef.value.clearValidate()
  }
}

// 生命周期
onMounted(() => {
  loadUserData()
})
</script>

<style scoped>
.user-management-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left h1 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
}

.header-left p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-right: 15px;
}

.stat-icon.total {
  background: linear-gradient(135deg, #409eff, #79bbff);
  color: white;
}

.stat-icon.admin {
  background: linear-gradient(135deg, #e6a23c, #eebe77);
  color: white;
}

.stat-icon.operator {
  background: linear-gradient(135deg, #67c23a, #95d475);
  color: white;
}

.stat-icon.viewer {
  background: linear-gradient(135deg, #909399, #b1b3b8);
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

/* 搜索区域 */
.search-section {
  margin-bottom: 20px;
}

.search-content {
  padding: 16px 0;
}

.search-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: flex-end;
}

/* 表格区域 */
.table-section {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-actions {
  display: flex;
  gap: 8px;
}

/* 用户单元格 */
.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 500;
  color: #303133;
}

.email {
  font-size: 12px;
  color: #909399;
}

/* 状态文本 */
.status-text {
  margin-left: 8px;
  font-size: 12px;
}

.status-text.enabled {
  color: #67c23a;
}

.status-text.disabled {
  color: #f56c6c;
}

/* 分页 */
.pagination-section {
  margin-top: 20px;
  text-align: center;
}

/* 重置密码提示 */
.reset-tip {
  padding: 10px 0 20px;
  color: #666;
}

.default-pwd {
  color: #e6a23c;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-management-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .stats-row .el-col {
    margin-bottom: 10px;
  }
  
  .search-content .el-col {
    margin-bottom: 10px;
  }
  
  .search-actions {
    justify-content: flex-start;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .batch-actions {
    flex-wrap: wrap;
  }
}
</style>