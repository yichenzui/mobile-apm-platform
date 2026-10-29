import { createRouter, createWebHistory } from 'vue-router'
import PerformanceDashboard from '@/views/dashboard/PerformanceDashboard.vue'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: PerformanceDashboard,
    meta: { title: '性能监控仪表盘', roles: ['ADMIN', 'USER'] }
  },
  // 以下路由暂时保留，但组件还未创建
  {
    path: '/analysis/performance',
    name: 'PerformanceAnalysis',
    component: () => import('@/views/analysis/PerformanceAnalysis.vue'),
    meta: { title: '性能分析', roles: ['ADMIN', 'USER'] }
  },
  {
    path: '/analysis/region',
    name: 'RegionAnalysis',
    component: () => import('@/views/analysis/RegionAnalysis.vue'),
    meta: { title: '地区分析', roles: ['ADMIN'] }
  },
  {
    path: '/analysis/version',
    name: 'VersionAnalysis',
    component: () => import('@/views/analysis/VersionAnalysis.vue'),
    meta: { title: '版本对比', roles: ['ADMIN'] }
  },
  {
    path: '/alert',
    name: 'AlertCenter',
    component: () => import('@/views/alert/AlertCenter.vue'),
    meta: { title: '告警中心', roles: ['ADMIN', 'USER'] }
  },
  {
    path: '/alert/detail/:id',
    name: 'AlertDetail',
    component: () => import('@/views/alert/AlertDetail.vue'),
    meta: { title: '告警详情', roles: ['ADMIN', 'USER'] }
  },
  {
    path: '/issue',
    name: 'IssueCenter',
    component: () => import('@/views/issue/IssueCenter.vue'),
    meta: { title: '问题中心', roles: ['ADMIN'] }
  },
  {
    path: '/issue/detail/:id',
    name: 'IssueDetail',
    component: () => import('@/views/issue/IssueDetail.vue'),
    meta: { title: '问题详情', roles: ['ADMIN'] }
  },
  {
    path: '/ai-assistant',
    name: 'AiAssistant',
    component: () => import('@/views/ai/AiAssistant.vue'),
    meta: { title: 'AI 智能助手', roles: ['ADMIN', 'USER'] }
  },
  {
    path: '/simulator',
    name: 'Simulator',
    component: () => import('@/views/Simulator.vue'),
    meta: { title: '数据采集模拟器', roles: ['ADMIN'] }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心', roles: ['ADMIN', 'USER'] }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  },
  // 兜底：未匹配的路径统一跳转 404，避免白屏
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 移动应用性能监控平台` : '移动应用性能监控平台'

  // 检查是否需要登录验证
  const requiresAuth = !['/login', '/register'].includes(to.path)
  const token = localStorage.getItem('token')
  let userInfo = null
  if (token) {
    try {
      const raw = localStorage.getItem('userInfo')
      userInfo = raw ? JSON.parse(raw) : null
    } catch (e) {
      userInfo = null
      localStorage.removeItem('userInfo')
    }
  }
  const userRole = userInfo ? userInfo.role : null

  // token 存在但 userInfo 缺失 → 登录态已损坏，强制重新登录
  if (requiresAuth && token && !userInfo) {
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    next('/login')
    return
  }

  if (requiresAuth && !token) {
    // 需要登录但没有token，跳转到登录页
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    // 已登录用户访问登录/注册页，跳转到首页
    next('/dashboard')
  } else if (to.meta && to.meta.roles && (!userRole || !to.meta.roles.includes(userRole))) {
    // 角色权限校验：无角色或角色不匹配时都视为无权限，跳转回首页
    next('/dashboard')
  } else {
    next()
  }
})

export default router
