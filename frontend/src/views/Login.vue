<template>
  <div class="login-container">
    <div class="login-background">
      <div class="login-box">
        <!-- 登录表单 -->
        <div class="login-form">
          <div class="login-header">
            <h1>智能仓储监控系统</h1>
            <p>欢迎回来，请登录您的账户</p>
          </div>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form-content"
            @keyup.enter="handleLogin"
          >
            <!-- 用户名输入 -->
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>

            <!-- 密码输入 -->
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>

            <!-- 记住密码和忘记密码 -->
            <div class="login-options">
              <el-checkbox v-model="loginForm.rememberMe">
                记住密码
              </el-checkbox>
              <el-link type="primary" :underline="false">
                忘记密码？
              </el-link>
            </div>

            <!-- 登录按钮 -->
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-button"
                :loading="loading"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 底部信息 -->
          <div class="login-footer">
            <p>默认管理员账户: admin / admin123</p>
          </div>
        </div>

        <!-- 系统介绍 -->
        <div class="system-info">
          <div class="info-content">
            <h2>智能仓储环境监控系统</h2>
            <div class="feature-list">
              <div class="feature-item">
                <span class="feature-icon">🌡️</span>
                <div class="feature-text">
                  <h3>实时温度监控</h3>
                  <p>24小时不间断监控仓库温度变化</p>
                </div>
              </div>
              <div class="feature-item">
                <span class="feature-icon">💧</span>
                <div class="feature-text">
                  <h3>智能湿度控制</h3>
                  <p>精确控制仓库湿度，保障存储安全</p>
                </div>
              </div>
              <div class="feature-item">
                <span class="feature-icon">🚨</span>
                <div class="feature-text">
                  <h3>异常告警系统</h3>
                  <p>及时发现并预警环境异常情况</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { login as loginApi } from '@/api/auth'

// 路由和状态管理
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const loginFormRef = ref()
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 表单验证
    const valid = await loginFormRef.value.validate()
    if (!valid) return

    // 开始登录
    loading.value = true

    // 调用登录API
    const response = await loginApi({
      username: loginForm.username,
      password: loginForm.password
    })

    console.log('完整登录响应:', response)
    console.log('响应数据结构:', {
      success: response.success,
      message: response.message,
      data: response.data
    })

    // 根据后端返回的数据结构处理响应
    if (response.success) {
      console.log('登录成功，准备保存用户信息')
      
      // 保存用户信息和token
      const userData = {
        username: response.data.username,
        role: response.data.role,
        fullName: response.data.fullName,
        token: response.data.token
      }
      
      console.log('用户数据:', userData)
      console.log('Token:', response.data.token)
      
      authStore.setUser(userData)
      authStore.setToken(response.data.token)
      
      // 记住密码功能 - 只存储用户名，不存储密码
      if (loginForm.rememberMe) {
        localStorage.setItem('remembered_username', loginForm.username)
      } else {
        localStorage.removeItem('remembered_username')
      }
      
      // 显示成功消息
      ElMessage.success('登录成功！')
      
      console.log('准备跳转到首页...')
      // 跳转到首页
      router.push('/')
      console.log('跳转指令已发送')
    } else {
      ElMessage.error(response.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    ElMessage.error(error.message || '登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载记住的用户名
onMounted(() => {
  const savedUsername = localStorage.getItem('remembered_username')
  
  if (savedUsername) {
    loginForm.username = savedUsername
    loginForm.rememberMe = true
  }
})
</script>

<style scoped>
.login-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.login-background {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  width: 1000px;
  height: 600px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  display: flex;
  overflow: hidden;
}

/* 登录表单样式 */
.login-form {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: white;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-form-content {
  max-width: 320px;
  margin: 0 auto;
  width: 100%;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

:deep(.el-checkbox) {
  color: #606266;
}

.login-button {
  width: 100%;
  font-size: 16px;
  height: 48px;
}

.login-footer {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.login-footer p {
  color: #909399;
  font-size: 12px;
  margin: 0;
}

/* 系统信息样式 */
.system-info {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 40px;
  display: flex;
  align-items: center;
}

.info-content {
  max-width: 400px;
}

.system-info h2 {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 40px;
  text-align: center;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
}

.feature-icon {
  font-size: 24px;
  margin-top: 4px;
}

.feature-text h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: white;
}

.feature-text p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-box {
    flex-direction: column;
    height: auto;
    margin: 20px;
  }
  
  .system-info {
    display: none;
  }
  
  .login-form {
    padding: 40px 30px;
  }
}
</style>