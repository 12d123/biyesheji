import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
// 补充导入 ElMessage
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { 
      title: '登录 - 智能仓储监控系统',
      requiresAuth: false 
    }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: { 
      title: '智能仓储监控系统',
      requiresAuth: true 
    }
  },
  {
    path: '/system-settings',
    name: 'SystemSettings',
    component: () => import('@/views/SystemSettings.vue'),
    meta: { 
      title: '系统设置 - 智能仓储监控系统',
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/data-view',
    name: 'DataView',
    component: () => import('@/views/DataView.vue'),
    meta: { 
      title: '数据查看 - 智能仓储监控系统',
      requiresAuth: true 
    }
  },
  {
    path: '/data-management',
    name: 'DataManagement',
    component: () => import('@/views/DataManagement.vue'),
    meta: { 
      title: '数据管理 - 智能仓储监控系统',
      requiresAuth: true,
      requiresOperator: true
    }
  },
  {
    path: '/user-management',
    name: 'UserManagement',
    component: () => import('@/views/UserManagement.vue'),
    meta: { 
      title: '用户管理 - 智能仓储监控系统',
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/unauthorized',
    name: 'Unauthorized',
    component: () => import('@/views/Unauthorized.vue'),
    meta: { 
      title: '未授权访问',
      requiresAuth: false 
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL || '/'),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }
  
  const authStore = useAuthStore()
  
  // 检查是否需要认证
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
    return
  }
  
  // 检查管理员权限
  if (to.meta.requiresAdmin && !authStore.hasPermission('ADMIN')) {
    ElMessage.warning('无权限访问该页面')
    next('/unauthorized')
    return
  }
  
  // 检查操作员权限（VIEWER 没有，OPERATOR 和 ADMIN 有）
  if (to.meta.requiresOperator && !authStore.hasPermission('OPERATOR') && !authStore.hasPermission('ADMIN')) {
    ElMessage.warning('无权限访问该页面')
    next('/unauthorized')
    return
  }
  
  // 如果已登录且访问登录页，跳转到首页
  if (to.path === '/login' && authStore.isAuthenticated) {
    next('/')
    return
  }
  
  next()
})

export default router