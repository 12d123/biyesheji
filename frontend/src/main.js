import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

// 创建应用实例
const app = createApp(App)

// 创建 Pinia 实例
const pinia = createPinia()

// 注册插件
app.use(pinia)
app.use(ElementPlus)
app.use(router)

// 在挂载前初始化认证状态
const initializeApp = async () => {
  try {
    // 必须在app.use(pinia)之后
    const { useAuthStore } = await import('@/stores/auth')
    const authStore = useAuthStore()
    authStore.initialize()
    
    console.log('✅ 认证状态初始化完成')
    console.log('✅ Token:', authStore.token ? '已存在' : '不存在')
    console.log('✅ 用户:', authStore.user || '未登录')
  } catch (error) {
    console.error('❌ 认证初始化失败:', error)
  }
  
  // 挂载应用
  app.mount('#app')
  
  console.log('✅ Vue 应用已成功启动')
  console.log('✅ Pinia 状态管理已注册')
}

initializeApp()